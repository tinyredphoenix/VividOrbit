package com.custom.dth.aosp_port

import com.custom.dth.data.local.MergedChannel

class ChannelImpl(private val mergedChannel: MergedChannel) : Channel {
    val internalChannel: MergedChannel get() = mergedChannel

    override fun getId(): Long = mergedChannel.id

    override fun isPassthrough(): Boolean = false // DVB channels are not passthrough inputs

    override fun getInputId(): String? = "com.droidlogic.dtvkit.inputsource/.DtvkitTvInput/HW19"

    override fun isBrowsable(): Boolean = true // We manage our own visibility

    override fun hasSameReadOnlyInfo(other: Channel?): Boolean {
        if (other !is ChannelImpl) return false
        val otherMerged = other.internalChannel
        return mergedChannel.displayName == otherMerged.displayName &&
               mergedChannel.displayNumber == otherMerged.displayNumber &&
               mergedChannel.logoPath == otherMerged.logoPath
    }
}
