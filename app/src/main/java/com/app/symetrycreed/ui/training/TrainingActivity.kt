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

        // Rutina Rápida - AHORA INICIA EL ENTRENAMIENTO DIRECTAMENTE
        findViewById<View>(R.id.opt_rutina_rapida)?.setOnClickListener {
            android.util.Log.d("TrainingActivity", "Click: Rutina Rápida")
            val training = buildQuickTraining()
            startActiveTraining(training) // ✓ CAMBIO: Inicia directamente
        }

        // Temporizador
        findViewById<View>(R.id.opt_temporizador)?.setOnClickListener {
            android.util.Log.d("TrainingActivity", "Click: Temporizador")
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        }

        // Sugerido 1: HIIT - AHORA INICIA EL ENTRENAMIENTO DIRECTAMENTE
        findViewById<View>(R.id.btn_comenzar_1)?.setOnClickListener {
            android.util.Log.d("TrainingActivity", "Click: Comenzar HIIT")
            val training = buildSuggestedHiit()
            startActiveTraining(training) // ✓ CAMBIO: Inicia directamente
        }

        // Sugerido 2: Fuerza Superior - AHORA INICIA EL ENTRENAMIENTO DIRECTAMENTE
        findViewById<View>(R.id.btn_comenzar_2)?.setOnClickListener {
            android.util.Log.d("TrainingActivity", "Click: Comenzar Fuerza")
            val training = buildSuggestedStrength()
            startActiveTraining(training) // ✓ CAMBIO: Inicia directamente
        }
    }

    // ✓ NUEVO MÉTODO: Inicia el entrenamiento activo directamente
    private fun startActiveTraining(training: Training) {
        val intent = Intent(this, ActiveTrainingActivity::class.java)
        intent.putExtra("training", training)
        startActivity(intent)
    }

    // Método auxiliar para abrir en modo edición (ya no se usa para los sugeridos)
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
                id = "ex_${currentTime}_1",
                name = "Jumping Jacks",
                series = 3,
                reps = 20,
                weightKg = 0.0,
                muscle = "cardio",
                restSec = 30,
                notes = "Mantén un ritmo constante"
            ),
            Exercise(
                id = "ex_${currentTime}_2",
                name = "Push Ups",
                series = 3,
                reps = 12,
                weightKg = 0.0,
                muscle = "chest",
                restSec = 60,
                notes = "Espalda recta"
            ),
            Exercise(
                id = "ex_${currentTime}_3",
                name = "Squats",
                series = 3,
                reps = 15,
                weightKg = 0.0,
                muscle = "legs",
                restSec = 60,
                notes = "Rodillas alineadas con los pies"
            )
        )

        return Training(
            id = "quick_${currentTime}",
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
                id = "ex_${currentTime}_1",
                name = "Burpees",
                series = 4,
                reps = 12,
                weightKg = 0.0,
                muscle = "full body",
                restSec = 60,
                notes = "Mantén el ritmo constante"
            ),
            Exercise(
                id = "ex_${currentTime}_2",
                name = "Mountain Climbers",
                series = 4,
                reps = 20,
                weightKg = 0.0,
                muscle = "core",
                restSec = 45,
                notes = "Abdomen contraído"
            ),
            Exercise(
                id = "ex_${currentTime}_3",
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
            id = "hiit_${currentTime}",
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
                id = "ex_${currentTime}_1",
                name = "Bench Press",
                series = 4,
                reps = 10,
                weightKg = 40.0,
                muscle = "chest",
                restSec = 90,
                notes = "Controla el descenso"
            ),
            Exercise(
                id = "ex_${currentTime}_2",
                name = "Dumbbell Row",
                series = 4,
                reps = 10,
                weightKg = 22.5,
                muscle = "back",
                restSec = 90,
                notes = "Mantén la espalda recta"
            ),
            Exercise(
                id = "ex_${currentTime}_3",
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
            id = "strength_${currentTime}",
            title = "Fuerza Superior",
            timestamp = currentTime,
            createdAt = currentTime,
            exercises = exercises,
            notes = "Enfoque en tren superior",
            duration = 30
        )
    }
}