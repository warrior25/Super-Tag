package com.huikka.supertag.data.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TimeConverter {
    companion object {

        private const val FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss"

        fun longToTimestamp(time: Long): String {
            val format = SimpleDateFormat(FORMAT_STRING, Locale.US)
            return format.format(Date(time))
        }

        fun timestampToLong(timestamp: String): Long {
            val format = SimpleDateFormat(FORMAT_STRING, Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            return format.parse(timestamp)?.time ?: 0
        }
    }
}