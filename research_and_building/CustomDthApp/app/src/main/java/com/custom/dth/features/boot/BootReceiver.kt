package com.custom.dth.features.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.custom.dth.PhaseZeroActivity
import com.custom.dth.features.settings.AppPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = AppPreferences(context)
            if (prefs.openOnBoot) {
                val launchIntent = Intent(context, PhaseZeroActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(launchIntent)
            }
        }
    }
}
