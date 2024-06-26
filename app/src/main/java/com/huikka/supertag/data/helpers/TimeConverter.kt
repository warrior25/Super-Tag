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
            format.timeZone = TimeZone.getTimeZone("UTC")
            return format.format(Date(time))
        }

        fun timestampToLong(timestamp: String): Long {
            val format = SimpleDateFormat(FORMAT_STRING, Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            val date = format.parse(timestamp) ?: return 0
            return date.time
        }

        fun longToMinutesAndSeconds(time: Long): Pair<Long, Long> {
            // Convert delay from milliseconds to seconds
            val delaySeconds = time / 1000

            // Calculate minutes and remaining seconds
            val minutes = delaySeconds / 60
            val seconds = delaySeconds % 60

            return Pair(minutes, seconds)
        }
    }
}