package com.custom.dth.features.timer

import android.os.CountDownTimer
import com.custom.dth.core.AppEvent
import com.custom.dth.core.AppEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SleepTimerManager(
    private val eventBus: AppEventBus,
    private val scope: CoroutineScope
) {
    private var timer: CountDownTimer? = null

    /**
     * Starts the sleep timer for the given duration.
     * @param minutes The duration in minutes.
     */
    fun startTimer(minutes: Int) {
        timer?.cancel()
        
        val durationMillis = minutes * 60 * 1000L
        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Could emit a progress event here if UI needs it
            }

            override fun onFinish() {
                scope.launch {
                    eventBus.publish(AppEvent.StopPlaybackRequested)
                }
            }
        }.start()
    }

    fun cancelTimer() {
        timer?.cancel()
        timer = null
    }
}
