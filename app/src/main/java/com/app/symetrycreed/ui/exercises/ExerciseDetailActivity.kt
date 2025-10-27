package com.app.symetrycreed.ui.exercises

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExerciseDetailActivity : AppCompatActivity() {

    private lateinit var tvExerciseName: TextView
    private lateinit var tvExerciseMuscle: TextView
    private lateinit var tvExerciseEquipment: TextView
    private lateinit var tvExerciseLevel: TextView
    private lateinit var tvExerciseDescription: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnPlayPause: FloatingActionButton
    private lateinit var btnReset: FloatingActionButton

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 30000L
    private var initialTime: Long = 30000L
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_detail)

        initViews()
        loadExerciseData()
        setupTimer()
    }

    private fun initViews() {
        tvExerciseName = findViewById(R.id.tvExerciseName)
        tvExerciseMuscle = findViewById(R.id.tvExerciseMuscle)
        tvExerciseEquipment = findViewById(R.id.tvExerciseEquipment)
        tvExerciseLevel = findViewById(R.id.tvExerciseLevel)
        tvExerciseDescription = findViewById(R.id.tvExerciseDescription)
        tvTimer = findViewById(R.id.tvTimer)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnReset = findViewById(R.id.btnReset)
    }

    private fun loadExerciseData() {
        val name = intent.getStringExtra("exercise_name") ?: "Ejercicio"
        val muscle = intent.getStringExtra("exercise_muscle") ?: ""
        val equipment = intent.getStringExtra("exercise_equipment") ?: ""
        val level = intent.getStringExtra("exercise_level") ?: ""
        val description = intent.getStringExtra("exercise_description") ?: ""
        val duration = intent.getIntExtra("exercise_duration", 30)

        tvExerciseName.text = name
        tvExerciseMuscle.text = "🎯 Músculo: $muscle"
        tvExerciseEquipment.text = "🏋️ Equipo: $equipment"
        tvExerciseLevel.text = "Nivel: $level"
        tvExerciseDescription.text = description

        // Configurar timer con duración recomendada
        initialTime = duration * 1000L
        timeLeftInMillis = initialTime
        updateTimerText()

        // Color según nivel
        val levelColor = when(level) {
            "Básico" -> getColor(android.R.color.holo_green_light)
            "Intermedio" -> getColor(android.R.color.holo_orange_light)
            "Avanzado" -> getColor(android.R.color.holo_red_light)
            else -> getColor(R.color.textSecondary)
        }
        tvExerciseLevel.setTextColor(levelColor)
    }

    private fun setupTimer() {
        btnPlayPause.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        btnReset.setOnClickListener {
            resetTimer()
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                isRunning = false
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                // Vibrar o sonar
                android.widget.Toast.makeText(this@ExerciseDetailActivity, "¡Tiempo completado! 🎉", android.widget.Toast.LENGTH_SHORT).show()
            }
        }.start()

        isRunning = true
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isRunning = false
        btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isRunning = false
        timeLeftInMillis = initialTime
        updateTimerText()
        btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000 / 60).toInt()
        val seconds = (timeLeftInMillis / 1000 % 60).toInt()
        tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}