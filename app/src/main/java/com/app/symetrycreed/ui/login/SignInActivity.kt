package com.app.symetrycreed.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.databinding.ActivitySignInBinding
import com.app.symetrycreed.ui.home.CenterActivity
import com.app.symetrycreed.ui.signup.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Google → reutiliza tu flujo existente
        binding.btnGoogle.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Enter en password
        binding.etPass.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                signIn()
                true
            } else false
        }

        binding.btnSignIn.setOnClickListener { signIn() }

        // Olvidé contraseña
        binding.tvForgot.setOnClickListener { sendReset() }

        // Ir a registro
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun signIn() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val pass  = binding.etPass.text?.toString()?.trim().orEmpty()

        binding.tilEmail.error = null
        binding.tilPass.error = null

        var valid = true
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Correo inválido"; valid = false
        }
        if (pass.isEmpty()) {
            binding.tilPass.error = "Requerido"; valid = false
        }
        if (!valid) return

        binding.btnSignIn.isEnabled = false

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                binding.btnSignIn.isEnabled = true
                if (task.isSuccessful) {
                    // Usuario ya existente: solo actualizar lastLoginAt y navegar a CenterActivity
                    auth.currentUser?.uid?.let { uid ->
                        db.child("users").child(uid)
                            .updateChildren(mapOf("lastLoginAt" to ServerValue.TIMESTAMP))
                            .addOnCompleteListener {
                                startActivity(Intent(this, CenterActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                // Aunque falle la actualización, vamos al centro
                                startActivity(Intent(this, CenterActivity::class.java))
                                finish()
                            }
                    } ?: run {
                        // Si por alguna razón no hay uid, ir al centro de todas formas
                        startActivity(Intent(this, CenterActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Credenciales inválidas",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun sendReset() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            android.widget.Toast.makeText(this, "Escribe tu correo para enviar el enlace", android.widget.Toast.LENGTH_LONG).show()
            return
        }
        com.google.firebase.auth.FirebaseAuth.getInstance()
            .sendPasswordResetEmail(email)
            .addOnSuccessListener {
                android.widget.Toast.makeText(this, "Te enviamos un enlace para restablecer tu contraseña", android.widget.Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                android.widget.Toast.makeText(this, "No se pudo enviar: ${it.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
            }
    }
}