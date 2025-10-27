package com.app.symetrycreed.ui.progress

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class ProgressActivity : AppCompatActivity() {

    private lateinit var tvTrainingsCount: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvGoalProgress: TextView

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        initViews()
        loadProgressData()
    }

    private fun initViews() {
        tvTrainingsCount = findViewById(R.id.tvTrainingsCount)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        tvCurrentStreak = findViewById(R.id.tvCurrentStreak)
        tvGoalProgress = findViewById(R.id.tvGoalProgress)
    }

    private fun loadProgressData() {
        val user = auth.currentUser ?: return
        val userRef = db.child("users").child(user.uid)

        // Cargar entrenamientos del mes actual
        userRef.child("trainings").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    tvTrainingsCount.text = "0"
                    tvTotalTime.text = "0h"
                    tvCurrentStreak.text = "0"
                    return
                }

                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)

                var monthCount = 0
                var totalMinutes = 0
                val allDates = mutableListOf<Long>()

                for (child in snapshot.children) {
                    try {
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: continue
                        val duration = child.child("duration").getValue(Int::class.java) ?: 30

                        allDates.add(timestamp)

                        val trainingCal = Calendar.getInstance()
                        trainingCal.timeInMillis = timestamp

                        if (trainingCal.get(Calendar.MONTH) == currentMonth &&
                            trainingCal.get(Calendar.YEAR) == currentYear) {
                            monthCount++
                            totalMinutes += duration
                        }
                    } catch (e: Exception) {
                        Log.e("ProgressActivity", "Error: ${e.message}")
                    }
                }

                tvTrainingsCount.text = monthCount.toString()

                val hours = totalMinutes / 60
                tvTotalTime.text = "${hours}h"

                // Calcular racha
                calculateStreak(allDates)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProgressActivity", "Error: ${error.message}")
            }
        })

        // Cargar progreso de objetivos
        calculateGoalProgress(user.uid)
    }

    private fun calculateStreak(dates: List<Long>) {
        if (dates.isEmpty()) {
            tvCurrentStreak.text = "0"
            return
        }

        val sortedDates = dates.sortedDescending()

        var streak = 0
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val yesterday = Calendar.getInstance()
        yesterday.timeInMillis = today.timeInMillis
        yesterday.add(Calendar.DAY_OF_YEAR, -1)

        var checkDate = Calendar.getInstance()
        checkDate.timeInMillis = sortedDates[0]
        checkDate.set(Calendar.HOUR_OF_DAY, 0)
        checkDate.set(Calendar.MINUTE, 0)
        checkDate.set(Calendar.SECOND, 0)
        checkDate.set(Calendar.MILLISECOND, 0)

        // Verificar si entrenó hoy o ayer
        if (checkDate.timeInMillis != today.timeInMillis &&
            checkDate.timeInMillis != yesterday.timeInMillis) {
            tvCurrentStreak.text = "0"
            return
        }

        val uniqueDays = mutableSetOf<Long>()
        for (timestamp in sortedDates) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            uniqueDays.add(cal.timeInMillis)
        }

        val sortedUniqueDays = uniqueDays.sorted().reversed()

        if (sortedUniqueDays.isEmpty()) {
            tvCurrentStreak.text = "0"
            return
        }

        var currentDate = Calendar.getInstance()
        currentDate.timeInMillis = sortedUniqueDays[0]

        // Si el último día no es hoy ni ayer, racha = 0
        if (currentDate.timeInMillis != today.timeInMillis &&
            currentDate.timeInMillis != yesterday.timeInMillis) {
            tvCurrentStreak.text = "0"
            return
        }

        streak = 1

        for (i in 1 until sortedUniqueDays.size) {
            val prevDate = Calendar.getInstance()
            prevDate.timeInMillis = sortedUniqueDays[i-1]

            val nextDate = Calendar.getInstance()
            nextDate.timeInMillis = sortedUniqueDays[i]

            val expectedDate = Calendar.getInstance()
            expectedDate.timeInMillis = prevDate.timeInMillis
            expectedDate.add(Calendar.DAY_OF_YEAR, -1)

            if (nextDate.timeInMillis == expectedDate.timeInMillis) {
                streak++
            } else {
                break
            }
        }

        tvCurrentStreak.text = streak.toString()
    }

    private fun calculateGoalProgress(uid: String) {
        val goalsRef = db.child("users").child(uid).child("goals")

        goalsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    tvGoalProgress.text = "0%"
                    return
                }

                var totalProgress = 0.0
                var goalCount = 0

                for (child in snapshot.children) {
                    try {
                        val current = child.child("current").getValue(Double::class.java) ?: 0.0
                        val target = child.child("target").getValue(Double::class.java) ?: 1.0

                        if (target > 0) {
                            val progress = (current / target) * 100
                            totalProgress += progress.coerceAtMost(100.0)
                            goalCount++
                        }
                    } catch (e: Exception) {
                        Log.e("ProgressActivity", "Error: ${e.message}")
                    }
                }

                val averageProgress = if (goalCount > 0) {
                    (totalProgress / goalCount).toInt()
                } else {
                    0
                }

                tvGoalProgress.text = "$averageProgress%"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProgressActivity", "Error: ${error.message}")
            }
        })
    }
}