package com.example.reclaim.data

import android.content.Context
import com.example.reclaim.reclaimApplication

object BlockingServiceController {

    fun startIfNeeded(context: Context) {
        val app = context.reclaimApplication()
        if (app.addedApps.addedApps().isNotEmpty()) {
            BlockingService.start(context)
        }
    }
}
