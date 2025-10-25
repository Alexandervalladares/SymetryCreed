package com.app.symetrycreed.model

import java.io.Serializable

// Data class para un entrenamiento (Serializable para pasar por Intent)
data class Training(
    var id: String = "",
    var title: String = "",
    var timestamp: Long = 0L,
    var exercises: List<Exercise> = emptyList()
) : Serializable