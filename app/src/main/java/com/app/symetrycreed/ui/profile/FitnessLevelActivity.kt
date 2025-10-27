package com.app.symetrycreed.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.databinding.ActivityFitnessLevelBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class FitnessLevelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFitnessLevelBinding
    private var selectedLevel: String? = null

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFitnessLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnContinue.isEnabled = false

        binding.cardBeginner.setOnClickListener {
            select(binding.cardBeginner, "beginner")
        }
        binding.cardIntermediate.setOnClickListener {
            select(binding.cardIntermediate, "intermediate")
        }
        binding.cardAdvanced.setOnClickListener {
            select(binding.cardAdvanced, "advanced")
        }

        binding.btnContinue.setOnClickListener {
            val uid = auth.currentUser?.uid
            val level = selectedLevel
            if (uid == null || level == null) return@setOnClickListener

            if (level !in listOf("beginner", "intermediate", "advanced")) {
                Snackbar.make(binding.root, "Nivel inválido", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnContinue.isEnabled = false
            saveFitnessLevel(uid, level)
        }

        applySelectionUI(null)
    }

    private fun saveFitnessLevel(uid: String, level: String) {
        val userRef = db.child("users").child(uid)

        userRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                // Usuario nuevo - crear estructura completa
                createNewUser(uid, level)
            } else {
                // Usuario existente - actualizar solo fitness level
                updateFitnessLevel(uid, level)
            }
        }.addOnFailureListener { e ->
            binding.btnContinue.isEnabled = true
            android.util.Log.e("FitnessLevel", "Error checking user: ${e.message}", e)
            Snackbar.make(binding.root, "Error de conexión: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun createNewUser(uid: String, level: String) {
        val userRef = db.child("users").child(uid)
        val profileRef = userRef.child("profile")

        // Crear usuario campo por campo para evitar problemas de validación
        userRef.child("uid").setValue(uid)
            .addOnSuccessListener {
                android.util.Log.d("FitnessLevel", "UID set")
                // Crear createdAt
                userRef.child("createdAt").setValue(ServerValue.TIMESTAMP)
                    .addOnSuccessListener {
                        android.util.Log.d("FitnessLevel", "createdAt set")
                        // Crear lastLoginAt
                        userRef.child("lastLoginAt").setValue(ServerValue.TIMESTAMP)
                            .addOnSuccessListener {
                                android.util.Log.d("FitnessLevel", "lastLoginAt set")
                                // Crear fitnessLevel
                                profileRef.child("fitnessLevel").setValue(level)
                                    .addOnSuccessListener {
                                        android.util.Log.d("FitnessLevel", "fitnessLevel set")
                                        // Crear fitnessLevelUpdatedAt
                                        profileRef.child("fitnessLevelUpdatedAt").setValue(ServerValue.TIMESTAMP)
                                            .addOnSuccessListener {
                                                android.util.Log.d("FitnessLevel", "User created successfully")
                                                goNext()
                                            }
                                            .addOnFailureListener { e ->
                                                binding.btnContinue.isEnabled = true
                                                android.util.Log.e("FitnessLevel", "Error setting fitnessLevelUpdatedAt: ${e.message}", e)
                                                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        binding.btnContinue.isEnabled = true
                                        android.util.Log.e("FitnessLevel", "Error setting fitnessLevel: ${e.message}", e)
                                        Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                binding.btnContinue.isEnabled = true
                                android.util.Log.e("FitnessLevel", "Error setting lastLoginAt: ${e.message}", e)
                                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        binding.btnContinue.isEnabled = true
                        android.util.Log.e("FitnessLevel", "Error setting createdAt: ${e.message}", e)
                        Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.btnContinue.isEnabled = true
                android.util.Log.e("FitnessLevel", "Error setting uid: ${e.message}", e)
                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun updateFitnessLevel(uid: String, level: String) {
        val userRef = db.child("users").child(uid)
        val profileRef = userRef.child("profile")

        // Actualizar cada campo individualmente
        profileRef.child("fitnessLevel").setValue(level)
            .addOnSuccessListener {
                android.util.Log.d("FitnessLevel", "Fitness level updated")
                // Actualizar timestamp
                profileRef.child("fitnessLevelUpdatedAt").setValue(ServerValue.TIMESTAMP)
                    .addOnSuccessListener {
                        android.util.Log.d("FitnessLevel", "Timestamp updated")
                        // Actualizar lastLoginAt
                        userRef.child("lastLoginAt").setValue(ServerValue.TIMESTAMP)
                            .addOnSuccessListener {
                                android.util.Log.d("FitnessLevel", "All updates successful")
                                goNext()
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.e("FitnessLevel", "Error updating lastLoginAt: ${e.message}", e)
                                // Continuar de todas formas
                                goNext()
                            }
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("FitnessLevel", "Error updating timestamp: ${e.message}", e)
                        // Continuar de todas formas
                        goNext()
                    }
            }
            .addOnFailureListener { e ->
                binding.btnContinue.isEnabled = true
                android.util.Log.e("FitnessLevel", "Error updating fitness level: ${e.message}", e)
                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun goNext() {
        Snackbar.make(binding.root, "Estado guardado ✓", Snackbar.LENGTH_SHORT).show()
        startActivity(Intent(this, ObstacleTextActivity::class.java))
        finish()
    }

    private fun select(card: MaterialCardView, value: String) {
        selectedLevel = value
        binding.btnContinue.isEnabled = true
        applySelectionUI(card)
    }

    private fun applySelectionUI(selectedCard: MaterialCardView?) {
        val groups = listOf(binding.cardBeginner, binding.cardIntermediate, binding.cardAdvanced)
        groups.forEach { card ->
            val isSelected = card == selectedCard
            card.strokeWidth =
                if (isSelected) resources.getDimensionPixelSize(R.dimen.card_stroke_selected) else 0
            card.cardElevation =
                if (isSelected) resources.getDimension(R.dimen.card_elevation_selected) else 0f
            val bg =
                if (isSelected) getColor(R.color.gray_box_selected) else getColor(R.color.gray_box)
            card.setCardBackgroundColor(bg)
        }
    }
}