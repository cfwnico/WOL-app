package com.cfw.wol

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.cfw.wol.util.WolSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WakeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (intent.action == "com.cfw.wol.ACTION_WAKE_DEVICE") {
            val mac = intent.getStringExtra("EXTRA_MAC") ?: return
            val port = intent.getIntExtra("EXTRA_PORT", 9)
            val name = intent.getStringExtra("EXTRA_NAME") ?: "设备"

            Toast.makeText(this, "唤醒设备：$name", Toast.LENGTH_SHORT).show()

            // CoroutineScope attached to Dispatchers.IO will survive the Activity finishing
            CoroutineScope(Dispatchers.IO).launch {
                WolSender.sendMagicPacket(mac, port)
            }
        }
        
        // Immediately finish to prevent any UI from flashing or staying on screen
        finish()
    }
}
