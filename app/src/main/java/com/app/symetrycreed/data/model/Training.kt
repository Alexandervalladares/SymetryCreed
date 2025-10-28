package com.app.symetrycreed.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Training(
    var id: String = "",
    var title: String = "",
    var timestamp: Long = 0L,
    var createdAt: Long = 0L,
    var updatedAt: Long? = null,
    var completedAt: Long? = null,
    var duration: Int? = null,
    var notes: String = "",
    var exercises: List<Exercise> = emptyList()
) : Parcelable {

    fun isValid(): Boolean {
        return title.isNotBlank() &&
                title.length <= 80 &&
                notes.length <= 500 &&
                exercises.all { it.isValid() }
    }
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "timestamp" to timestamp,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "completedAt" to completedAt,
            "duration" to duration,
            "notes" to notes
        ).filterValues { it != null }
    }
}