package com.app.symetrycreed.ui.training

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.model.Exercise
import com.app.symetrycreed.model.Training

class TrainingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Nuevo Entrenamiento
        findViewById<View>(R.id.btnNewTraining)?.setOnClickListener {
            android.util.Log.d("TrainingActivity", "Click: Nuevo Entrenamiento")
            val intent = Intent(this, ExercisesActivity::class.java)
            startActivity(intent)
        }

        // Rutina Rápida
        findViewById<View>(R.id.opt_rutina_rapida)?.setOnClickListener {
            android.util.Log.d("TrainingActivity", "Click: Rutina Rápida")
            val training = buildQuickTraining()
            openExercisesWithTraining(training)
        }

        // Temporizador
        findViewById<View>(R.id.opt_temporizador)?.setOnClickListener {
            android.util.Log.d("TrainingActivity", "Click: Temporizador")
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        }

        // Sugerido 1: HIIT
        findViewById<View>(R.id.btn_comenzar_1)?.setOnClickListener {
            android.util.Log.d("TrainingActivity", "Click: Comenzar HIIT")
            val training = buildSuggestedHiit()
            openExercisesWithTraining(training)
        }

        // Sugerido 2: Fuerza Superior
        findViewById<View>(R.id.btn_comenzar_2)?.setOnClickListener {
            android.util.Log.d("TrainingActivity", "Click: Comenzar Fuerza")
            val training = buildSuggestedStrength()
            openExercisesWithTraining(training)
        }
    }

    private fun openExercisesWithTraining(training: Training) {
        val intent = Intent(this, ExercisesActivity::class.java)
        intent.putExtra("training", training)
        startActivity(intent)
    }

    // ========== ENTRENAMIENTOS PREDEFINIDOS ==========

    private fun buildQuickTraining(): Training {
        val currentTime = System.currentTimeMillis()
        val exercises = listOf(
            Exercise(
                id = "",
                name = "Jumping Jacks",
                series = 3,
                reps = 20,
                weightKg = 0.0,
                muscle = "cardio",
                restSec = 30
            ),
            Exercise(
                id = "",
                name = "Push Ups",
                series = 3,
                reps = 12,
                weightKg = 0.0,
                muscle = "chest",
                restSec = 60
            ),
            Exercise(
                id = "",
                name = "Squats",
                series = 3,
                reps = 15,
                weightKg = 0.0,
                muscle = "legs",
                restSec = 60
            )
        )

        return Training(
            id = "",
            title = "Rutina Rápida",
            timestamp = currentTime,
            createdAt = currentTime,
            exercises = exercises,
            notes = "Entrenamiento rápido y efectivo"
        )
    }

    private fun buildSuggestedHiit(): Training {
        val currentTime = System.currentTimeMillis()
        val exercises = listOf(
            Exercise(
                id = "",
                name = "Burpees",
                series = 4,
                reps = 12,
                weightKg = 0.0,
                muscle = "full body",
                restSec = 60,
                notes = "Mantén el ritmo constante"
            ),
            Exercise(
                id = "",
                name = "Mountain Climbers",
                series = 4,
                reps = 20,
                weightKg = 0.0,
                muscle = "core",
                restSec = 45,
                notes = "Abdomen contraído"
            ),
            Exercise(
                id = "",
                name = "High Knees",
                series = 4,
                reps = 30,
                weightKg = 0.0,
                muscle = "cardio",
                restSec = 60,
                notes = "Rodillas arriba"
            )
        )

        return Training(
            id = "",
            title = "Entrenamiento Rápido",
            timestamp = currentTime,
            createdAt = currentTime,
            exercises = exercises,
            notes = "Entrenamiento HIIT de alta intensidad",
            duration = 20
        )
    }

    private fun buildSuggestedStrength(): Training {
        val currentTime = System.currentTimeMillis()
        val exercises = listOf(
            Exercise(
                id = "",
                name = "Bench Press",
                series = 4,
                reps = 10,
                weightKg = 40.0,
                muscle = "chest",
                restSec = 90,
                notes = "Controla el descenso"
            ),
            Exercise(
                id = "",
                name = "Dumbbell Row",
                series = 4,
                reps = 10,
                weightKg = 22.5,
                muscle = "back",
                restSec = 90,
                notes = "Mantén la espalda recta"
            ),
            Exercise(
                id = "",
                name = "Shoulder Press",
                series = 3,
                reps = 12,
                weightKg = 18.0,
                muscle = "shoulders",
                restSec = 75,
                notes = "No arquees la espalda"
            )
        )

        return Training(
            id = "",
            title = "Fuerza Superior",
            timestamp = currentTime,
            createdAt = currentTime,
            exercises = exercises,
            notes = "Enfoque en tren superior",
            duration = 30
        )
    }
}
