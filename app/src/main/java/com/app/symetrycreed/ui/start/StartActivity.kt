package com.app.symetrycreed.ui.start

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.databinding.ActivityStartBinding
import com.app.symetrycreed.ui.home.HolaMundoActivity
import com.app.symetrycreed.ui.login.MainActivity
import com.app.symetrycreed.ui.signup.SignUpActivity
import com.google.firebase.auth.FirebaseAuth

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 👉 Sign Up: registro con correo/contraseña
        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, com.app.symetrycreed.ui.login.SignInActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // Si ya hay sesión iniciada, ir directo a la home
        auth.currentUser?.let {
            startActivity(Intent(this, HolaMundoActivity::class.java))
            finish()
        }
    }
}
