package com.app.symetrycreed.ui.training

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.model.Exercise
import com.app.symetrycreed.model.Training
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RoutinesActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private val db by lazy { FirebaseDatabase.getInstance().reference }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routines)
        container = findViewById(R.id.containerRoutines)

        loadRoutines()
    }

    override fun onResume() {
        super.onResume()
        // Recargar rutinas al volver a la actividad
        loadRoutines()
    }

    private fun loadRoutines() {
        val user = auth.currentUser
        if (user == null) {
            showEmptyState("Debes iniciar sesión")
            return
        }

        val ref = db.child("users").child(user.uid).child("trainings")

        ref.orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    container.removeAllViews()

                    if (!snapshot.exists() || !snapshot.hasChildren()) {
                        showEmptyState("No tienes entrenamientos guardados")
                        return
                    }

                    val trainings = mutableListOf<Training>()

                    for (child in snapshot.children) {
                        try {
                            val trainingId = child.key ?: continue
                            val title = child.child("title").getValue(String::class.java) ?: "Sin título"
                            val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                            val createdAt = child.child("createdAt").getValue(Long::class.java) ?: timestamp
                            val completedAt = child.child("completedAt").getValue(Long::class.java)
                            val duration = child.child("duration").getValue(Int::class.java)

                            // Leer ejercicios
                            val exercisesList = mutableListOf<Exercise>()
                            val exercisesSnapshot = child.child("exercises")

                            for (exChild in exercisesSnapshot.children) {
                                val exId = exChild.key ?: ""
                                val exName = exChild.child("name").getValue(String::class.java) ?: ""
                                val exSeries = exChild.child("series").getValue(Int::class.java) ?: 0
                                val exReps = exChild.child("reps").getValue(Int::class.java) ?: 0
                                val exWeight = exChild.child("weightKg").getValue(Double::class.java) ?: 0.0
                                val exRestSec = exChild.child("restSec").getValue(Int::class.java)
                                val exMuscle = exChild.child("muscle").getValue(String::class.java) ?: ""
                                val exNotes = exChild.child("notes").getValue(String::class.java) ?: ""
                                val exCompleted = exChild.child("completed").getValue(Boolean::class.java) ?: false

                                if (exName.isNotBlank()) {
                                    exercisesList.add(
                                        Exercise(
                                            id = exId,
                                            name = exName,
                                            series = exSeries,
                                            reps = exReps,
                                            weightKg = exWeight,
                                            restSec = exRestSec,
                                            muscle = exMuscle,
                                            notes = exNotes,
                                            completed = exCompleted
                                        )
                                    )
                                }
                            }

                            val training = Training(
                                id = trainingId,
                                title = title,
                                timestamp = timestamp,
                                createdAt = createdAt,
                                completedAt = completedAt,
                                duration = duration,
                                exercises = exercisesList
                            )

                            trainings.add(training)

                        } catch (e: Exception) {
                            android.util.Log.e("RoutinesActivity", "Error parseando training: ${e.message}", e)
                        }
                    }

                    if (trainings.isEmpty()) {
                        showEmptyState("No se pudieron cargar los entrenamientos")
                        return
                    }

                    // Ordenar por timestamp descendente (más recientes primero)
                    trainings.sortByDescending { it.timestamp }

                    // Mostrar
                    for (training in trainings) {
                        addRoutineCard(training)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("RoutinesActivity", "Error: ${error.message}")
                    showEmptyState("Error al cargar: ${error.message}")
                }
            })
    }

    private fun addRoutineCard(training: Training) {
        val inflater = LayoutInflater.from(this)
        val card = inflater.inflate(R.layout.item_training_detailed, container, false)

        val tvTitle = card.findViewById<TextView>(R.id.tvTrainingTitle)
        val tvExercisesCount = card.findViewById<TextView>(R.id.tvExercisesCount)
        val tvCompletionStatus = card.findViewById<TextView>(R.id.tvCompletionStatus)
        val btnStart = card.findViewById<MaterialButton>(R.id.btnStartTraining)
        val btnEdit = card.findViewById<MaterialButton>(R.id.btnEditTraining)

        tvTitle.text = training.title
        tvExercisesCount.text = "${training.exercises.size} ejercicios"

        // Mostrar estado de completación
        if (training.completedAt != null) {
            tvCompletionStatus.text = "✓ Completado"
            tvCompletionStatus.setTextColor(getColor(R.color.green_completed))
            btnStart.text = "Repetir"
        } else {
            val completedExercises = training.exercises.count { it.completed }
            if (completedExercises > 0) {
                tvCompletionStatus.text = "En progreso ($completedExercises/${training.exercises.size})"
                tvCompletionStatus.setTextColor(getColor(R.color.cyan))
            } else {
                tvCompletionStatus.text = "No iniciado"
                tvCompletionStatus.setTextColor(getColor(R.color.textSecondary))
            }
            btnStart.text = "Iniciar"
        }

        // Botón iniciar/repetir entrenamiento
        btnStart.setOnClickListener {
            val intent = Intent(this, ActiveTrainingActivity::class.java)
            intent.putExtra("training", training)
            startActivity(intent)
        }

        // Botón editar
        btnEdit.setOnClickListener {
            val intent = Intent(this, ExercisesActivity::class.java)
            intent.putExtra("training", training)
            startActivity(intent)
        }

        container.addView(card)
    }

    private fun showEmptyState(message: String) {
        container.removeAllViews()
        val emptyView = TextView(this).apply {
            text = message
            textSize = 16f
            setTextColor(getColor(R.color.textSecondary))
            gravity = android.view.Gravity.CENTER
            setPadding(24, 48, 24, 48)
        }
        container.addView(emptyView)
    }
}