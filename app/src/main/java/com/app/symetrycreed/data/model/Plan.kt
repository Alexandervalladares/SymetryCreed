package com.app.symetrycreed.model

data class Plan(
    var id: String = "",
    var title: String = "",
    var level: String = "",
    var weeks: Int = 0,
    var trainingsCount: Int = 0,
    var stripeColorHex: String = "#FF5160",
    var shortDescription: String = "",
    var createdAt: Long = 0L,
    var updatedAt: Long? = null,
    var startedAt: Long? = null,
    var completedAt: Long? = null,
    var isActive: Boolean = false
) {
    // Validación client-side que coincide con las rules
    fun isValid(): Boolean {
        return title.isNotBlank() &&
                title.length <= 80 &&
                level in listOf("beginner", "intermediate", "advanced") &&
                weeks in 1..52 &&
                trainingsCount >= 0 &&
                stripeColorHex.matches(Regex("^#[0-9A-Fa-f]{6}$")) &&
                shortDescription.length <= 500
    }

    // Método para convertir a Map para Firebase
    fun toMap(): Map<String, Any?> {
        val map = hashMapOf<String, Any?>(
            "id" to id,
            "title" to title,
            "level" to level,
            "weeks" to weeks,  // ✅ Se guarda como Int
            "trainingsCount" to trainingsCount,  // ✅ Se guarda como Int
            "stripeColorHex" to stripeColorHex,
            "shortDescription" to shortDescription,
            "createdAt" to createdAt
        )

        // Solo agregar campos opcionales si no son null
        updatedAt?.let { map["updatedAt"] = it }
        startedAt?.let { map["startedAt"] = it }
        completedAt?.let { map["completedAt"] = it }
        map["isActive"] = isActive

        return map
    }
}
