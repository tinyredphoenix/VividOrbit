package com.custom.dth.aosp_port

interface ChannelDataManager {
    fun getChannelList(): List<Channel>
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
    fun isDbLoadFinished(): Boolean

    interface Listener {
        fun onLoadFinished()
        fun onChannelListUpdated()
        fun onChannelBrowsableChanged()
    }
}
