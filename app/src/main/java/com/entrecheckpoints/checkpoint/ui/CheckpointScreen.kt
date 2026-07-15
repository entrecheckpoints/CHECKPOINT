@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.entrecheckpoints.checkpoint.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.entrecheckpoints.checkpoint.data.analytics.BudgetPlanner
import com.entrecheckpoints.checkpoint.data.analytics.DealAnalytics
import com.entrecheckpoints.checkpoint.data.local.GameEntity
import com.entrecheckpoints.checkpoint.data.local.GameEventEntity
import com.entrecheckpoints.checkpoint.data.local.PricePointEntity
import com.entrecheckpoints.checkpoint.data.local.SyncRunEntity
import com.entrecheckpoints.checkpoint.data.model.AlertEventType
import com.entrecheckpoints.checkpoint.data.model.GameFormat
import com.entrecheckpoints.checkpoint.data.model.LibraryStatus
import com.entrecheckpoints.checkpoint.data.model.ProductEdition
import com.entrecheckpoints.checkpoint.data.model.Store
import com.entrecheckpoints.checkpoint.data.model.SubscriptionService
import com.entrecheckpoints.checkpoint.ui.components.Barcode
import com.entrecheckpoints.checkpoint.ui.components.CheckpointPanel
import com.entrecheckpoints.checkpoint.ui.components.PriceChart
import com.entrecheckpoints.checkpoint.ui.components.SectionRule
import com.entrecheckpoints.checkpoint.ui.components.StoreBadge
import com.entrecheckpoints.checkpoint.ui.components.TechnicalLabel
import com.entrecheckpoints.checkpoint.ui.components.editorialTexture
import com.entrecheckpoints.checkpoint.ui.components.storeColor
import com.entrecheckpoints.checkpoint.ui.model.AppSection
import com.entrecheckpoints.checkpoint.ui.theme.CheckpointStyle
import com.entrecheckpoints.checkpoint.ui.theme.CheckpointTheme
import com.entrecheckpoints.checkpoint.ui.theme.CheckpointThemeMode
import com.entrecheckpoints.checkpoint.ui.theme.checkpointShape
import com.entrecheckpoints.checkpoint.ui.theme.checkpointVisualsFor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun CheckpointAppRoot(viewModel: MainViewModel) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    CheckpointTheme(themeMode) {
        val context = LocalContext.current
        val games by viewModel.games.collectAsStateWithLifecycle()
        val allHistory by viewModel.allHistory.collectAsStateWithLifecycle()
        val events by viewModel.events.collectAsStateWithLifecycle()
        val syncRuns by viewModel.syncRuns.collectAsStateWithLifecycle()
        val selectedId by viewModel.selectedGameId.collectAsStateWithLifecycle()
        val selectedHistory by viewModel.selectedHistory.collectAsStateWithLifecycle()
        val section by viewModel.section.collectAsStateWithLifecycle()
        val query by viewModel.query.collectAsStateWithLifecycle()
        val storeFilter by viewModel.storeFilter.collectAsStateWithLifecycle()
        val libraryFilter by viewModel.libraryFilter.collectAsStateWithLifecycle()
        val busy by viewModel.isBusy.collectAsStateWithLifecycle()
        val pendingUrl by viewModel.pendingAddUrl.collectAsStateWithLifecycle()
        val interval by viewModel.intervalHours.collectAsStateWithLifecycle()
        val notifications by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
        val budget by viewModel.monthlyBudgetCents.collectAsStateWithLifecycle()
        val activeSubscriptions by viewModel.activeSubscriptions.collectAsStateWithLifecycle()

        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var showAdd by remember { mutableStateOf(false) }
        var showBulk by remember { mutableStateOf(false) }
        var addInitialUrl by remember { mutableStateOf("") }
        var pendingExport by remember { mutableStateOf<String?>(null) }

        val notificationPermission = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted -> if (!granted) viewModel.updateNotifications(false) }
        val importLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("No se pudo leer el archivo.")
                }.onSuccess { viewModel.importJson(it) }
                    .onFailure { scope.launch { snackbar.showSnackbar(it.message ?: "No se pudo importar.") } }
            }
        }
        val exportLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json"),
        ) { uri ->
            val content = pendingExport
            if (uri != null && content != null) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(content) }
                        ?: error("No se pudo crear el archivo.")
                }.onFailure { scope.launch { snackbar.showSnackbar(it.message ?: "No se pudo exportar.") } }
            }
            pendingExport = null
        }

        LaunchedEffect(Unit) {
            viewModel.messages.collect { snackbar.showSnackbar(it) }
        }
        LaunchedEffect(pendingUrl) {
            pendingUrl?.let {
                addInitialUrl = it
                showAdd = true
                viewModel.consumePendingUrl()
            }
        }
        LaunchedEffect(notifications) {
            if (
                notifications && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val historyByGame = remember(allHistory) { allHistory.groupBy { it.gameId } }
        val selectedGame = games.firstOrNull { it.id == selectedId }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize().editorialTexture(),
            bottomBar = {
                SectionNavigation(
                    selected = section,
                    unreadEvents = events.count { !it.seen },
                    onSelect = viewModel::navigate,
                )
            },
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding(),
            ) {
                AppHeader(
                    busy = busy,
                    section = section,
                    onAdd = { showAdd = true },
                    onRefresh = viewModel::refreshAll,
                )
                when (section) {
                    AppSection.TRACKING -> TrackingScreen(
                        games = games,
                        historyByGame = historyByGame,
                        query = query,
                        storeFilter = storeFilter,
                        activeSubscriptions = activeSubscriptions,
                        busy = busy,
                        onQuery = { viewModel.query.value = it },
                        onStore = { viewModel.storeFilter.value = it },
                        onOpen = viewModel::selectGame,
                        onRefresh = viewModel::refreshGame,
                        onAdd = { showAdd = true },
                    )
                    AppSection.DEALS -> DealsScreen(
                        games = games,
                        events = events,
                        historyByGame = historyByGame,
                        onOpen = viewModel::selectGame,
                    )
                    AppSection.COMPARE -> CompareScreen(
                        games = games,
                        historyByGame = historyByGame,
                        onOpen = viewModel::selectGame,
                    )
                    AppSection.LIBRARY -> LibraryScreen(
                        games = games,
                        historyByGame = historyByGame,
                        filter = libraryFilter,
                        budgetCents = budget,
                        onFilter = { viewModel.libraryFilter.value = it },
                        onOpen = viewModel::selectGame,
                        onBudget = viewModel::updateBudget,
                    )
                    AppSection.SYSTEM -> SystemScreen(
                        games = games,
                        runs = syncRuns,
                        interval = interval,
                        notifications = notifications,
                        themeMode = themeMode,
                        activeSubscriptions = activeSubscriptions,
                        onInterval = viewModel::updateInterval,
                        onNotifications = viewModel::updateNotifications,
                        onTheme = viewModel::updateTheme,
                        onToggleSubscription = viewModel::toggleActiveSubscription,
                        onImportBackup = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                        onExportBackup = {
                            viewModel.exportJson { json ->
                                pendingExport = json
                                exportLauncher.launch("checkpoint-backup-${System.currentTimeMillis()}.json")
                            }
                        },
                        onBulkImport = { showBulk = true },
                    )
                }
            }
        }

        if (showAdd) {
            AddGameDialog(
                initialUrl = addInitialUrl,
                busy = busy,
                onDismiss = {
                    showAdd = false
                    addInitialUrl = ""
                },
                onAdd = { url, target ->
                    viewModel.addGame(url, target) {
                        showAdd = false
                        addInitialUrl = ""
                    }
                },
            )
        }
        if (showBulk) {
            BulkImportDialog(
                busy = busy,
                onDismiss = { showBulk = false },
                onImport = { raw -> viewModel.importWishlist(raw) { showBulk = false } },
            )
        }
        if (selectedGame != null) {
            GameDetailSheet(
                game = selectedGame,
                history = selectedHistory,
                activeSubscriptions = activeSubscriptions,
                busy = busy,
                onDismiss = { viewModel.selectGame(null) },
                onRefresh = { viewModel.refreshGame(selectedGame.id) },
                onDelete = { viewModel.deleteGame(selectedGame.id) },
                onSetTarget = { viewModel.setTarget(selectedGame.id, it) },
                onAlerts = { anyDrop, target, discount, amount, low, ending ->
                    viewModel.updateAlerts(selectedGame.id, anyDrop, target, discount, amount, low, ending)
                },
                onComparison = { key, edition -> viewModel.updateComparison(selectedGame.id, key, edition) },
                onLibrary = { status, paid, date, format, rating ->
                    viewModel.updateLibrary(selectedGame.id, status, paid, date, format, rating)
                },
                onSubscriptions = { viewModel.updateSubscriptions(selectedGame.id, it) },
                onNotes = { viewModel.updateNotes(selectedGame.id, it) },
                onOpenStore = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(selectedGame.url))) },
                onOpenSteamDb = if (selectedGame.storeId == Store.STEAM.id && selectedGame.productId != null) {
                    { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://steamdb.info/app/${selectedGame.productId}/"))) }
                } else null,
            )
        }
    }
}

