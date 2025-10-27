package com.app.symetrycreed.ui.exercises

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.google.android.material.card.MaterialCardView

class ExerciseLibraryActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_library)

        container = findViewById(R.id.containerExercises)

        loadRecommendedExercises()
    }

    private fun loadRecommendedExercises() {
        val exercises = listOf(
            ExerciseInfo(
                "Flexiones",
                "Pecho",
                "Peso corporal",
                "Básico",
                "Colócate boca abajo con las manos a la altura de los hombros. Baja el cuerpo manteniendo la espalda recta hasta que el pecho casi toque el suelo. Empuja hacia arriba hasta la posición inicial.",
                30
            ),
            ExerciseInfo(
                "Sentadillas",
                "Piernas",
                "Peso corporal",
                "Básico",
                "Párate con los pies al ancho de los hombros. Baja como si fueras a sentarte en una silla, manteniendo la espalda recta. Empuja con los talones para volver arriba.",
                45
            ),
            ExerciseInfo(
                "Dominadas",
                "Espalda",
                "Barra",
                "Intermedio",
                "Agarra una barra con las palmas hacia adelante. Cuélgate completamente y luego tira de tu cuerpo hacia arriba hasta que tu barbilla supere la barra. Baja controladamente.",
                60
            ),
            ExerciseInfo(
                "Plancha",
                "Core",
                "Peso corporal",
                "Básico",
                "Apóyate en los antebrazos y las puntas de los pies. Mantén el cuerpo recto como una tabla, contrayendo el abdomen. No dejes que las caderas caigan.",
                30
            ),
            ExerciseInfo(
                "Burpees",
                "Cuerpo completo",
                "Peso corporal",
                "Avanzado",
                "Desde pie, baja a posición de flexión, haz una flexión, salta con los pies hacia las manos y salta verticalmente con los brazos arriba. Repite rápidamente.",
                20
            ),
            ExerciseInfo(
                "Press de Banca",
                "Pecho",
                "Barra",
                "Intermedio",
                "Acostado en un banco, toma la barra con las manos al ancho de los hombros. Baja la barra al pecho controladamente y empuja hacia arriba hasta extender los brazos.",
                90
            ),
            ExerciseInfo(
                "Peso Muerto",
                "Espalda/Piernas",
                "Barra",
                "Avanzado",
                "Con la barra en el suelo, agáchate manteniendo la espalda recta. Agarra la barra y levántala extendiendo las piernas y la espalda simultáneamente hasta estar de pie.",
                120
            ),
            ExerciseInfo(
                "Press Militar",
                "Hombros",
                "Mancuernas",
                "Intermedio",
                "De pie o sentado, sostén las mancuernas a la altura de los hombros. Empuja hacia arriba hasta extender los brazos completamente. Baja controladamente.",
                60
            ),
            ExerciseInfo(
                "Zancadas",
                "Piernas",
                "Peso corporal",
                "Básico",
                "Da un paso largo hacia adelante y baja el cuerpo hasta que ambas rodillas formen ángulos de 90 grados. Empuja con el pie delantero para volver a la posición inicial.",
                30
            ),
            ExerciseInfo(
                "Mountain Climbers",
                "Core/Cardio",
                "Peso corporal",
                "Intermedio",
                "En posición de flexión, lleva alternadamente las rodillas hacia el pecho rápidamente, como si estuvieras corriendo en el lugar.",
                30
            )
        )

        for (exercise in exercises) {
            addExerciseCard(exercise)
        }
    }

    private fun addExerciseCard(exercise: ExerciseInfo) {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.item_exercise_card, container, false) as MaterialCardView

        val tvName = cardView.findViewById<TextView>(R.id.tvExerciseName)
        val tvMuscle = cardView.findViewById<TextView>(R.id.tvExerciseMuscle)
        val tvEquipment = cardView.findViewById<TextView>(R.id.tvExerciseEquipment)
        val tvLevel = cardView.findViewById<TextView>(R.id.tvExerciseLevel)

        tvName.text = exercise.name
        tvMuscle.text = "🎯 ${exercise.muscle}"
        tvEquipment.text = "🏋️ ${exercise.equipment}"
        tvLevel.text = exercise.level

        val levelColor = when(exercise.level) {
            "Básico" -> getColor(android.R.color.holo_green_light)
            "Intermedio" -> getColor(android.R.color.holo_orange_light)
            "Avanzado" -> getColor(android.R.color.holo_red_light)
            else -> getColor(R.color.textSecondary)
        }
        tvLevel.setTextColor(levelColor)

        cardView.setOnClickListener {
            openExerciseDetail(exercise)
        }

        container.addView(cardView)
    }

    private fun openExerciseDetail(exercise: ExerciseInfo) {
        val intent = Intent(this, ExerciseDetailActivity::class.java)
        intent.putExtra("exercise_name", exercise.name)
        intent.putExtra("exercise_muscle", exercise.muscle)
        intent.putExtra("exercise_equipment", exercise.equipment)
        intent.putExtra("exercise_level", exercise.level)
        intent.putExtra("exercise_description", exercise.description)
        intent.putExtra("exercise_duration", exercise.recommendedDuration)
        startActivity(intent)
    }

    data class ExerciseInfo(
        val name: String,
        val muscle: String,
        val equipment: String,
        val level: String,
        val description: String,
        val recommendedDuration: Int // en segundos
    )
}