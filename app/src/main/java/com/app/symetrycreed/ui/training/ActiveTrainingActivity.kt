package com.app.symetrycreed.ui.training

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.model.Exercise
import com.app.symetrycreed.model.Training
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ActiveTrainingActivity : AppCompatActivity() {

    private lateinit var tvTrainingTitle: TextView
    private lateinit var tvExerciseCounter: TextView
    private lateinit var tvCurrentExerciseName: TextView
    private lateinit var tvCurrentSeries: TextView
    private lateinit var tvCurrentReps: TextView
    private lateinit var tvCurrentWeight: TextView
    private lateinit var tvRestTimer: TextView
    private lateinit var containerSeries: LinearLayout
    private lateinit var btnCompleteSet: MaterialButton
    private lateinit var btnPreviousExercise: MaterialButton
    private lateinit var btnNextExercise: MaterialButton
    private lateinit var btnFinishTraining: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var training: Training
    private var currentExerciseIndex = 0
    private var currentSetIndex = 0
    private var restTimer: CountDownTimer? = null
    private var isResting = false

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    // Para trackear sets completados por ejercicio
    private val completedSets = mutableMapOf<Int, MutableSet<Int>>() // exerciseIndex -> set of completed set indices

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_training)

        // Obtener el training del intent
        training = intent.getParcelableExtra("training") ?: run {
            Toast.makeText(this, "Error: No se encontró el entrenamiento", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupClickListeners()
        setupBackPressHandler()
        loadExercise()
    }

    private fun initViews() {
        tvTrainingTitle = findViewById(R.id.tvTrainingTitle)
        tvExerciseCounter = findViewById(R.id.tvExerciseCounter)
        tvCurrentExerciseName = findViewById(R.id.tvCurrentExerciseName)
        tvCurrentSeries = findViewById(R.id.tvCurrentSeries)
        tvCurrentReps = findViewById(R.id.tvCurrentReps)
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight)
        tvRestTimer = findViewById(R.id.tvRestTimer)
        containerSeries = findViewById(R.id.containerSeries)
        btnCompleteSet = findViewById(R.id.btnCompleteSet)
        btnPreviousExercise = findViewById(R.id.btnPreviousExercise)
        btnNextExercise = findViewById(R.id.btnNextExercise)
        btnFinishTraining = findViewById(R.id.btnFinishTraining)
        progressBar = findViewById(R.id.progressBar)

        tvTrainingTitle.text = training.title
    }

    private fun setupClickListeners() {
        btnCompleteSet.setOnClickListener {
            completeCurrentSet()
        }

        btnPreviousExercise.setOnClickListener {
            if (currentExerciseIndex > 0) {
                currentExerciseIndex--
                currentSetIndex = 0
                loadExercise()
            }
        }

        btnNextExercise.setOnClickListener {
            goToNextExercise()
        }

        btnFinishTraining.setOnClickListener {
            showFinishDialog()
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@ActiveTrainingActivity)
                    .setTitle("Salir")
                    .setMessage("¿Estás seguro de que deseas salir?\n\nTu progreso no se guardará.")
                    .setPositiveButton("Salir") { _, _ ->
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                    .setNegativeButton("Continuar", null)
                    .show()
            }
        })
    }

    private fun loadExercise() {
        if (currentExerciseIndex >= training.exercises.size) {
            // Todos los ejercicios completados
            finishTraining()
            return
        }

        val exercise = training.exercises[currentExerciseIndex]

        // Actualizar UI
        tvExerciseCounter.text = "${currentExerciseIndex + 1}/${training.exercises.size}"
        tvCurrentExerciseName.text = exercise.name
        tvCurrentSeries.text = "Series: ${exercise.series}"
        tvCurrentReps.text = "Reps: ${exercise.reps}"

        val weightText = if (exercise.weightKg > 0) "${exercise.weightKg}kg" else "Peso corporal"
        tvCurrentWeight.text = weightText

        // Actualizar progreso
        val totalSets = training.exercises.sumOf { it.series }
        val completedSetsCount = completedSets.values.sumOf { it.size }
        val progress = ((completedSetsCount.toFloat() / totalSets) * 100).toInt()
        progressBar.progress = progress

        // Inicializar sets completados si no existe
        if (!completedSets.containsKey(currentExerciseIndex)) {
            completedSets[currentExerciseIndex] = mutableSetOf()
        }

        // Renderizar indicadores de series
        renderSeriesIndicators(exercise)

        // Actualizar botones
        btnPreviousExercise.isEnabled = currentExerciseIndex > 0
        updateCompleteSetButton(exercise)

        // Cancelar timer si existe
        stopRestTimer()
    }

    private fun renderSeriesIndicators(exercise: Exercise) {
        containerSeries.removeAllViews()

        val completedSetsForExercise = completedSets[currentExerciseIndex] ?: mutableSetOf()

        for (i in 0 until exercise.series) {
            val indicator = LayoutInflater.from(this).inflate(
                R.layout.item_set_indicator,
                containerSeries,
                false
            )

            val card = indicator.findViewById<MaterialCardView>(R.id.cardSetIndicator)
            val tvSetNumber = indicator.findViewById<TextView>(R.id.tvSetNumber)
            val iconCheck = indicator.findViewById<ImageView>(R.id.iconCheck)

            tvSetNumber.text = (i + 1).toString()

            if (completedSetsForExercise.contains(i)) {
                // Set completado
                card.setCardBackgroundColor(getColor(R.color.green_completed))
                iconCheck.visibility = ImageView.VISIBLE
            } else if (i == currentSetIndex) {
                // Set actual
                card.setCardBackgroundColor(getColor(R.color.red_accent))
                iconCheck.visibility = ImageView.GONE
            } else {
                // Set pendiente
                card.setCardBackgroundColor(getColor(R.color.gray_box))
                iconCheck.visibility = ImageView.GONE
            }

            containerSeries.addView(indicator)
        }
    }

    private fun updateCompleteSetButton(exercise: Exercise) {
        val completedSetsForExercise = completedSets[currentExerciseIndex] ?: mutableSetOf()

        if (completedSetsForExercise.size >= exercise.series) {
            // Todas las series completadas
            btnCompleteSet.text = "Siguiente Ejercicio"
            btnCompleteSet.setIconResource(android.R.drawable.ic_media_next)
        } else {
            btnCompleteSet.text = "Completar Serie ${currentSetIndex + 1}"
            btnCompleteSet.setIconResource(android.R.drawable.checkbox_on_background)
        }
    }

    private fun completeCurrentSet() {
        val exercise = training.exercises[currentExerciseIndex]
        val completedSetsForExercise = completedSets[currentExerciseIndex] ?: mutableSetOf()

        if (completedSetsForExercise.size >= exercise.series) {
            // Ya se completaron todas las series, ir al siguiente ejercicio
            goToNextExercise()
            return
        }

        // Marcar el set actual como completado
        completedSetsForExercise.add(currentSetIndex)
        completedSets[currentExerciseIndex] = completedSetsForExercise

        // Vibrar para feedback
        vibratePhone(100)

        // Buscar el siguiente set no completado
        var nextSetIndex = -1
        for (i in 0 until exercise.series) {
            if (!completedSetsForExercise.contains(i)) {
                nextSetIndex = i
                break
            }
        }

        if (nextSetIndex != -1) {
            // Hay más sets por completar
            currentSetIndex = nextSetIndex

            // Iniciar descanso si está configurado
            val restTime = exercise.restSec ?: 60
            if (restTime > 0) {
                startRestTimer(restTime)
            }

            renderSeriesIndicators(exercise)
            updateCompleteSetButton(exercise)
        } else {
            // Todas las series completadas
            Toast.makeText(this, "✓ Ejercicio completado", Toast.LENGTH_SHORT).show()
            renderSeriesIndicators(exercise)
            updateCompleteSetButton(exercise)
        }

        // Actualizar progreso
        val totalSets = training.exercises.sumOf { it.series }
        val completedSetsCount = completedSets.values.sumOf { it.size }
        val progress = ((completedSetsCount.toFloat() / totalSets) * 100).toInt()
        progressBar.progress = progress
    }

    private fun vibratePhone(durationMs: Long) {
        try {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    android.os.VibrationEffect.createOneShot(
                        durationMs,
                        android.os.VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (e: Exception) {
            android.util.Log.e("ActiveTraining", "Error al vibrar: ${e.message}")
        }
    }

    private fun startRestTimer(seconds: Int) {
        isResting = true
        tvRestTimer.visibility = TextView.VISIBLE

        restTimer = object : CountDownTimer(seconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                tvRestTimer.text = "Descanso: ${secondsLeft}s"
            }

            override fun onFinish() {
                tvRestTimer.visibility = TextView.GONE
                tvRestTimer.text = ""
                isResting = false

                // Notificar que el descanso terminó
                vibratePhone(300)
                Toast.makeText(this@ActiveTrainingActivity, "¡Descanso terminado!", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun stopRestTimer() {
        restTimer?.cancel()
        tvRestTimer.visibility = TextView.GONE
        tvRestTimer.text = ""
        isResting = false
    }

    private fun goToNextExercise() {
        stopRestTimer()

        if (currentExerciseIndex < training.exercises.size - 1) {
            currentExerciseIndex++
            currentSetIndex = 0
            loadExercise()
        } else {
            // Último ejercicio, mostrar diálogo de finalización
            showFinishDialog()
        }
    }

    private fun showFinishDialog() {
        val totalSets = training.exercises.sumOf { it.series }
        val completedSetsCount = completedSets.values.sumOf { it.size }
        val completionPercentage = ((completedSetsCount.toFloat() / totalSets) * 100).toInt()

        AlertDialog.Builder(this)
            .setTitle("Finalizar Entrenamiento")
            .setMessage("Has completado $completedSetsCount de $totalSets series ($completionPercentage%).\n\n¿Deseas finalizar el entrenamiento?")
            .setPositiveButton("Finalizar") { _, _ ->
                finishTraining()
            }
            .setNegativeButton("Continuar", null)
            .show()
    }

    private fun finishTraining() {
        stopRestTimer()

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Marcar ejercicios como completados
        training.exercises.forEachIndexed { index, exercise ->
            val completedSetsForExercise = completedSets[index] ?: mutableSetOf()
            exercise.completed = completedSetsForExercise.size >= exercise.series
        }

        // Actualizar en Firebase
        val trainingRef = db.child("users").child(user.uid).child("trainings").child(training.id)

        // Actualizar campos principales
        trainingRef.child("completedAt").setValue(System.currentTimeMillis())
        trainingRef.child("duration").setValue(calculateDuration())

        // Actualizar estado de ejercicios
        training.exercises.forEach { exercise ->
            trainingRef.child("exercises").child(exercise.id).child("completed").setValue(exercise.completed)
        }

        // Actualizar estadísticas del usuario
        updateUserStats(user.uid)

        Toast.makeText(this, "🎉 ¡Entrenamiento completado!", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun calculateDuration(): Int {
        // Calcular duración aproximada basada en los ejercicios y descansos
        val totalSets = training.exercises.sumOf { it.series }
        val avgRestTime = 60 // segundos promedio de descanso
        val avgSetTime = 30 // segundos promedio por set

        return ((totalSets * avgSetTime) + ((totalSets - 1) * avgRestTime)) / 60 // minutos
    }

    private fun updateUserStats(uid: String) {
        val statsRef = db.child("users").child(uid).child("stats")

        statsRef.child("trainings").get().addOnSuccessListener { snapshot ->
            val currentCount = snapshot.getValue(Int::class.java) ?: 0
            val updates = hashMapOf<String, Any>(
                "trainings" to (currentCount + 1),
                "lastTrainingAt" to System.currentTimeMillis()
            )
            statsRef.updateChildren(updates)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRestTimer()
    }
}