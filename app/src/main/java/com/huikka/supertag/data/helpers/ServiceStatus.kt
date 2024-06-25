package com.huikka.supertag.data.helpers

import android.content.Context

class ServiceStatus {
    companion object {
        fun isServiceRunning(context: Context): Boolean {
            val prefs = context.getSharedPreferences("service_prefs", Context.MODE_PRIVATE)
            return prefs.getBoolean("is_service_running", false)
        }

        fun setServiceRunning(context: Context, isRunning: Boolean) {
            val prefs = context.getSharedPreferences("service_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("is_service_running", isRunning).apply()
        }
    }
}