@Composable
private fun AppHeader(
    busy: Boolean,
    section: AppSection,
    onAdd: () -> Unit,
    onRefresh: () -> Unit,
) {
    val style = CheckpointStyle.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(style.surfaceRaised.copy(alpha = 0.97f))
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TechnicalLabel("EC / PRICE TRACKING SYSTEM", color = style.muted)
            TechnicalLabel("BUILD 03.1 · ${section.label}", color = style.muted)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("CHECKPOINT", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                TechnicalLabel("by Entre Checkpoints", color = style.accentSecondary)
            }
            IconButton(onClick = onRefresh, enabled = !busy) {
                if (busy) CircularProgressIndicator(Modifier.size(21.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Refresh, contentDescription = "Actualizar todo", tint = style.ink)
            }
            Spacer(Modifier.width(4.dp))
            Button(
                onClick = onAdd,
                shape = checkpointShape(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text("AÑADIR")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Barcode("CHECKPOINT-ENTRE-CHECKPOINTS", Modifier.width(92.dp).height(12.dp))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f).height(1.dp).background(style.borderStrong))
            Spacer(Modifier.width(10.dp))
            TechnicalLabel("ISSUE 003.1", color = style.ink)
        }
    }
}
@Composable
private fun SectionNavigation(
    selected: AppSection,
    unreadEvents: Int,
    onSelect: (AppSection) -> Unit,
) {
    val style = CheckpointStyle.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(style.surfaceRaised)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = style.borderStrong)
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 5.dp)) {
            AppSection.entries.forEach { section ->
                val icon = when (section) {
                    AppSection.TRACKING -> Icons.Default.TrackChanges
                    AppSection.DEALS -> Icons.Default.LocalOffer
                    AppSection.COMPARE -> Icons.Default.CompareArrows
                    AppSection.LIBRARY -> Icons.Default.LibraryBooks
                    AppSection.SYSTEM -> Icons.Default.Settings
                }
                val isSelected = selected == section
                Column(
                    Modifier
                        .weight(1f)
                        .clickable { onSelect(section) }
                        .background(if (isSelected) style.accentSoft else Color.Transparent, checkpointShape())
                        .padding(horizontal = 2.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Box {
                        Icon(
                            icon,
                            contentDescription = section.label,
                            modifier = Modifier.size(19.dp),
                            tint = if (isSelected) style.accentSecondary else style.muted,
                        )
                        if (section == AppSection.DEALS && unreadEvents > 0) {
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .size(6.dp)
                                    .background(style.accentSecondary),
                            )
                        }
                    }
                    Text(
                        section.navigationLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) style.ink else style.muted,
                        maxLines = 1,
                    )
                    Box(
                        Modifier
                            .width(22.dp)
                            .height(2.dp)
                            .background(if (isSelected) style.accentSecondary else Color.Transparent),
                    )
                }
            }
        }
    }
}

