package com.app.symetrycreed.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.databinding.ActivityObstacleTextBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import android.content.Intent

class ObstacleTextActivity : AppCompatActivity() {

    private lateinit var binding: ActivityObstacleTextBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObstacleTextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnContinue.isEnabled = false

        // Habilitar botón cuando hay texto
        binding.etObstacle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnContinue.isEnabled = !s.isNullOrBlank()
            }
        })

        binding.btnContinue.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val text = binding.etObstacle.text?.toString()?.trim().orEmpty()
            if (text.isBlank()) return@setOnClickListener

            binding.btnContinue.isEnabled = false

            val updates = mapOf(
                "obstacleText" to text,
                "updatedAt" to ServerValue.TIMESTAMP
            )

            db.child("users").child(uid).child("profile")
                .updateChildren(updates)
                .addOnCompleteListener { task ->
                    binding.btnContinue.isEnabled = true
                    if (task.isSuccessful) {
                        // Feedback opcional
                        Snackbar.make(binding.root, "Guardado ✓", Snackbar.LENGTH_SHORT).show()
                        // Ir a Información Personal
                        startActivity(Intent(this, com.app.symetrycreed.ui.profile.PersonalInfoActivity::class.java))
                        finish()
                    } else {
                        Snackbar.make(binding.root, "Error al guardar", Snackbar.LENGTH_LONG).show()
                    }
                }
        }
    }
}
