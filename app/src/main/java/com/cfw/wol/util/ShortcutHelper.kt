package com.cfw.wol.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.cfw.wol.WakeActivity
import com.cfw.wol.data.Device

object ShortcutHelper {
    fun createShortcut(context: Context, device: Device, customName: String) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val intent = Intent(context, WakeActivity::class.java).apply {
                action = "com.cfw.wol.ACTION_WAKE_DEVICE"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("EXTRA_MAC", device.mac)
                putExtra("EXTRA_PORT", device.port)
                putExtra("EXTRA_NAME", device.name)
            }

            // Using standard Android system icon for simplicity in this generated project
            val icon = IconCompat.createWithResource(context, android.R.drawable.ic_lock_power_off)

            val shortcutInfo = ShortcutInfoCompat.Builder(context, "shortcut_${device.id}")
                .setShortLabel(customName)
                .setLongLabel("局域网唤醒 $customName")
                .setIcon(icon)
                .setIntent(intent)
                .build()

            ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
        }
    }
}