private fun AppSection.navigationLabel(): String = when (this) {
    AppSection.TRACKING -> "SEGUIR"
    AppSection.DEALS -> "OFERTAS"
    AppSection.COMPARE -> "COMPARAR"
    AppSection.LIBRARY -> "BIBLIO"
    AppSection.SYSTEM -> "SISTEMA"
}
@Composable
private fun TrackingScreen(
    games: List<GameEntity>,
    historyByGame: Map<String, List<PricePointEntity>>,
    query: String,
    storeFilter: Store?,
    activeSubscriptions: Set<SubscriptionService>,
    busy: Boolean,
    onQuery: (String) -> Unit,
    onStore: (Store?) -> Unit,
    onOpen: (String) -> Unit,
    onRefresh: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val filtered = games.filter { game ->
        (query.isBlank() || game.title.contains(query, ignoreCase = true)) &&
            (storeFilter == null || game.storeId == storeFilter.id)
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp, 14.dp, 18.dp, 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DashboardHero(
                games = games,
                historyByGame = historyByGame,
                activeSubscriptions = activeSubscriptions,
                onOpen = onOpen,
                onAdd = onAdd,
            )
        }
        item {
            SearchAndStoreFilters(
                query = query,
                storeFilter = storeFilter,
                onQuery = onQuery,
                onStore = onStore,
            )
        }
        item {
            SectionTitle(
                title = "TU RADAR",
                subtitle = "${filtered.size} de ${games.size} productos visibles",
            )
        }
        if (filtered.isEmpty()) {
            item { EmptyState(games.isNotEmpty(), onAdd) }
        } else {
            items(filtered, key = { it.id }) { game ->
                GameCard(
                    game = game,
                    history = historyByGame[game.id].orEmpty(),
                    activeSubscriptions = activeSubscriptions,
                    busy = busy,
                    onOpen = { onOpen(game.id) },
                    onRefresh = { onRefresh(game.id) },
                )
            }
        }
    }
}

@Composable
private fun DashboardHero(
    games: List<GameEntity>,
    historyByGame: Map<String, List<PricePointEntity>>,
    activeSubscriptions: Set<SubscriptionService>,
    onOpen: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val style = CheckpointStyle.current
    val featured = remember(games, historyByGame) {
        games.maxByOrNull { game ->
            val analytics = DealAnalytics.calculate(game, historyByGame[game.id].orEmpty())
            analytics.dealScore + game.discountPercent / 12.0
        }
    }
    val deals = games.count { it.discountPercent > 0 }
    val targets = games.count { it.targetPriceCents != null && it.priceCents <= it.targetPriceCents }
    val included = games.count { SubscriptionService.fromCsv(it.subscriptionTags).any(activeSubscriptions::contains) }

    CheckpointPanel(Modifier.fillMaxWidth(), highlighted = true, padding = 0.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TechnicalLabel("PRICE WATCH / LIVE", color = style.accentSecondary)
            TechnicalLabel("${games.size.toString().padStart(2, '0')} TRACKED", color = style.ink)
        }
        HorizontalDivider(color = style.hairline)
        if (featured == null) {
            Column(
                Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TechnicalLabel("RADAR VACÍO", color = style.accentSecondary)
                Text("Empieza tu lista de seguimiento", style = MaterialTheme.typography.headlineMedium)
                Text("Agrega un enlace de Nintendo, Steam o Xbox. Checkpoint hará la vigilancia tediosa por ti.", color = style.muted)
                Button(onClick = onAdd, shape = checkpointShape()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("AGREGAR PRIMER JUEGO")
                }
            }
        } else {
            val analytics = DealAnalytics.calculate(featured, historyByGame[featured.id].orEmpty())
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(featured.id) }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GameCover(featured, Modifier.width(78.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    TechnicalLabel("MEJOR OPORTUNIDAD AHORA", color = style.accentSecondary)
                    Text(featured.title, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(formatPrice(featured.priceCents, featured.currency), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        if (featured.discountPercent > 0) {
                            Spacer(Modifier.width(8.dp))
                            TechnicalLabel("-${featured.discountPercent}%", color = storeColor(Store.fromId(featured.storeId)))
                        }
                    }
                    TechnicalLabel("${analytics.dealLabel} · score ${String.format(Locale.US, "%.1f", analytics.dealScore)}", color = style.muted)
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Abrir", tint = style.muted)
            }
        }
        HorizontalDivider(color = style.hairline)
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 5.dp)) {
            DashboardMetric("OFERTAS", deals.toString(), Modifier.weight(1f))
            DashboardMetric("OBJETIVOS", targets.toString(), Modifier.weight(1f))
            DashboardMetric("INCLUIDOS", included.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun DashboardMetric(label: String, value: String, modifier: Modifier = Modifier) {
    val style = CheckpointStyle.current
    Column(modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
        TechnicalLabel(label, color = style.muted)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = style.ink)
    }
}

@Composable
private fun SearchAndStoreFilters(
    query: String,
    storeFilter: Store?,
    onQuery: (String) -> Unit,
    onStore: (Store?) -> Unit,
) {
    CheckpointPanel(Modifier.fillMaxWidth(), padding = 12.dp) {
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("Buscar en seguimiento") },
            shape = checkpointShape(),
            singleLine = true,
        )
        Spacer(Modifier.height(9.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FilterChip("Todas", storeFilter == null) { onStore(null) }
            Store.entries.forEach { store ->
                FilterChip(store.displayName, storeFilter == store, storeColor(store)) { onStore(store) }
            }
        }
    }
}
@Composable
private fun DealsScreen(
    games: List<GameEntity>,
    events: List<GameEventEntity>,
    historyByGame: Map<String, List<PricePointEntity>>,
    onOpen: (String) -> Unit,
) {
    val deals = games
        .filter { it.discountPercent > 0 || it.priceCents <= it.minPriceCents }
        .sortedByDescending { DealAnalytics.calculate(it, historyByGame[it.id].orEmpty()).dealScore }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp, 14.dp, 18.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionTitle("DEAL FEED", "Bajadas, mínimos, objetivos y ofertas que regresan") }
        if (events.isEmpty()) {
            item { InfoPanel("Aún no hay eventos", "El feed se llenará cuando Checkpoint detecte cambios reales de precio.") }
        } else {
            items(events.take(30), key = { it.id }) { event -> EventCard(event) { onOpen(event.gameId) } }
        }
        item { SectionTitle("MEJORES OFERTAS ACTIVAS", "Ordenadas mediante Deal Score") }
        if (deals.isEmpty()) item { InfoPanel("Sin ofertas activas", "La paciencia conserva dinero, aunque destruya la emoción de comprar basura a medianoche.") }
        else items(deals, key = { "deal-${it.id}" }) { game ->
            CompactDealCard(game, historyByGame[game.id].orEmpty()) { onOpen(game.id) }
        }
    }
}

