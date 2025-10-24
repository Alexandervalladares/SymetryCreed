package com.app.symetrycreed.ui.signup

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.databinding.ActivitySignUpBinding
import com.app.symetrycreed.ui.home.HolaMundoActivity
import com.app.symetrycreed.ui.login.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.tvLogin.setOnClickListener {
            // Ir al flujo de login (puedes tener otra pantalla si quieres)
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.btnGoogle.setOnClickListener {
            // Reutiliza tu login con Google existente
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Enviar con IME Done en confirm
        binding.etConfirm.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createAccount()
                true
            } else false
        }

        binding.btnCreate.setOnClickListener { createAccount() }
    }

    private fun createAccount() {
        val name = binding.etName.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val pass = binding.etPass.text?.toString()?.trim().orEmpty()
        val confirm = binding.etConfirm.text?.toString()?.trim().orEmpty()

        clearErrors()

        var valid = true
        if (name.isEmpty()) {
            binding.tilName.error = "Requerido"
            valid = false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Correo inválido"
            valid = false
        }
        if (pass.length < 6) {
            binding.tilPass.error = "Mínimo 6 caracteres"
            valid = false
        }
        if (pass != confirm) {
            binding.tilConfirm.error = "No coincide"
            valid = false
        }
        if (!valid) return

        binding.btnCreate.isEnabled = false

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                binding.btnCreate.isEnabled = true
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userRef = db.child("users").child(uid)
                    val data = mapOf(
                        "uid" to uid,
                        "name" to name,
                        "email" to email,
                        "photoUrl" to "",
                        "provider" to "password",
                        "createdAt" to ServerValue.TIMESTAMP,
                        "lastLoginAt" to ServerValue.TIMESTAMP
                    )
                    userRef.setValue(data).addOnCompleteListener {
                        goHome()
                    }
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Error creando cuenta",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPass.error = null
        binding.tilConfirm.error = null
    }

    private fun goHome() {
        startActivity(Intent(this, HolaMundoActivity::class.java))
        finish()
    }
}
