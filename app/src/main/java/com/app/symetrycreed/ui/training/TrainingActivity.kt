package com.app.symetrycreed.ui.training

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.model.Exercise
import com.app.symetrycreed.model.Training
import java.util.*

class TrainingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        // IDs del layout (asegúrate de tenerlos en activity_training.xml)
        val btnNewTraining = findViewById<View>(R.id.btnNewTraining)
        val optRutinaRapida = findViewById<View>(R.id.opt_rutina_rapida)
        val optTemporizador = findViewById<View>(R.id.opt_temporizador)
        val btnComenzar1 = findViewById<View>(R.id.btn_comenzar_1)
        val btnComenzar2 = findViewById<View>(R.id.btn_comenzar_2)

        // BOTÓN: abre ExercisesActivity vacío (editable) para crear nuevo entrenamiento
        btnNewTraining.setOnClickListener {
            // abrir ExercisesActivity sin datos => nuevo entrenamiento vacío
            val intent = Intent(this, ExercisesActivity::class.java)
            startActivity(intent)
        }

        // RUTINA RÁPIDA: genera un training predefinido y abre ExercisesActivity con datos
        optRutinaRapida.setOnClickListener {
            val training = buildQuickTraining()
            openExercisesWithTraining(training)
        }

        // TEMPORIZADOR: abre TimerActivity (simple countdown)
        optTemporizador.setOnClickListener {
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        }

        // Suggested cards: Comenzar -> abrir Exercises con training sugerido
        btnComenzar1.setOnClickListener {
            val training = buildSuggestedHiit()
            openExercisesWithTraining(training)
        }
        btnComenzar2.setOnClickListener {
            val training = buildSuggestedStrength()
            openExercisesWithTraining(training)
        }
    }

    private fun openExercisesWithTraining(training: Training) {
        val i = Intent(this, ExercisesActivity::class.java)
        i.putExtra("training", training)
        startActivity(i)
    }

    // Ejemplo: rutina rápida generada dinámicamente
    private fun buildQuickTraining(): Training {
        val list = listOf(
            Exercise(name = "Jumping Jacks", series = 3, reps = 20),
            Exercise(name = "Push Ups", series = 3, reps = 12),
            Exercise(name = "Squats", series = 3, reps = 15)
        )
        return Training(
            id = UUID.randomUUID().toString(),
            title = "Rutina Rápida",
            timestamp = System.currentTimeMillis(),
            exercises = list
        )
    }

    private fun buildSuggestedHiit(): Training {
        val list = listOf(
            Exercise(name = "Burpees", series = 4, reps = 12),
            Exercise(name = "Mountain Climbers", series = 4, reps = 20),
            Exercise(name = "High Knees", series = 4, reps = 30)
        )
        return Training(
            id = UUID.randomUUID().toString(),
            title = "Entrenamiento Rápido",
            timestamp = System.currentTimeMillis(),
            exercises = list
        )
    }

    private fun buildSuggestedStrength(): Training {
        val list = listOf(
            Exercise(name = "Bench Press", series = 4, reps = 10, weightKg = 40.0),
            Exercise(name = "Dumbbell Row", series = 4, reps = 10, weightKg = 22.5),
            Exercise(name = "Shoulder Press", series = 3, reps = 12, weightKg = 18.0)
        )
        return Training(
            id = UUID.randomUUID().toString(),
            title = "Fuerza Superior",
            timestamp = System.currentTimeMillis(),
            exercises = list
        )
    }
}