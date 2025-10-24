package com.app.symetrycreed.data.model

data class AppUser(
    val uid: String = "",
    val name: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val provider: String = "google",
    val createdAt: Long? = null,
    val lastLoginAt: Long? = null
)