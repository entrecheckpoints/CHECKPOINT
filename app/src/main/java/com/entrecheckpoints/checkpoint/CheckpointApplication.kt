package com.entrecheckpoints.checkpoint

import android.app.Application
import com.entrecheckpoints.checkpoint.data.AppPreferences
import com.entrecheckpoints.checkpoint.data.CheckpointRepository
import com.entrecheckpoints.checkpoint.data.local.CheckpointDatabase
import com.entrecheckpoints.checkpoint.data.network.HttpClient
import com.entrecheckpoints.checkpoint.data.network.StoreRegistry
import com.entrecheckpoints.checkpoint.data.network.WishlistImporter
import com.entrecheckpoints.checkpoint.notifications.NotificationHelper
import com.entrecheckpoints.checkpoint.widget.CheckpointWidgetUpdater
import com.entrecheckpoints.checkpoint.workers.SyncScheduler

class CheckpointApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        SyncScheduler.schedule(this, container.preferences.intervalHours)
    }
}

class AppContainer(application: Application) {
    val preferences = AppPreferences(application)
    private val database = CheckpointDatabase.get(application)
    private val http = HttpClient()
    private val stores = StoreRegistry(http)
    private val notifications = NotificationHelper(application, preferences)
    private val widgetUpdater = CheckpointWidgetUpdater(application)
    private val wishlistImporter = WishlistImporter(http, stores)
    val repository = CheckpointRepository(
        dao = database.checkpointDao(),
        stores = stores,
        wishlistImporter = wishlistImporter,
        preferences = preferences,
        notifications = notifications,
        widgetUpdater = widgetUpdater,
    )
}
