package com.app.symetrycreed.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.databinding.ActivityCenterBinding
import com.app.symetrycreed.ui.training.GenerateTrainingActivity
import com.app.symetrycreed.ui.training.RoutinesActivity
import com.app.symetrycreed.ui.training.TrainingActivity
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

        val user = auth.currentUser
        val display = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email ?: "Usuario"
        binding.tvUsuario.text = display

        // Abrir TrainingActivity desde el card "Nuevo Entrenamiento"
        binding.cardNuevoEntrenamiento.setOnClickListener {
            val intent = Intent(this, TrainingActivity::class.java)
            startActivity(intent)
        }

        // --- AÑADIDO: abrir GenerateTrainingActivity desde el card "Generar Entrenamiento" ---
        binding.cardGenerarEntrenamiento.setOnClickListener {
            val intent = Intent(this, GenerateTrainingActivity::class.java)
            startActivity(intent)
        }

        // Abrir "Tus Rutinas" desde el card correspondiente
        binding.cardTusRutinas.setOnClickListener {
            val i = Intent(this, RoutinesActivity::class.java)
            startActivity(i)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshTrainingsCount()
    }

    private fun refreshTrainingsCount() {
        val user = auth.currentUser ?: return
        val trainingsRef = db.child("users").child(user.uid).child("trainings")
        trainingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                binding.countRutinas.text = count.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Puedes manejar/loguear el error si lo deseas
            }
        })
    }
}