package com.app.symetrycreed.ui.training

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TimerActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var btnPlayPause: FloatingActionButton
    private lateinit var btnReset: FloatingActionButton
    private lateinit var btn5min: MaterialButton
    private lateinit var btn10min: MaterialButton
    private lateinit var btn15min: MaterialButton

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 15 * 60 * 1000L // 15 minutos por defecto
    private var initialTime: Long = 15 * 60 * 1000L
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        initViews()
        updateTimerText()
        setupListeners()
    }

    private fun initViews() {
        tvTimer = findViewById(R.id.tvTimer)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnReset = findViewById(R.id.btnReset)
        btn5min = findViewById(R.id.btn5min)
        btn10min = findViewById(R.id.btn10min)
        btn15min = findViewById(R.id.btn15min)
    }

    private fun setupListeners() {
        // Play/Pause
        btnPlayPause.setOnClickListener {
            android.util.Log.d("TimerActivity", "Play/Pause clicked, isRunning: $isRunning")
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        // Reset
        btnReset.setOnClickListener {
            android.util.Log.d("TimerActivity", "Reset clicked")
            resetTimer()
        }

        // Botones de tiempo
        btn5min.setOnClickListener {
            android.util.Log.d("TimerActivity", "5 min clicked")
            setTime(5 * 60 * 1000L)
            highlightButton(btn5min)
        }

        btn10min.setOnClickListener {
            android.util.Log.d("TimerActivity", "10 min clicked")
            setTime(10 * 60 * 1000L)
            highlightButton(btn10min)
        }

        btn15min.setOnClickListener {
            android.util.Log.d("TimerActivity", "15 min clicked")
            setTime(15 * 60 * 1000L)
            highlightButton(btn15min)
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
                android.util.Log.d("TimerActivity", "Timer finished")
                // Aquí puedes agregar sonido o vibración
            }
        }.start()

        isRunning = true
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
        android.util.Log.d("TimerActivity", "Timer started")
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isRunning = false
        btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
        android.util.Log.d("TimerActivity", "Timer paused")
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isRunning = false
        timeLeftInMillis = initialTime
        updateTimerText()
        btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
        android.util.Log.d("TimerActivity", "Timer reset")
    }

    private fun setTime(millis: Long) {
        if (!isRunning) {
            initialTime = millis
            timeLeftInMillis = millis
            updateTimerText()
            android.util.Log.d("TimerActivity", "Time set to ${millis / 1000} seconds")
        }
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000 / 60).toInt()
        val seconds = (timeLeftInMillis / 1000 % 60).toInt()
        tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun highlightButton(selected: MaterialButton) {
        // Reset todos los botones
        btn5min.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.gray_box))
        btn10min.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.gray_box))
        btn15min.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.gray_box))

        // Highlight el seleccionado
        selected.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.red_accent))
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}