package com.cfw.wol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.cfw.wol.util.WolSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WakeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.cfw.wol.ACTION_WAKE_DEVICE") {
            val mac = intent.getStringExtra("EXTRA_MAC") ?: return
            val port = intent.getIntExtra("EXTRA_PORT", 9)
            val name = intent.getStringExtra("EXTRA_NAME") ?: "设备"

            Toast.makeText(context, "唤醒设备：$name", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                WolSender.sendMagicPacket(mac, port)
            }
        }
    }
}
