package com.app.symetrycreed.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.databinding.ActivityCenterBinding
import com.app.symetrycreed.ui.training.GenerateTrainingActivity
import com.app.symetrycreed.ui.training.RoutinesActivity
import com.app.symetrycreed.ui.training.TrainingActivity
import com.app.symetrycreed.ui.plans.UserPlansActivity
import com.app.symetrycreed.ui.exercises.ExerciseLibraryActivity
import com.app.symetrycreed.ui.progress.ProgressActivity
import com.app.symetrycreed.ui.goals.GoalsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CenterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCenterBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUserInfo()
        setupClickListeners()
        setupBottomNavigation()
    }

    private fun setupUserInfo() {
        val user = auth.currentUser
        val display = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email ?: "Usuario"
        binding.tvUsuario.text = display
    }

    private fun setupClickListeners() {
        // Nuevo Entrenamiento
        binding.cardNuevoEntrenamiento.setOnClickListener {
            startActivity(Intent(this, TrainingActivity::class.java))
        }

        // Generar Entrenamiento (Templates/Planes Globales)
        binding.cardGenerarEntrenamiento.setOnClickListener {
            startActivity(Intent(this, GenerateTrainingActivity::class.java))
        }

        // Tus Rutinas (Entrenamientos guardados)
        binding.cardTusRutinas.setOnClickListener {
            startActivity(Intent(this, RoutinesActivity::class.java))
        }

        // Tus Planificación (Planes del usuario)
        binding.cardTusPlanificacion.setOnClickListener {
            startActivity(Intent(this, UserPlansActivity::class.java))
        }

        // Ejercicios Recomendados (Biblioteca de ejercicios)
        binding.cardEntrenamientoVacio.setOnClickListener {
            startActivity(Intent(this, ExerciseLibraryActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_training -> {
                    startActivity(Intent(this, TrainingActivity::class.java))
                    true
                }
                R.id.nav_plans -> {
                    startActivity(Intent(this, GoalsActivity::class.java))
                    true
                }
                R.id.nav_progress -> {
                    startActivity(Intent(this, ProgressActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    // ✅ ABRIR PERFIL
                    startActivity(Intent(this, com.app.symetrycreed.ui.profile.ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        binding.bottomNavigation?.selectedItemId = R.id.nav_home
    }

    override fun onResume() {
        super.onResume()
        refreshStats()

        // Asegurar que el home esté seleccionado al volver
        binding.bottomNavigation?.selectedItemId = R.id.nav_home
    }

    private fun refreshStats() {
        val user = auth.currentUser ?: return

        // Contador de Rutinas (entrenamientos guardados)
        val trainingsRef = db.child("users").child(user.uid).child("trainings")
        trainingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                binding.countRutinas.text = count.toString()
                android.util.Log.d("CenterActivity", "Trainings count: $count")
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("CenterActivity", "Error al leer trainings: ${error.message}")
                binding.countRutinas.text = "0"
            }
        })

        // Contador de Planes del Usuario
        val plansRef = db.child("users").child(user.uid).child("plans")
        plansRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                binding.countPlanificacion.text = count.toString()

                // Mostrar/ocultar badge según si hay planes
                binding.countPlanificacion.visibility = if (count > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                android.util.Log.d("CenterActivity", "User plans count: $count")
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("CenterActivity", "Error al leer planes: ${error.message}")
                binding.countPlanificacion.text = "0"
                binding.countPlanificacion.visibility = View.GONE
            }
        })
    }
}