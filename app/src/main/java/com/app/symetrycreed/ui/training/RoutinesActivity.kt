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
                            // ✅ CORRECCIÓN: Leer datos manualmente
                            val trainingId = child.key ?: continue
                            val title = child.child("title").getValue(String::class.java) ?: "Sin título"
                            val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                            val createdAt = child.child("createdAt").getValue(Long::class.java) ?: timestamp

                            // ✅ Leer ejercicios como Map
                            val exercisesList = mutableListOf<Exercise>()
                            val exercisesSnapshot = child.child("exercises")

                            for (exChild in exercisesSnapshot.children) {
                                val exId = exChild.key ?: ""
                                val exName = exChild.child("name").getValue(String::class.java) ?: ""
                                val exSeries = exChild.child("series").getValue(Int::class.java) ?: 0
                                val exReps = exChild.child("reps").getValue(Int::class.java) ?: 0
                                val exWeight = exChild.child("weightKg").getValue(Double::class.java) ?: 0.0

                                if (exName.isNotBlank()) {
                                    exercisesList.add(
                                        Exercise(
                                            id = exId,
                                            name = exName,
                                            series = exSeries,
                                            reps = exReps,
                                            weightKg = exWeight
                                        )
                                    )
                                }
                            }

                            val training = Training(
                                id = trainingId,
                                title = title,
                                timestamp = timestamp,
                                createdAt = createdAt,
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
                        addRoutineRow(training)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("RoutinesActivity", "Error: ${error.message}")
                    showEmptyState("Error al cargar: ${error.message}")
                }
            })
    }

    private fun addRoutineRow(training: Training) {
        val inflater = LayoutInflater.from(this)
        val row = inflater.inflate(R.layout.item_routine_row, container, false)
        val tvTitle = row.findViewById<TextView>(R.id.tvRoutineTitle)
        val tvCount = row.findViewById<TextView>(R.id.tvRoutineCount)

        tvTitle.text = training.title
        tvCount.text = "${training.exercises.size} ejercicios"

        row.setOnClickListener {
            val i = Intent(this, ExercisesActivity::class.java)
            i.putExtra("training", training)
            startActivity(i)
        }

        container.addView(row)
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