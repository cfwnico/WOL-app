package com.cfw.wol.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.cfw.wol.WakeReceiver
import com.cfw.wol.data.Device

object ShortcutHelper {
    fun createShortcut(context: Context, device: Device) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val intent = Intent(context, WakeReceiver::class.java).apply {
                action = "com.cfw.wol.ACTION_WAKE_DEVICE"
                putExtra("EXTRA_MAC", device.mac)
                putExtra("EXTRA_PORT", device.port)
                putExtra("EXTRA_NAME", device.name)
            }

            // Using standard Android system icon for simplicity in this generated project
            val icon = IconCompat.createWithResource(context, android.R.drawable.ic_lock_power_off)

            val shortcutInfo = ShortcutInfoCompat.Builder(context, "shortcut_${device.id}")
                .setShortLabel("唤醒: ${device.name}")
                .setLongLabel("局域网唤醒 ${device.name}")
                .setIcon(icon)
                .setIntent(intent)
                .build()

            ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
        }
    }
}
