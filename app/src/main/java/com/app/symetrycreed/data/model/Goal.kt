package com.app.symetrycreed.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Goal(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val target: Double = 0.0,
    val current: Double = 0.0,
    val unit: String = "",
    val colorHex: String = "#FF5160",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "target" to target,
            "current" to current,
            "unit" to unit,
            "colorHex" to colorHex,
            "createdAt" to createdAt
        )
    }

    fun getProgressPercentage(): Int {
        return if (target > 0) {
            ((current / target) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }
}