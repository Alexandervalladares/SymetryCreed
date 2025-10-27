package com.app.symetrycreed.ui.settings

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var switchPushNotifications: SwitchMaterial
    private lateinit var switchSounds: SwitchMaterial
    private lateinit var switchDataSync: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        switchPushNotifications = findViewById(R.id.switchPushNotifications)
        switchSounds = findViewById(R.id.switchSounds)
        switchDataSync = findViewById(R.id.switchDataSync)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        switchPushNotifications.setOnCheckedChangeListener { _, isChecked ->
            android.widget.Toast.makeText(
                this,
                if (isChecked) "Notificaciones activadas" else "Notificaciones desactivadas",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        switchSounds.setOnCheckedChangeListener { _, isChecked ->
            android.widget.Toast.makeText(
                this,
                if (isChecked) "Sonidos activados" else "Sonidos desactivados",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        switchDataSync.setOnCheckedChangeListener { _, isChecked ->
            android.widget.Toast.makeText(
                this,
                if (isChecked) "Sincronización activada" else "Sincronización desactivada",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}