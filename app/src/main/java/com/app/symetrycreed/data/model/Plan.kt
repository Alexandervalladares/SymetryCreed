package com.app.symetrycreed.model

// Data class para Plan. Compatible con Firebase (constructor vacío).
data class Plan(
    var id: String = "",
    var title: String = "",
    var level: String = "",
    var weeks: String = "",
    var trainingsCount: String = "",
    var stripeColorHex: String = "#FF5160", // color en HEX para la banda izquierda
    var shortDescription: String = "" // descripción opcional / preview
)