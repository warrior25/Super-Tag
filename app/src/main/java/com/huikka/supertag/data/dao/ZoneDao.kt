package com.huikka.supertag.data.dao

import com.huikka.supertag.STApplication
import com.huikka.supertag.data.dto.Zone
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

class ZoneDao(application: STApplication) {
    private val db: Postgrest = application.supabase.postgrest

    suspend fun getZones(): List<Zone> {
        return db.from("zones").select().decodeList<Zone>()

    }

    suspend fun getZoneById(id: Int): Zone? {
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

    suspend fun getPlayingArea(): Zone {
        return db.from("zones").select {
            filter {
                eq("type", "playing_area")
            }
        }.decodeSingle<Zone>()
    }
}