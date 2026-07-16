package com.custom.dth.aosp_port

interface Channel {
    fun getId(): Long
    fun isPassthrough(): Boolean
    fun getInputId(): String?
    fun isBrowsable(): Boolean
    fun hasSameReadOnlyInfo(other: Channel?): Boolean

    companion object {
        const val INVALID_ID = -1L
        const val CHANNEL_NUMBER_DELIMITER = "-"
    }
}
