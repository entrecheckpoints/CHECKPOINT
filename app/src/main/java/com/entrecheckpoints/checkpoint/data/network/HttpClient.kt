package com.entrecheckpoints.checkpoint.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpClient {
    data class Response(val body: String, val finalUrl: String, val status: Int)

    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): Response = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 15_000
        connection.readTimeout = 25_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept-Language", "es-MX,es;q=0.9,en;q=0.5")
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/131 Mobile Safari/537.36 Checkpoint/0.3.1",
        )
        headers.forEach(connection::setRequestProperty)
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.use { input ->
            BufferedReader(InputStreamReader(input)).readText()
        }.orEmpty()
        val finalUrl = connection.url.toString()
        connection.disconnect()
        if (status !in 200..299) throw StoreFetchException("La tienda respondió con HTTP $status.")
        Response(body, finalUrl, status)
    }
}

class StoreFetchException(message: String, cause: Throwable? = null) : Exception(message, cause)
