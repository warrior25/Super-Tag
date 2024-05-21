package com.huikka.supertag.data.helpers

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeConverter {
    companion object {

        private const val FORMAT_STRING = "yyyy-MM-dd HH:mm:ss"

        fun longToTimestamp(time: Long): String {
            val format = SimpleDateFormat(FORMAT_STRING, Locale.getDefault())
            Log.d("FORMAT", format.format(Date(time)))
            return format.format(Date(time))
        }
    }
}