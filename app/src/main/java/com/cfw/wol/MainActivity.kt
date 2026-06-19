package com.cfw.wol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cfw.wol.data.DeviceManager
import com.cfw.wol.ui.MainScreen
import com.cfw.wol.ui.theme.WOLTheme

class MainActivity : ComponentActivity() {
    private lateinit var deviceManager: DeviceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceManager = DeviceManager(this)

        setContent {
            WOLTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(deviceManager = deviceManager)
                }
            }
        }
    }
}
