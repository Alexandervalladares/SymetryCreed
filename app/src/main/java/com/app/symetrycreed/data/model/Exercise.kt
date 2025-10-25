package com.app.symetrycreed.model

import java.io.Serializable

// Data class para un ejercicio (Serializable para pasar por Intent)
data class Exercise(
    var id: String = "",
    var name: String = "",
    var series: Int = 3,
    var reps: Int = 10,
    var weightKg: Double = 0.0
) : Serializable