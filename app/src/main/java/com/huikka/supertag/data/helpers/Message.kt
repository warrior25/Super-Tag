package com.huikka.supertag.data.helpers

import kotlinx.serialization.Serializable


@Serializable
data class Message(val content: String, val sender: String)