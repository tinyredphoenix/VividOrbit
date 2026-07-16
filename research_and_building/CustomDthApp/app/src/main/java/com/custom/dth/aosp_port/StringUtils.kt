package com.custom.dth.aosp_port

object StringUtils {
    @JvmStatic
    fun compare(lhs: String?, rhs: String?): Int {
        if (lhs === rhs) return 0
        if (lhs == null) return -1
        if (rhs == null) return 1
        return lhs.compareTo(rhs)
    }
}
