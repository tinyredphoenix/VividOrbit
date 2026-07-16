package com.custom.dth.aosp_port

import com.custom.dth.data.local.ChannelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChannelDataManagerWrapper(
    private val repository: ChannelRepository
) : ChannelDataManager {

    private val listeners = mutableListOf<ChannelDataManager.Listener>()
    private var channels = emptyList<Channel>()
    private var isLoaded = false
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        scope.launch {
            repository.observeChannels().collect { mergedList ->
                channels = mergedList.map { ChannelImpl(it) }
                
                if (!isLoaded) {
                    isLoaded = true
                    listeners.toList().forEach { it.onLoadFinished() }
                }
                
                listeners.toList().forEach { it.onChannelListUpdated() }
                listeners.toList().forEach { it.onChannelBrowsableChanged() }
            }
        }
    }

    override fun getChannelList(): List<Channel> {
        return channels
    }

    override fun addListener(listener: ChannelDataManager.Listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
            if (isLoaded) {
                listener.onLoadFinished()
                listener.onChannelListUpdated()
                listener.onChannelBrowsableChanged()
            }
        }
    }

    override fun removeListener(listener: ChannelDataManager.Listener) {
        listeners.remove(listener)
    }

    override fun isDbLoadFinished(): Boolean {
        return isLoaded
    }
}
