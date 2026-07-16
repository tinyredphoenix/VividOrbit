package com.custom.dth.aosp_port

import android.util.Log

object SoftPreconditions {
    @JvmStatic
    fun checkState(expression: Boolean, tag: String, message: String) {
        if (!expression) {
            Log.e(tag, message)
        }
    }
}
