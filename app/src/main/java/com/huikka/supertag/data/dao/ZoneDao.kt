package com.huikka.supertag.data.dao

import android.util.Log
import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.Zone
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

class ZoneDao(application: STApplication) {
    private val db: Postgrest = application.supabase.postgrest

    suspend fun getZones(): List<Zone> {
        return try {
            db.from("zones").select().decodeList<Zone>()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAttractions(): List<Zone> {
        return try {
            db.from("zones").select {
                filter {
                    eq("type", "attraction")
                }
            }.decodeList<Zone>()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getZoneById(id: String): Zone? {
        return try {
            db.from("zones").select {
                filter {
                    eq("id", id)
                }
            }.decodeSingleOrNull<Zone>()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getPlayingArea(): Zone? {
        return try {
            db.from("zones").select {
                filter {
                    eq("type", "playing_area")
                }
            }.decodeSingle<Zone>()
        } catch (e: Exception) {
            Log.e("PLAYING_AREA", "Failed to fetch playing area")
            null
        }
    }
}