@Composable
private fun CompareScreen(
    games: List<GameEntity>,
    historyByGame: Map<String, List<PricePointEntity>>,
    onOpen: (String) -> Unit,
) {
    val groups = games
        .filter { it.comparisonKey.isNotBlank() }
        .groupBy { "${it.comparisonKey}|${it.editionLabel}|${it.currency}" }
        .values
        .filter { group -> group.map { it.storeId }.distinct().size > 1 }
        .sortedBy { it.first().title }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp, 14.dp, 18.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { SectionTitle("COMPARADOR MULTITIENDA", "Mismo juego y misma edición; menos comparaciones tramposas") }
        if (groups.isEmpty()) {
            item { InfoPanel("No hay coincidencias todavía", "Agrega el mismo juego desde dos tiendas. Checkpoint normaliza el título y puedes corregir la clave desde su ficha.") }
        }
        items(groups, key = { group -> group.joinToString("-") { it.id } }) { group ->
            ComparisonCard(group, historyByGame, onOpen)
        }
    }
}

@Composable
private fun LibraryScreen(
    games: List<GameEntity>,
    historyByGame: Map<String, List<PricePointEntity>>,
    filter: LibraryStatus?,
    budgetCents: Long,
    onFilter: (LibraryStatus?) -> Unit,
    onOpen: (String) -> Unit,
    onBudget: (String) -> Unit,
) {
    val now = Calendar.getInstance()
    val budgetCurrency = games
        .groupingBy { it.currency }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
        ?: "MXN"
    val budgetGames = games.filter { it.currency == budgetCurrency }
    val spent = budgetGames.filter { game ->
        game.paidPriceCents != null && game.purchaseDate?.let { timestamp ->
            Calendar.getInstance().apply { timeInMillis = timestamp }.let { date ->
                date.get(Calendar.YEAR) == now.get(Calendar.YEAR) && date.get(Calendar.MONTH) == now.get(Calendar.MONTH)
            }
        } == true
    }.sumOf { it.paidPriceCents ?: 0L }
    val remaining = (budgetCents - spent).coerceAtLeast(0L)
    val suggestion = BudgetPlanner.suggest(budgetGames, historyByGame, remaining)
    val filtered = games.filter { filter == null || it.libraryStatus == filter.id }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp, 14.dp, 18.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            BudgetPanel(
                budget = budgetCents,
                spent = spent,
                remaining = remaining,
                currency = budgetCurrency,
                onBudget = onBudget,
            )
        }
        if (suggestion.games.isNotEmpty()) {
            item {
                CheckpointPanel(Modifier.fillMaxWidth(), highlighted = true, padding = 14.dp) {
                    TechnicalLabel("Mejor combinación dentro del presupuesto", color = CheckpointStyle.current.accentSecondary)
                    Spacer(Modifier.height(5.dp))
                    suggestion.games.forEach { game ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(game.title, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.width(8.dp))
                            Text(formatPrice(game.priceCents, game.currency), fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = CheckpointStyle.current.hairline)
                    Text("Total: ${formatPrice(suggestion.totalCents, suggestion.games.first().currency)} · Ahorro: ${formatPrice(suggestion.totalSavingsCents, suggestion.games.first().currency)}")
                }
            }
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                FilterChip("Todos", filter == null) { onFilter(null) }
                LibraryStatus.entries.forEach { status -> FilterChip(status.displayName, filter == status) { onFilter(status) } }
            }
        }
        items(filtered, key = { "library-${it.id}" }) { game -> LibraryCard(game) { onOpen(game.id) } }
    }
}

