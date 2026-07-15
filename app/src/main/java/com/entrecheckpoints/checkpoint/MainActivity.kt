package com.entrecheckpoints.checkpoint

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.entrecheckpoints.checkpoint.ui.CheckpointAppRoot
import com.entrecheckpoints.checkpoint.ui.MainViewModel
import com.entrecheckpoints.checkpoint.workers.SyncScheduler

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        val container = (application as CheckpointApplication).container
        MainViewModel.Factory(container) { hours -> SyncScheduler.schedule(this, hours) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            CheckpointAppRoot(viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            viewModel.handleSharedText(intent.getStringExtra(Intent.EXTRA_TEXT))
        }
        intent?.getStringExtra(EXTRA_GAME_ID)?.let(viewModel::selectGame)
    }

    companion object {
        const val EXTRA_GAME_ID = "checkpoint_game_id"
    }
}
