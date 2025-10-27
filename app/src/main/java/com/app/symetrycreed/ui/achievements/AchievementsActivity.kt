package com.app.symetrycreed.ui.achievements

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView

class AchievementsActivity : AppCompatActivity() {

    private lateinit var tvUnlockedCount: TextView
    private lateinit var container: LinearLayout
    private lateinit var btnBack: ImageView

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        initViews()
        loadAchievements()
        setupClickListeners()
    }

    private fun initViews() {
        tvUnlockedCount = findViewById(R.id.tvUnlockedCount)
        container = findViewById(R.id.containerAchievements)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadAchievements() {
        val user = auth.currentUser ?: return

        // Logros predefinidos
        val achievements = listOf(
            Achievement(
                "Primera Victoria",
                "Completa tu primer entrenamiento",
                "Desbloqueado el 15 Ene 2024",
                true
            ),
            Achievement(
                "Constancia",
                "Entrena 7 días seguidos",
                "Desbloqueado el 22 Ene 2024",
                true
            ),
            Achievement(
                "Fuerza Imparable",
                "Completa 50 entrenamientos de fuerza",
                "23/50",
                false
            ),
            Achievement(
                "Maratonista",
                "Corre un total de 100km",
                "45/100",
                false
            ),
            Achievement(
                "Dedicación Total",
                "Entrena 30 días en un mes",
                "18/30",
                false
            )
        )

        val unlockedCount = achievements.count { it.isUnlocked }
        tvUnlockedCount.text = unlockedCount.toString()

        for (achievement in achievements) {
            addAchievementCard(achievement)
        }
    }

    private fun addAchievementCard(achievement: Achievement) {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.item_achievement_card, container, false)

        val icon = cardView.findViewById<ImageView>(R.id.imgAchievementIcon)
        val tvTitle = cardView.findViewById<TextView>(R.id.tvAchievementTitle)
        val tvDescription = cardView.findViewById<TextView>(R.id.tvAchievementDescription)
        val tvProgress = cardView.findViewById<TextView>(R.id.tvAchievementProgress)

        tvTitle.text = achievement.title
        tvDescription.text = achievement.description
        tvProgress.text = achievement.progress

        if (achievement.isUnlocked) {
            icon.setColorFilter(getColor(android.R.color.holo_blue_light))
            tvProgress.setTextColor(getColor(android.R.color.holo_blue_light))
        } else {
            icon.setColorFilter(getColor(R.color.textSecondary))
            tvProgress.setTextColor(getColor(R.color.textSecondary))
            icon.alpha = 0.5f
        }

        container.addView(cardView)
    }

    data class Achievement(
        val title: String,
        val description: String,
        val progress: String,
        val isUnlocked: Boolean
    )
}