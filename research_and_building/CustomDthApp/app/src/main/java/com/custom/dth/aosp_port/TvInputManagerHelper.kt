package com.custom.dth.aosp_port

import android.content.Context
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager

class TvInputManagerHelper(context: Context) {
    private val tvInputManager: TvInputManager = 
        context.getSystemService(Context.TV_INPUT_SERVICE) as TvInputManager

    fun getTvInputInfo(inputId: String?): TvInputInfo? {
        if (inputId == null) return null
        return tvInputManager.getTvInputInfo(inputId)
    }
}
