package com.entrecheckpoints.checkpoint.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Resuelve una wishlist compartida de Nintendo usando el mismo documento público que
 * Nintendo entrega al navegador. El hash de la URL contiene los SKU, pero los enlaces
 * de producto se generan en el cliente mediante JavaScript; por eso se utiliza un
 * WebView temporal y aislado en lugar de fingir que el HTML inicial ya contiene todo.
 */
class NintendoSharedWishlistResolver(context: Context) {
    private val appContext = context.applicationContext

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun resolve(inputUrl: String, expectedItems: Int): List<String> =
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                val handler = Handler(Looper.getMainLooper())
                val collected = linkedSetOf<String>()
                var attempts = 0
                var stableRounds = 0
                var lastCount = 0
                var finished = false

                val webView = WebView(appContext).apply {
                    visibility = View.INVISIBLE
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = false
                    settings.userAgentString =
                        "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 " +
                            "Chrome/131 Mobile Safari/537.36 Checkpoint/1.0.3"
                    measure(
                        View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY),
                    )
                    layout(0, 0, 1080, 1920)
                }

                fun cleanup() {
                    handler.removeCallbacksAndMessages(null)
                    runCatching {
                        webView.stopLoading()
                        webView.loadUrl("about:blank")
                        webView.clearHistory()
                        webView.removeAllViews()
                        webView.destroy()
                    }
                }

                fun fail(message: String, cause: Throwable? = null) {
                    if (finished || !continuation.isActive) return
                    finished = true
                    cleanup()
                    continuation.resumeWithException(StoreFetchException(message, cause))
                }

                fun complete() {
                    if (finished || !continuation.isActive) return
                    finished = true
                    val result = collected.toList()
                    cleanup()
                    if (result.isEmpty()) {
                        continuation.resumeWithException(
                            StoreFetchException(
                                "Nintendo abrió la wishlist, pero no expuso enlaces de producto. " +
                                    "Comprueba que sea un enlace /wish-list/share/ vigente.",
                            ),
                        )
                    } else {
                        continuation.resume(result)
                    }
                }

                fun pollDom() {
                    if (finished || !continuation.isActive) return
                    attempts += 1
                    webView.evaluateJavascript(EXTRACT_PRODUCT_LINKS_SCRIPT) { raw ->
                        if (finished || !continuation.isActive) return@evaluateJavascript
                        parseJavascriptArray(raw).forEach { collected += normalizeProductUrl(it) }

                        stableRounds = if (collected.size == lastCount) stableRounds + 1 else 0
                        lastCount = collected.size

                        val reachedExpected = expectedItems > 0 && collected.size >= expectedItems
                        val settled = attempts >= MIN_ATTEMPTS && stableRounds >= STABLE_ROUNDS
                        val exhausted = attempts >= MAX_ATTEMPTS

                        when {
                            reachedExpected || settled || exhausted -> complete()
                            else -> handler.postDelayed(::pollDom, POLL_DELAY_MS)
                        }
                    }
                }

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) = Unit

                    override fun onPageFinished(view: WebView?, url: String?) {
                        handler.postDelayed(::pollDom, INITIAL_DELAY_MS)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?,
                    ) {
                        if (request?.isForMainFrame == true) {
                            fail(
                                "No pude abrir la wishlist compartida de Nintendo: " +
                                    (error?.description?.toString() ?: "error de red"),
                            )
                        }
                    }
                }

                continuation.invokeOnCancellation { handler.post(::cleanup) }
                handler.postDelayed(
                    { fail("Nintendo tardó demasiado en cargar la wishlist compartida.") },
                    TIMEOUT_MS,
                )
                webView.loadUrl(inputUrl)
            }
        }

    companion object {
        private const val INITIAL_DELAY_MS = 1_200L
        private const val POLL_DELAY_MS = 900L
        private const val TIMEOUT_MS = 45_000L
        private const val MIN_ATTEMPTS = 4
        private const val MAX_ATTEMPTS = 30
        private const val STABLE_ROUNDS = 4

        /**
         * Desplaza la página para activar carga diferida y recoge tanto enlaces visibles
         * como URLs escapadas dentro del HTML hidratado.
         */
        private val EXTRACT_PRODUCT_LINKS_SCRIPT = """
            (() => {
              const urls = new Set();
              const accept = (value) => {
                if (!value || typeof value !== 'string') return;
                try {
                  const absolute = new URL(value.replace(/\\u002F/g, '/').replace(/\\\//g, '/'), location.href).href;
                  if (/\/store\/products\//i.test(absolute)) urls.add(absolute.split('#')[0]);
                } catch (_) {}
              };
              document.querySelectorAll('a[href]').forEach((node) => accept(node.getAttribute('href')));
              document.querySelectorAll('[data-href],[data-url],[data-product-url]').forEach((node) => {
                accept(node.getAttribute('data-href'));
                accept(node.getAttribute('data-url'));
                accept(node.getAttribute('data-product-url'));
              });
              const html = document.documentElement.innerHTML;
              const absoluteMatches = html.match(/https?:\\?\/\\?\/[^\"'<>\\s]+\/store\/products\/[^\"'<>\\s]+/gi) || [];
              const relativeMatches = html.match(/\\?\/(?:[a-z]{2}-[a-z]{2}\\?\/)?store\\?\/products\\?\/[^\"'<>\\s]+/gi) || [];
              absoluteMatches.concat(relativeMatches).forEach(accept);
              window.scrollTo(0, Math.max(document.body.scrollHeight, document.documentElement.scrollHeight));
              document.querySelectorAll('main,section,[role="main"],div').forEach((node) => {
                if (node.scrollHeight > node.clientHeight + 200) node.scrollTop = node.scrollHeight;
              });
              return Array.from(urls);
            })();
        """.trimIndent()

        internal fun parseJavascriptArray(raw: String?): List<String> {
            if (raw.isNullOrBlank() || raw == "null") return emptyList()
            val array = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyList()
            return buildList {
                for (index in 0 until array.length()) {
                    array.optString(index)
                        .takeIf(String::isNotBlank)
                        ?.let(::add)
                }
            }
        }

        internal fun normalizeProductUrl(url: String): String {
            val clean = url.substringBefore('#').trim()
            val queryIndex = clean.indexOf('?')
            if (queryIndex < 0) return clean.trimEnd('/')
            return clean.substring(0, queryIndex).trimEnd('/') + clean.substring(queryIndex)
        }
    }
}
