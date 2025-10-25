package com.app.symetrycreed.ui.training

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R

class TimerActivity : AppCompatActivity() {

    private lateinit var tvTime: TextView
    private lateinit var btnStartStop: Button
    private var timer: CountDownTimer? = null
    private var running = false
    private var millisLeft: Long = 15 * 60 * 1000L // 15 minutos por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        tvTime = findViewById(R.id.tvTimer)
        btnStartStop = findViewById(R.id.btnStartStop)

        updateUi()

        btnStartStop.setOnClickListener {
            if (running) stopTimer() else startTimer()
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(millisLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                millisLeft = millisUntilFinished
                updateUi()
            }

            override fun onFinish() {
                running = false
                millisLeft = 0
                updateUi()
            }
        }.start()
        running = true
        updateUi()
    }

    private fun stopTimer() {
        timer?.cancel()
        running = false
        updateUi()
    }

    private fun updateUi() {
        val totalSeconds = millisLeft / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        tvTime.text = String.format("%02d:%02d", minutes, seconds)
        btnStartStop.text = if (running) "Detener" else "Iniciar"
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}