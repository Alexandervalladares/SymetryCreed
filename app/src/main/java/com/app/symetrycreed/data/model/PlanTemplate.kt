package com.app.symetrycreed.data.model

data class PlanTemplate(
    var id: String = "",
    var title: String = "",
    var level: String = "", // "beginner", "intermediate", "advanced"
    var weeks: Int = 0,
    var trainingsCount: Int = 0,
    var stripeColorHex: String = "#FF5160",
    var shortDescription: String = "",
    var createdAt: Long = 0L
) {
    fun isValid(): Boolean {
        return title.isNotBlank() &&
                title.length <= 80 &&
                level in listOf("beginner", "intermediate", "advanced") &&
                weeks in 1..52 &&
                trainingsCount >= 0 &&
                stripeColorHex.matches(Regex("^#[0-9A-Fa-f]{6}$")) &&
                shortDescription.length <= 500
    }

    // Convertir template a Plan de usuario
    fun toPlan(userId: String): com.app.symetrycreed.model.Plan {
        return com.app.symetrycreed.model.Plan(
            id = "", // Se generará uno nuevo
            title = this.title,
            level = this.level,
            weeks = this.weeks,
            trainingsCount = this.trainingsCount,
            stripeColorHex = this.stripeColorHex,
            shortDescription = this.shortDescription,
            createdAt = System.currentTimeMillis(),
            isActive = false
        )
    }
}