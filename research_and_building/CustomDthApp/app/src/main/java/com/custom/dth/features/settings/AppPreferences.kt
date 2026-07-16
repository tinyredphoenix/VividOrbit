package com.custom.dth.features.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vividorbit_prefs", Context.MODE_PRIVATE)

    var defaultChannelId: Long
        get() = prefs.getLong("default_channel_id", -1L)
        set(value) = prefs.edit { putLong("default_channel_id", value) }

    var openOnBoot: Boolean
        get() = prefs.getBoolean("open_on_boot", true)
        set(value) = prefs.edit { putBoolean("open_on_boot", value) }

    var parentalPinHash: String?
        get() = prefs.getString("parental_pin_hash", null)
        set(value) = prefs.edit { putString("parental_pin_hash", value) }

    fun exportToJson(): String {
        val map = prefs.all
        // A simple JSON conversion for backup purposes
        val builder = java.lang.StringBuilder()
        builder.append("{")
        val entries = map.entries.toList()
        for (i in entries.indices) {
            val entry = entries[i]
            val valueStr = if (entry.value is String) "\"${entry.value}\"" else entry.value.toString()
            builder.append("\"${entry.key}\": $valueStr")
            if (i < entries.size - 1) builder.append(", ")
        }
        builder.append("}")
        return builder.toString()
    }
}