@Composable
private fun SystemScreen(
    games: List<GameEntity>,
    runs: List<SyncRunEntity>,
    interval: Long,
    notifications: Boolean,
    themeMode: CheckpointThemeMode,
    activeSubscriptions: Set<SubscriptionService>,
    onInterval: (Long) -> Unit,
    onNotifications: (Boolean) -> Unit,
    onTheme: (CheckpointThemeMode) -> Unit,
    onToggleSubscription: (SubscriptionService, Boolean) -> Unit,
    onImportBackup: () -> Unit,
    onExportBackup: () -> Unit,
    onBulkImport: () -> Unit,
) {
    val style = CheckpointStyle.current
    val sourceGroups = games.groupBy { Store.fromId(it.storeId) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp, 14.dp, 18.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SectionTitle("APARIENCIA", "La interfaz cambia; tu backlog permanece aterrador") }
        item { ThemeGrid(themeMode, onTheme) }
        item { SectionTitle("AUTOMATIZACIÓN", "Revisiones y notificaciones") }
        item {
            SettingRow("Notificaciones inteligentes", "Mínimos, objetivos, descuentos y ofertas") {
                Switch(checked = notifications, onCheckedChange = onNotifications)
            }
        }
        item {
            CheckpointPanel(Modifier.fillMaxWidth(), padding = 12.dp) {
                TechnicalLabel("Intervalo de revisión", color = style.ink)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1L, 6L, 12L, 24L, 48L).forEach { value ->
                        FilterChip("${value}H", interval == value) { onInterval(value) }
                    }
                }
            }
        }
        item { SectionTitle("MIS SUSCRIPCIONES", "Sirve para destacar juegos incluidos en servicios que ya pagas") }
        items(SubscriptionService.entries, key = { it.id }) { service ->
            SettingRow(service.displayName, "Marca únicamente las que tienes activas") {
                Switch(
                    checked = service in activeSubscriptions,
                    onCheckedChange = { onToggleSubscription(service, it) },
                )
            }
        }
        item { SectionTitle("ESTADO DE FUENTES", "Diagnóstico de Nintendo, Steam y Xbox") }
        items(Store.entries, key = { it.id }) { store ->
            val storeGames = sourceGroups[store].orEmpty()
            val errors = storeGames.count { it.lastStatus == "error" }
            SourceStatusCard(store, storeGames.size, errors, storeGames.maxOfOrNull { it.lastChecked })
        }
        item {
            val latest = runs.firstOrNull()
            InfoPanel(
                "Última sincronización global",
                latest?.let { "${it.updated} actualizados · ${it.errors} errores · ${formatDate(it.finishedAt)}" }
                    ?: "Todavía no existe un registro global.",
            )
        }
        item { SectionTitle("DATOS", "Importación masiva y respaldos locales") }
        item {
            CheckpointPanel(Modifier.fillMaxWidth(), padding = 12.dp) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onBulkImport, shape = checkpointShape(), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(Modifier.width(5.dp))
                        Text("WISHLIST")
                    }
                    OutlinedButton(onClick = onImportBackup, shape = checkpointShape(), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(5.dp))
                        Text("IMPORTAR")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onExportBackup, shape = checkpointShape(), modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(Modifier.width(5.dp))
                    Text("EXPORTAR RESPALDO")
                }
            }
        }
        item {
            InfoPanel(
                "Widget Android",
                "Mantén pulsada la pantalla de inicio → Widgets → Checkpoint. Muestra hasta tres ofertas u objetivos destacados.",
                Icons.Default.Widgets,
            )
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier) {
    val style = CheckpointStyle.current
    Column(modifier.padding(horizontal = 8.dp, vertical = 9.dp)) {
        TechnicalLabel(label, color = style.muted)
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GameCard(
    game: GameEntity,
    history: List<PricePointEntity>,
    activeSubscriptions: Set<SubscriptionService>,
    busy: Boolean,
    onOpen: () -> Unit,
    onRefresh: () -> Unit,
) {
    val style = CheckpointStyle.current
    val store = Store.fromId(game.storeId)
    val analytics = remember(game, history) { DealAnalytics.calculate(game, history) }
    val included = SubscriptionService.fromCsv(game.subscriptionTags).firstOrNull(activeSubscriptions::contains)
    val statusText = when {
        game.targetPriceCents != null && game.priceCents <= game.targetPriceCents -> "OBJETIVO ALCANZADO"
        included != null -> "INCLUIDO EN ${included.displayName}"
        game.targetPriceCents != null -> "OBJETIVO ${formatPrice(game.targetPriceCents, game.currency)}"
        else -> null
    }
    val statusColor = when {
        game.targetPriceCents != null && game.priceCents <= game.targetPriceCents -> style.success
        included != null -> style.accentSecondary
        else -> style.muted
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(checkpointShape())
            .background(style.surfaceRaised)
            .border(
                1.dp,
                if (game.lastStatus == "error") MaterialTheme.colorScheme.error else style.hairline,
                checkpointShape(),
            )
            .clickable(onClick = onOpen)
            .padding(11.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        GameCover(game, Modifier.width(70.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StoreBadge(store)
                Spacer(Modifier.weight(1f))
                TechnicalLabel(formatRelative(game.lastChecked), color = style.muted)
                IconButton(onClick = onRefresh, enabled = !busy, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar", modifier = Modifier.size(17.dp), tint = style.muted)
                }
            }
            Text(game.title, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(formatPrice(game.priceCents, game.currency), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                if (game.discountPercent > 0) {
                    Spacer(Modifier.width(8.dp))
                    TechnicalLabel("-${game.discountPercent}%", color = storeColor(store))
                }
            }
            TechnicalLabel(
                "${analytics.dealLabel} · mínimo ${formatPrice(game.minPriceCents, game.currency)} · ${String.format(Locale.US, "%.1f", analytics.dealScore)}/10",
                color = style.muted,
            )
            if (statusText != null) StatusBadge(statusText, statusColor)
        }
    }
}
@Composable
private fun GameCover(game: GameEntity, modifier: Modifier = Modifier) {
    val style = CheckpointStyle.current
    val matrix = remember(style.coverSaturation) { ColorMatrix().apply { setToSaturation(style.coverSaturation) } }
    Box(modifier.aspectRatio(16f / 20f).background(style.surfaceAlt).border(1.dp, style.hairline)) {
        AsyncImage(
            model = game.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(matrix),
        )
    }
}

@Composable
private fun ScoreBadge(score: Double) {
    val style = CheckpointStyle.current
    Row(
        Modifier
            .background(style.accentSoft, checkpointShape())
            .border(1.dp, style.hairline, checkpointShape())
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(5.dp).background(style.accentSecondary))
        Spacer(Modifier.width(5.dp))
        TechnicalLabel("DEAL ${String.format(Locale.US, "%.1f", score)}/10", color = style.ink)
    }
}
@Composable
private fun StatusBadge(text: String, color: Color) {
    val style = CheckpointStyle.current
    Row(
        Modifier
            .background(style.surfaceAlt, checkpointShape())
            .border(1.dp, style.hairline, checkpointShape())
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(5.dp).background(color))
        Spacer(Modifier.width(6.dp))
        TechnicalLabel(text, color = style.ink)
    }
}
@Composable
private fun FilterChip(label: String, selected: Boolean, accent: Color? = null, onClick: () -> Unit) {
    val style = CheckpointStyle.current
    val marker = accent ?: style.accentSecondary
    Row(
        Modifier
            .background(if (selected) style.accentSoft else style.surfaceAlt, checkpointShape())
            .border(1.dp, if (selected) style.borderStrong else style.hairline, checkpointShape())
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected || accent != null) {
            Box(Modifier.size(5.dp).background(marker))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            label.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.labelMedium,
            color = style.ink,
            maxLines = 1,
        )
    }
}
@Composable
private fun EventCard(event: GameEventEntity, onOpen: () -> Unit) {
    val style = CheckpointStyle.current
    val type = AlertEventType.fromId(event.type)
    val color = when (type) {
        AlertEventType.NEW_LOW, AlertEventType.TARGET_REACHED -> style.success
        AlertEventType.SOURCE_ERROR -> MaterialTheme.colorScheme.error
        else -> style.accentSecondary
    }
    CheckpointPanel(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        highlighted = !event.seen,
        padding = 12.dp,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(color))
                Spacer(Modifier.width(6.dp))
                TechnicalLabel(type.displayName, color = style.ink)
            }
            TechnicalLabel(formatRelative(event.createdAt), color = style.muted)
        }
        Spacer(Modifier.height(5.dp))
        Text(event.title, style = MaterialTheme.typography.titleLarge)
        Text(event.detail, color = style.muted)
    }
}
@Composable
private fun CompactDealCard(game: GameEntity, history: List<PricePointEntity>, onOpen: () -> Unit) {
    val style = CheckpointStyle.current
    val analytics = DealAnalytics.calculate(game, history)
    CheckpointPanel(Modifier.fillMaxWidth().clickable(onClick = onOpen), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(game.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                TechnicalLabel("${analytics.dealLabel} · ahorras ${formatPrice(analytics.savingsCents, game.currency)}", color = style.muted)
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(formatPrice(game.priceCents, game.currency), fontWeight = FontWeight.Black, color = style.ink)
                Spacer(Modifier.height(4.dp))
                ScoreBadge(analytics.dealScore)
            }
        }
    }
}
@Composable
private fun ComparisonCard(
    group: List<GameEntity>,
    historyByGame: Map<String, List<PricePointEntity>>,
    onOpen: (String) -> Unit,
) {
    val style = CheckpointStyle.current
    val sorted = group.sortedBy { it.priceCents }
    val best = sorted.first()
    CheckpointPanel(Modifier.fillMaxWidth(), highlighted = true, padding = 14.dp) {
        TechnicalLabel("${ProductEdition.fromId(best.editionLabel).displayName} · ${best.comparisonKey}", color = style.muted)
        Spacer(Modifier.height(4.dp))
        Text(best.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        sorted.forEachIndexed { index, game ->
            val analytics = DealAnalytics.calculate(game, historyByGame[game.id].orEmpty())
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (index == 0) style.accentSoft else Color.Transparent, checkpointShape())
                    .clickable { onOpen(game.id) }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StoreBadge(Store.fromId(game.storeId))
                Spacer(Modifier.width(8.dp))
                Text(formatPrice(game.priceCents, game.currency), modifier = Modifier.weight(1f), fontWeight = FontWeight.Black)
                TechnicalLabel(
                    if (index == 0) "MEJOR PRECIO" else "SCORE ${String.format(Locale.US, "%.1f", analytics.dealScore)}",
                    color = if (index == 0) style.accentSecondary else style.muted,
                )
            }
        }
        if (sorted.size > 1) {
            Spacer(Modifier.height(5.dp))
            val difference = sorted.last().priceCents - best.priceCents
            TechnicalLabel("Ahorro frente al más caro: ${formatPrice(difference, best.currency)}", color = style.accentSecondary)
        }
    }
}
@Composable
private fun LibraryCard(game: GameEntity, onOpen: () -> Unit) {
    val style = CheckpointStyle.current
    val status = LibraryStatus.fromId(game.libraryStatus)
    CheckpointPanel(Modifier.fillMaxWidth().clickable(onClick = onOpen), padding = 11.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GameCover(game, Modifier.width(54.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                TechnicalLabel(status.displayName, color = style.accentSecondary)
                Text(game.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val detail = buildList {
                    add(GameFormat.fromId(game.gameFormat).displayName)
                    game.paidPriceCents?.let { add("Pagaste ${formatPrice(it, game.currency)}") }
                    game.personalRating?.let { add("$it/10") }
                }.joinToString(" · ")
                if (detail.isNotBlank()) TechnicalLabel(detail, color = style.muted)
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = style.muted)
        }
    }
}
@Composable
private fun BudgetPanel(
    budget: Long,
    spent: Long,
    remaining: Long,
    currency: String,
    onBudget: (String) -> Unit,
) {
    val style = CheckpointStyle.current
    var editing by remember { mutableStateOf(false) }
    var text by remember(budget) { mutableStateOf(if (budget > 0) String.format(Locale.US, "%.2f", budget / 100.0) else "") }
    CheckpointPanel(Modifier.fillMaxWidth(), highlighted = true, padding = 14.dp) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                TechnicalLabel("PRESUPUESTO MENSUAL", color = style.accentSecondary)
                Text("Control de compras", style = MaterialTheme.typography.titleLarge)
            }
            IconButton(onClick = { editing = !editing }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = style.muted) }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Metric("LÍMITE", formatPrice(budget, currency), Modifier.weight(1f))
            Metric("GASTADO", formatPrice(spent, currency), Modifier.weight(1f))
            Metric("RESTANTE", formatPrice(remaining, currency), Modifier.weight(1f))
        }
        if (editing) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Presupuesto") }, modifier = Modifier.weight(1f), singleLine = true, shape = checkpointShape())
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onBudget(text); editing = false }, shape = checkpointShape()) { Text("GUARDAR") }
            }
        }
    }
}
@Composable
private fun SourceStatusCard(store: Store, total: Int, errors: Int, lastChecked: Long?) {
    val style = CheckpointStyle.current
    val color = when {
        total == 0 -> style.muted
        errors == 0 -> style.success
        errors < total -> style.warning
        else -> MaterialTheme.colorScheme.error
    }
    CheckpointPanel(Modifier.fillMaxWidth(), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StoreBadge(store)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(if (total == 0) "Sin productos" else "$total productos · $errors errores")
                TechnicalLabel(lastChecked?.let(::formatRelative) ?: "sin revisión", color = style.muted)
            }
            Icon(if (errors == 0) Icons.Default.Check else Icons.Default.ErrorOutline, contentDescription = null, tint = color)
        }
    }
}
@Composable
private fun SettingRow(title: String, subtitle: String, action: @Composable () -> Unit) {
    val style = CheckpointStyle.current
    CheckpointPanel(Modifier.fillMaxWidth(), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = style.muted, style = MaterialTheme.typography.bodyMedium)
            }
            action()
        }
    }
}
@Composable
private fun ThemeOption(mode: CheckpointThemeMode, selected: Boolean, onClick: () -> Unit) {
    val style = CheckpointStyle.current
    val preview = checkpointVisualsFor(mode)
    Column(
        Modifier
            .fillMaxWidth()
            .background(style.surfaceRaised, checkpointShape())
            .border(1.dp, if (selected) style.accentSecondary else style.hairline, checkpointShape())
            .clickable(onClick = onClick)
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(preview.background, preview.surface, preview.accent, preview.accentSecondary).forEach { color ->
                Box(Modifier.weight(1f).height(18.dp).background(color).border(1.dp, preview.hairline))
            }
        }
        TechnicalLabel(if (selected) "● ACTIVO" else "○ TEMA", color = if (selected) style.accentSecondary else style.muted)
        Text(mode.displayName, style = MaterialTheme.typography.titleMedium)
        Text(mode.description, color = style.muted, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ThemeGrid(
    selected: CheckpointThemeMode,
    onTheme: (CheckpointThemeMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CheckpointThemeMode.entries.chunked(2).forEach { rowModes ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowModes.forEach { mode ->
                    Box(Modifier.weight(1f)) {
                        ThemeOption(mode, selected == mode) { onTheme(mode) }
                    }
                }
                if (rowModes.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
@Composable
private fun SectionTitle(title: String, subtitle: String) {
    val style = CheckpointStyle.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                TechnicalLabel(subtitle, color = style.muted)
            }
            TechnicalLabel("CP // ${title.hashCode().toUInt().toString(16).takeLast(4)}", color = style.accentSecondary)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(style.borderStrong))
    }
}
@Composable
private fun InfoPanel(title: String, body: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    val style = CheckpointStyle.current
    CheckpointPanel(Modifier.fillMaxWidth(), padding = 13.dp) {
        Row {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = style.accentSecondary)
                Spacer(Modifier.width(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(2.dp))
                Text(body, color = style.muted)
            }
        }
    }
}
@Composable
private fun EmptyState(hasAny: Boolean, onAdd: () -> Unit) {
    val title = if (hasAny) "No hay coincidencias" else "Tu radar está vacío"
    val body = if (hasAny) "Cambia los filtros o la búsqueda." else "Agrega un producto de Nintendo, Steam o Xbox para comenzar."
    Column(Modifier.fillMaxWidth().padding(vertical = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(46.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(body, color = CheckpointStyle.current.muted)
        if (!hasAny) {
            Spacer(Modifier.height(12.dp))
            Button(onClick = onAdd, shape = checkpointShape()) { Text("AGREGAR JUEGO") }
        }
    }
}

@Composable
private fun AddGameDialog(initialUrl: String, busy: Boolean, onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var url by remember(initialUrl) { mutableStateOf(initialUrl) }
    var target by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = checkpointShape(),
        title = { Text("NUEVO SEGUIMIENTO") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Enlace de Nintendo, Steam o Xbox") }, shape = checkpointShape(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("Precio objetivo opcional") }, shape = checkpointShape(), modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = { Button(onClick = { onAdd(url, target) }, enabled = url.isNotBlank() && !busy, shape = checkpointShape()) { Text("AGREGAR") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } },
    )
}

@Composable
private fun BulkImportDialog(busy: Boolean, onDismiss: () -> Unit, onImport: (String) -> Unit) {
    var raw by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = checkpointShape(),
        title = { Text("IMPORTAR WISHLIST") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pega varios enlaces, App IDs de Steam o una wishlist pública. Checkpoint agregará lo que pueda leer sin pedirte cuentas.")
                OutlinedTextField(
                    value = raw,
                    onValueChange = { raw = it },
                    label = { Text("Enlaces o IDs, uno por línea") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                    shape = checkpointShape(),
                )
            }
        },
        confirmButton = { Button(onClick = { onImport(raw) }, enabled = raw.isNotBlank() && !busy, shape = checkpointShape()) { Text("IMPORTAR") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } },
    )
}

@Composable
private fun GameDetailSheet(
    game: GameEntity,
    history: List<PricePointEntity>,
    activeSubscriptions: Set<SubscriptionService>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
    onSetTarget: (String) -> Unit,
    onAlerts: (Boolean, Boolean, String, String, Boolean, Boolean) -> Unit,
    onComparison: (String, ProductEdition) -> Unit,
    onLibrary: (LibraryStatus, String, Long?, GameFormat, String) -> Unit,
    onSubscriptions: (Set<SubscriptionService>) -> Unit,
    onNotes: (String) -> Unit,
    onOpenStore: () -> Unit,
    onOpenSteamDb: (() -> Unit)?,
) {
    val style = CheckpointStyle.current
    val analytics = remember(game, history) { DealAnalytics.calculate(game, history) }
    var targetText by remember(game.targetPriceCents) { mutableStateOf(game.targetPriceCents?.let { String.format(Locale.US, "%.2f", it / 100.0) }.orEmpty()) }
    var anyDrop by remember(game.alertAnyDrop) { mutableStateOf(game.alertAnyDrop) }
    var targetAlert by remember(game.alertTarget) { mutableStateOf(game.alertTarget) }
    var discountText by remember(game.alertDiscountPercent) { mutableStateOf(game.alertDiscountPercent?.toString().orEmpty()) }
    var amountText by remember(game.alertDropAmountCents) { mutableStateOf(game.alertDropAmountCents?.let { String.format(Locale.US, "%.2f", it / 100.0) }.orEmpty()) }
    var newLow by remember(game.alertNewLow) { mutableStateOf(game.alertNewLow) }
    var ending by remember(game.alertOfferEndingSoon) { mutableStateOf(game.alertOfferEndingSoon) }
    var comparisonKey by remember(game.comparisonKey) { mutableStateOf(game.comparisonKey) }
    var edition by remember(game.editionLabel) { mutableStateOf(ProductEdition.fromId(game.editionLabel)) }
    var libraryStatus by remember(game.libraryStatus) { mutableStateOf(LibraryStatus.fromId(game.libraryStatus)) }
    var paidText by remember(game.paidPriceCents) { mutableStateOf(game.paidPriceCents?.let { String.format(Locale.US, "%.2f", it / 100.0) }.orEmpty()) }
    var format by remember(game.gameFormat) { mutableStateOf(GameFormat.fromId(game.gameFormat)) }
    var ratingText by remember(game.personalRating) { mutableStateOf(game.personalRating?.toString().orEmpty()) }
    var subscriptions by remember(game.subscriptionTags) { mutableStateOf(SubscriptionService.fromCsv(game.subscriptionTags)) }
    var notes by remember(game.notes) { mutableStateOf(game.notes) }
    var confirmDelete by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = style.surface, shape = checkpointShape(), dragHandle = null) {
        LazyColumn(
            Modifier.fillMaxWidth().navigationBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        StoreBadge(Store.fromId(game.storeId))
                        Spacer(Modifier.height(8.dp))
                        Text(game.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                        TechnicalLabel("${game.productId ?: game.id} · ${game.region} · ${game.currency}", color = style.muted)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar") }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().border(1.dp, style.ink)) {
                    DetailMetric("CURRENT", formatPrice(game.priceCents, game.currency), Modifier.weight(1f))
                    DetailMetric("LOW", formatPrice(game.minPriceCents, game.currency), Modifier.weight(1f))
                    DetailMetric("SCORE", String.format(Locale.US, "%.1f", analytics.dealScore), Modifier.weight(1f))
                }
            }
            item {
                InfoPanel(analytics.dealLabel, analytics.forecastDetail, Icons.Default.Assessment)
            }
            item { SectionRule("Historial · ${history.size} registros") }
            item { PriceChart(history) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailMetric("PROMEDIO", formatPrice(analytics.averagePriceCents, game.currency), Modifier.weight(1f))
                    DetailMetric("MÁXIMO", formatPrice(analytics.maximumPriceCents, game.currency), Modifier.weight(1f))
                    DetailMetric("OFERTAS", analytics.offerCount.toString(), Modifier.weight(1f))
                }
            }
            item { SectionRule("Precio objetivo") }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = targetText, onValueChange = { targetText = it }, label = { Text("Objetivo") }, modifier = Modifier.weight(1f), shape = checkpointShape(), singleLine = true)
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onSetTarget(targetText) }, shape = checkpointShape()) { Text("GUARDAR") }
                }
            }
            item {
                ExpandableEditorSection(
                    title = "ALERTAS INTELIGENTES",
                    summary = "Bajadas, objetivos, mínimos y fin de oferta",
                ) {
                    ToggleLine("Cualquier bajada", anyDrop) { anyDrop = it }
                    ToggleLine("Objetivo alcanzado", targetAlert) { targetAlert = it }
                    ToggleLine("Nuevo mínimo", newLow) { newLow = it }
                    ToggleLine("Oferta termina pronto", ending) { ending = it }
                    OutlinedTextField(value = discountText, onValueChange = { discountText = it }, label = { Text("Descuento mínimo %") }, modifier = Modifier.fillMaxWidth(), shape = checkpointShape(), singleLine = true)
                    OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Bajada mínima en dinero") }, modifier = Modifier.fillMaxWidth(), shape = checkpointShape(), singleLine = true)
                    Button(onClick = { onAlerts(anyDrop, targetAlert, discountText, amountText, newLow, ending) }, shape = checkpointShape(), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("GUARDAR REGLAS")
                    }
                }
            }
            item {
                ExpandableEditorSection(
                    title = "COMPARACIÓN MULTITIENDA",
                    summary = "${edition.displayName} · clave editable",
                ) {
                    OutlinedTextField(value = comparisonKey, onValueChange = { comparisonKey = it }, label = { Text("Clave común") }, modifier = Modifier.fillMaxWidth(), shape = checkpointShape())
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProductEdition.entries.forEach { item -> FilterChip(item.displayName, edition == item) { edition = item } }
                    }
                    OutlinedButton(onClick = { onComparison(comparisonKey, edition) }, shape = checkpointShape(), modifier = Modifier.fillMaxWidth()) { Text("GUARDAR COMPARACIÓN") }
                }
            }
            item {
                ExpandableEditorSection(
                    title = "BIBLIOTECA PERSONAL",
                    summary = libraryStatus.displayName,
                ) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        LibraryStatus.entries.forEach { item -> FilterChip(item.displayName, libraryStatus == item) { libraryStatus = item } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = paidText, onValueChange = { paidText = it }, label = { Text("Precio pagado") }, modifier = Modifier.weight(1f), shape = checkpointShape(), singleLine = true)
                        OutlinedTextField(value = ratingText, onValueChange = { ratingText = it }, label = { Text("Nota 1-10") }, modifier = Modifier.weight(1f), shape = checkpointShape(), singleLine = true)
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        GameFormat.entries.forEach { item -> FilterChip(item.displayName, format == item) { format = item } }
                    }
                    Button(
                        onClick = {
                            val date = if (libraryStatus == LibraryStatus.WISHLIST) null else game.purchaseDate ?: System.currentTimeMillis()
                            onLibrary(libraryStatus, paidText, date, format, ratingText)
                        },
                        shape = checkpointShape(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.LibraryBooks, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("GUARDAR EN BIBLIOTECA")
                    }
                }
            }
            item {
                ExpandableEditorSection(
                    title = "SUSCRIPCIONES",
                    summary = if (subscriptions.isEmpty()) "Sin servicios vinculados" else "${subscriptions.size} servicios vinculados",
                ) {
                    SubscriptionService.entries.forEach { service ->
                        val selected = service in subscriptions
                        ToggleLine(service.displayName + if (service in activeSubscriptions) " · LA TIENES" else "", selected) { enabled ->
                            subscriptions = subscriptions.toMutableSet().apply { if (enabled) add(service) else remove(service) }.toSet()
                        }
                    }
                    OutlinedButton(onClick = { onSubscriptions(subscriptions) }, modifier = Modifier.fillMaxWidth(), shape = checkpointShape()) { Text("GUARDAR SUSCRIPCIONES") }
                }
            }
            item {
                ExpandableEditorSection(
                    title = "NOTAS",
                    summary = if (notes.isBlank()) "Sin notas personales" else "Nota guardada",
                ) {
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas personales") }, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), shape = checkpointShape())
                    OutlinedButton(onClick = { onNotes(notes) }, modifier = Modifier.fillMaxWidth(), shape = checkpointShape()) { Text("GUARDAR NOTAS") }
                }
            }
            item {
                val statusText = if (game.lastStatus == "error") game.lastError ?: "Error desconocido" else "Última revisión ${formatDate(game.lastChecked)} · ${game.source}"
                InfoPanel(if (game.lastStatus == "error") "Error de fuente" else "Fuente operativa", statusText, if (game.lastStatus == "error") Icons.Default.ErrorOutline else Icons.Default.Speed)
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRefresh, enabled = !busy, shape = checkpointShape(), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(5.dp))
                        Text("ACTUALIZAR")
                    }
                    OutlinedButton(onClick = onOpenStore, shape = checkpointShape(), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                        Spacer(Modifier.width(5.dp))
                        Text("TIENDA")
                    }
                }
            }
            if (onOpenSteamDb != null) {
                item { TextButton(onClick = onOpenSteamDb) { Icon(Icons.Default.OpenInNew, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("ABRIR STEAMDB") } }
            }
            item {
                TextButton(onClick = { confirmDelete = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("ELIMINAR DEL SEGUIMIENTO")
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            shape = checkpointShape(),
            title = { Text("¿ELIMINAR JUEGO?") },
            text = { Text("Se borrarán su historial, eventos y metadatos locales.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete(); onDismiss() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("ELIMINAR") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("CANCELAR") } },
        )
    }
}

@Composable
private fun ExpandableEditorSection(
    title: String,
    summary: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    val style = CheckpointStyle.current
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }
    CheckpointPanel(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        highlighted = expanded,
        padding = 12.dp,
    ) {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                TechnicalLabel(title, color = if (expanded) style.accentSecondary else style.ink)
                Text(summary, color = style.muted, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = if (expanded) "Contraer" else "Expandir",
                modifier = Modifier.rotate(if (expanded) 180f else 0f),
                tint = style.muted,
            )
        }
        AnimatedVisibility(expanded) {
            Column(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ToggleLine(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun DetailMetric(label: String, value: String, modifier: Modifier) {
    Column(modifier.padding(10.dp)) {
        TechnicalLabel(label, color = CheckpointStyle.current.muted)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
    }
}
