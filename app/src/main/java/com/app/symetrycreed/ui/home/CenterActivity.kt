package com.app.symetrycreed.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.databinding.ActivityCenterBinding
import com.google.firebase.auth.FirebaseAuth

class CenterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCenterBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Leer usuario actual y mostrar nombre (o email si no hay nombre)
        val user = auth.currentUser
        val display = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email ?: "Usuario"

        // Con ViewBinding: asignar directamente
        binding.tvUsuario.text = display

        // Solo visual; sin listeners por ahora.
    }
}