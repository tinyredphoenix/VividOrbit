package com.custom.dth.features.pin

import com.custom.dth.core.AppEvent
import com.custom.dth.core.AppEventBus
import com.custom.dth.features.settings.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.security.MessageDigest

class PinManager(
    private val appPreferences: AppPreferences,
    private val eventBus: AppEventBus,
    private val scope: CoroutineScope
) {
    init {
        scope.launch {
            eventBus.events.collect { event ->
                if (event is AppEvent.TuneRequested) {
                    handleTuneRequest(event)
                }
            }
        }
    }

    private suspend fun handleTuneRequest(request: AppEvent.TuneRequested) {
        val pinHash = appPreferences.parentalPinHash
        
        // Basic logic: if a PIN is set, assume we need to verify. 
        // In a real implementation, we would check if the specific channel is locked in ChannelRepository.
        if (pinHash == null) {
            // Unlocked, approve immediately
            eventBus.publish(AppEvent.TuneApproved(request.channelUri, request.channelId))
        } else {
            // Locked. Here we would emit an event to the UI to show the PIN prompt.
            // For modularity, we just block the tune. The UI will call verifyPin().
            // If verifyPin() succeeds, it will emit TuneApproved.
        }
    }

    /**
     * Hashes the given string using SHA-256.
     */
    fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(pin.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun setPin(pin: String) {
        appPreferences.parentalPinHash = hashPin(pin)
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = appPreferences.parentalPinHash ?: return true
        return hashPin(pin) == storedHash
    }
}
