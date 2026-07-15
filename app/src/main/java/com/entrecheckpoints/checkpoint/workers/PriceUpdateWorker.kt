package com.entrecheckpoints.checkpoint.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.entrecheckpoints.checkpoint.CheckpointApplication

class PriceUpdateWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val application = applicationContext as? CheckpointApplication ?: return Result.failure()
        return runCatching { application.container.repository.refreshAll() }
            .fold(
                onSuccess = { result ->
                    if (result.errors > 0 && result.updated == 0) Result.retry() else Result.success()
                },
                onFailure = { Result.retry() },
            )
    }
}
