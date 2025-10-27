package com.app.symetrycreed.ui.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.google.android.material.card.MaterialCardView

class HelpActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var cardFAQ: MaterialCardView
    private lateinit var cardContact: MaterialCardView
    private lateinit var cardTutorials: MaterialCardView
    private lateinit var cardTerms: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        cardFAQ = findViewById(R.id.cardFAQ)
        cardContact = findViewById(R.id.cardContact)
        cardTutorials = findViewById(R.id.cardTutorials)
        cardTerms = findViewById(R.id.cardTerms)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        cardFAQ.setOnClickListener {
            android.widget.Toast.makeText(
                this,
                "Preguntas Frecuentes (próximamente)",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        cardContact.setOnClickListener {
            // Abrir email
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:soporte@creedgym.com")
                putExtra(Intent.EXTRA_SUBJECT, "Ayuda - CreedGym")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                android.widget.Toast.makeText(
                    this,
                    "No se encontró una app de email",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        cardTutorials.setOnClickListener {
            android.widget.Toast.makeText(
                this,
                "Tutoriales (próximamente)",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        cardTerms.setOnClickListener {
            android.widget.Toast.makeText(
                this,
                "Términos y Condiciones (próximamente)",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}