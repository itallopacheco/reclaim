package com.example.reclaim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.reclaim.data.BlockingServiceController
import com.example.reclaim.navigation.ReclaimNavHost
import com.example.reclaim.ui.theme.ReclaimTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        BlockingServiceController.startIfNeeded(this)
        setContent {
            ReclaimTheme {
                ReclaimNavHost()
            }
        }
    }
}
