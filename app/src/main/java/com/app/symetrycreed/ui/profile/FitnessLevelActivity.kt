package com.app.symetrycreed.ui.profile

import android.content.Intent                         // 👈 IMPORTANTE
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

        binding.cardBeginner.setOnClickListener { select(binding.cardBeginner, "beginner") }
        binding.cardIntermediate.setOnClickListener { select(binding.cardIntermediate, "intermediate") }
        binding.cardAdvanced.setOnClickListener { select(binding.cardAdvanced, "advanced") }

        binding.btnContinue.setOnClickListener {
            val uid = auth.currentUser?.uid
            val level = selectedLevel
            if (uid == null || level == null) return@setOnClickListener

            binding.btnContinue.isEnabled = false
            val userRef = db.child("users").child(uid)

            // 1) Verificar si ya existe el usuario
            userRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    // Crear estructura inicial
                    val newUser = mapOf(
                        "createdAt" to ServerValue.TIMESTAMP,
                        "lastLoginAt" to ServerValue.TIMESTAMP,
                        "profile" to mapOf(
                            "fitnessLevel" to level,
                            "fitnessLevelUpdatedAt" to ServerValue.TIMESTAMP
                        )
                    )
                    userRef.setValue(newUser)
                        .addOnSuccessListener {
                            goNext() // 👈 avanzar
                        }
                        .addOnFailureListener { e ->
                            binding.btnContinue.isEnabled = true
                            Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                        }
                } else {
                    // Actualizar solo el perfil
                    val updates = mapOf(
                        "lastLoginAt" to ServerValue.TIMESTAMP,
                        "profile/fitnessLevel" to level,
                        "profile/fitnessLevelUpdatedAt" to ServerValue.TIMESTAMP
                    )
                    userRef.updateChildren(updates)
                        .addOnSuccessListener {
                            goNext() // 👈 avanzar
                        }
                        .addOnFailureListener { e ->
                            binding.btnContinue.isEnabled = true
                            Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                        }
                }
            }.addOnFailureListener { e ->
                binding.btnContinue.isEnabled = true
                Snackbar.make(binding.root, "Error de conexión: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }

        applySelectionUI(null)
    }

    private fun goNext() {
        // (Opcional) feedback rápido
        Snackbar.make(binding.root, "Estado guardado ✓", Snackbar.LENGTH_SHORT).show()

        // Navegar a la pantalla de “Mayor Obstáculo (texto)”
        startActivity(Intent(this, ObstacleTextActivity::class.java))
        finish() // cierra esta pantalla para no volver con back
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
