package com.quick.play.data

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val name: String,
    val url: String,
    val userAgent: String = "",
    val isCustom: Boolean = true
)

@Serializable
data class Channel(
    val name: String,
    val logo: String,
    val group: String,
    val url: String,
    val licenseType: String = "",
    val licenseKey: String = "",
    val userAgent: String = "",
    val cookie: String = ""
)