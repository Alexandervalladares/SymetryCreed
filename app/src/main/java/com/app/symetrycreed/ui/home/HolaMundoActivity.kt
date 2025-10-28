package com.app.symetrycreed.ui.home
import android.content.Intent
import com.app.symetrycreed.ui.profile.FitnessLevelActivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.databinding.ActivityHolaMundoBinding
import com.google.firebase.auth.FirebaseAuth

class HolaMundoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHolaMundoBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHolaMundoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = auth.currentUser
        val nombre = user?.displayName ?: "Entrenador"

        binding.tvBienvenida.text = "¡Bienvenido a CreedGym, $nombre!"
        binding.tvDescripcion.text =
            "Vamos a configurar tu perfil para crear entrenamientos personalizados"

        binding.btnComenzar.setOnClickListener {
            startActivity(Intent(this, FitnessLevelActivity::class.java))
        }
    }
}