package com.huikka.supertag.data.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeConverter {
    companion object {

        private const val FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss"

        fun longToTimestamp(time: Long): String {
            val format = SimpleDateFormat(FORMAT_STRING, Locale.getDefault())
            return format.format(Date(time))
        }

        fun timestampToLong(timestamp: String): Long {
            val format = SimpleDateFormat(FORMAT_STRING, Locale.getDefault())
            return format.parse(timestamp)?.time ?: 0
        }
    }
}