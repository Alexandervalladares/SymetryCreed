package com.app.symetrycreed.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Exercise(
    var id: String = "",
    var name: String = "",
    var series: Int = 3,
    var reps: Int = 10,
    var weightKg: Double = 0.0,
    var restSec: Int? = null, // tiempo de descanso en segundos
    var muscle: String = "", // grupo muscular
    var notes: String = "",
    var completed: Boolean = false
) : Parcelable {

    fun isValid(): Boolean {
        return name.isNotBlank() &&
                name.length <= 80 &&
                series in 1..20 &&
                reps in 1..1000 &&
                weightKg >= 0 && weightKg <= 1000 &&
                (restSec == null || restSec!! in 0..600) &&
                muscle.length <= 50 &&
                notes.length <= 200
    }

    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "series" to series,
            "reps" to reps,
            "weightKg" to weightKg,
            "restSec" to restSec,
            "muscle" to muscle,
            "notes" to notes,
            "completed" to completed
        ).filterValues { it != null }
    }
}