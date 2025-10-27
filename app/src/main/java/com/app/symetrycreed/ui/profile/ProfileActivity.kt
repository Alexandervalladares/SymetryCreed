package com.app.symetrycreed.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.ui.goals.GoalsActivity

import com.app.symetrycreed.ui.login.SignInActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvTrainingsCount: TextView
    private lateinit var tvActiveDays: TextView
    private lateinit var tvAchievements: TextView

    private lateinit var cardEditProfile: MaterialCardView
    private lateinit var cardObjectives: MaterialCardView
    private lateinit var cardAchievements: MaterialCardView
    private lateinit var cardSettings: MaterialCardView
    private lateinit var cardHelp: MaterialCardView
    private lateinit var cardLogout: MaterialCardView

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        loadUserData()
        setupClickListeners()
    }

    private fun initViews() {
        // Header info
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvTrainingsCount = findViewById(R.id.tvTrainingsCount)
        tvActiveDays = findViewById(R.id.tvActiveDays)
        tvAchievements = findViewById(R.id.tvAchievementsCount)

        // Cards de opciones
        cardEditProfile = findViewById(R.id.cardEditProfile)
        cardObjectives = findViewById(R.id.cardObjectives)
        cardAchievements = findViewById(R.id.cardAchievements)
        cardSettings = findViewById(R.id.cardSettings)
        cardHelp = findViewById(R.id.cardHelp)
        cardLogout = findViewById(R.id.cardLogout)
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user == null) {
            android.widget.Toast.makeText(this, "No estás autenticado", android.widget.Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Mostrar info básica del usuario
        tvUserName.text = user.displayName ?: "Usuario"
        tvUserEmail.text = user.email ?: ""

        // Cargar estadísticas
        loadUserStats(user.uid)
    }

    private fun loadUserStats(uid: String) {
        val userRef = db.child("users").child(uid)

        // Contador de entrenamientos
        userRef.child("trainings").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                tvTrainingsCount.text = count.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                tvTrainingsCount.text = "0"
            }
        })

        // Calcular días activos (días con al menos 1 entrenamiento)
        userRef.child("trainings").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uniqueDays = mutableSetOf<String>()

                for (child in snapshot.children) {
                    try {
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: continue
                        val calendar = java.util.Calendar.getInstance()
                        calendar.timeInMillis = timestamp
                        val dayKey = "${calendar.get(java.util.Calendar.YEAR)}-" +
                                "${calendar.get(java.util.Calendar.MONTH)}-" +
                                "${calendar.get(java.util.Calendar.DAY_OF_MONTH)}"
                        uniqueDays.add(dayKey)
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileActivity", "Error: ${e.message}")
                    }
                }

                tvActiveDays.text = uniqueDays.size.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                tvActiveDays.text = "0"
            }
        })

        // Contador de logros (objetivos completados)
        userRef.child("goals").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var completedGoals = 0

                for (child in snapshot.children) {
                    try {
                        val current = child.child("current").getValue(Double::class.java) ?: 0.0
                        val target = child.child("target").getValue(Double::class.java) ?: 1.0

                        if (current >= target) {
                            completedGoals++
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileActivity", "Error: ${e.message}")
                    }
                }

                tvAchievements.text = completedGoals.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                tvAchievements.text = "0"
            }
        })
    }

    private fun setupClickListeners() {
        // Editar Perfil
        cardEditProfile.setOnClickListener {
            startActivity(Intent(this, com.app.symetrycreed.ui.profile.EditProfileActivity::class.java))
        }

        // Objetivos
        cardObjectives.setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }

        // Logros
        cardAchievements.setOnClickListener {
            startActivity(Intent(this, com.app.symetrycreed.ui.achievements.AchievementsActivity::class.java))
        }

        // Configuración
        cardSettings.setOnClickListener {
            startActivity(Intent(this, com.app.symetrycreed.ui.settings.SettingsActivity::class.java))
        }

        // Ayuda
        cardHelp.setOnClickListener {
            startActivity(Intent(this, com.app.symetrycreed.ui.help.HelpActivity::class.java))
        }

        // Cerrar Sesión
        cardLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logout() {
        // Cerrar sesión en Firebase
        auth.signOut()

        // Ir a LoginActivity y limpiar el stack
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        android.widget.Toast.makeText(this, "Sesión cerrada", android.widget.Toast.LENGTH_SHORT).show()
    }
}