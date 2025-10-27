package com.app.symetrycreed.ui.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.model.Exercise
import com.app.symetrycreed.model.Training
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ExercisesActivity : AppCompatActivity() {

    private lateinit var containerExercises: LinearLayout
    private lateinit var btnAddExercise: Button
    private lateinit var btnSaveTraining: View
    private lateinit var edtTrainingTitle: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        initViews()
        loadTrainingData()
        setupListeners()
    }

    private fun initViews() {
        containerExercises = findViewById(R.id.containerExercises)
        btnAddExercise = findViewById(R.id.btnAddExercise)
        btnSaveTraining = findViewById(R.id.btnSaveTraining)
        edtTrainingTitle = findViewById(R.id.edtTrainingTitle)
    }

    private fun loadTrainingData() {
        val trainingExtra = intent.getParcelableExtra<Training>("training")
        if (trainingExtra != null) {
            edtTrainingTitle.setText(trainingExtra.title)
            for (ex in trainingExtra.exercises) {
                addExerciseView(prefill = ex)
            }
        } else {
            addExerciseView()
        }
    }

    private fun setupListeners() {
        btnAddExercise.setOnClickListener {
            addExerciseView()
        }

        btnSaveTraining.setOnClickListener {
            saveTrainingToFirebase()
        }
    }

    private fun addExerciseView(prefill: Exercise? = null) {
        val inflater = LayoutInflater.from(this)
        val item = inflater.inflate(R.layout.exercise_item, containerExercises, false)

        val edtName = item.findViewById<EditText>(R.id.edtExerciseName)
        val tvSeries = item.findViewById<TextView>(R.id.tvSeries)
        val tvReps = item.findViewById<TextView>(R.id.tvReps)
        val btnSeriesPlus = item.findViewById<Button>(R.id.btnSeriesPlus)
        val btnSeriesMinus = item.findViewById<Button>(R.id.btnSeriesMinus)
        val btnRepsPlus = item.findViewById<Button>(R.id.btnRepsPlus)
        val btnRepsMinus = item.findViewById<Button>(R.id.btnRepsMinus)
        val edtWeight = item.findViewById<EditText>(R.id.edtWeight)
        val btnRemove = item.findViewById<TextView>(R.id.btnRemoveExercise)

        if (prefill != null) {
            edtName.setText(prefill.name)
            tvSeries.text = prefill.series.toString()
            tvReps.text = prefill.reps.toString()
            edtWeight.setText(prefill.weightKg.toString())
        } else {
            tvSeries.text = "3"
            tvReps.text = "10"
            edtWeight.setText("0")
        }

        fun safeParseInt(tv: TextView): Int = tv.text.toString().toIntOrNull() ?: 0

        btnSeriesPlus.setOnClickListener {
            var v = safeParseInt(tvSeries)
            if (v < 20) v++
            tvSeries.text = v.toString()
        }
        btnSeriesMinus.setOnClickListener {
            var v = safeParseInt(tvSeries)
            if (v > 1) v--
            tvSeries.text = v.toString()
        }
        btnRepsPlus.setOnClickListener {
            var v = safeParseInt(tvReps)
            if (v < 1000) v++
            tvReps.text = v.toString()
        }
        btnRepsMinus.setOnClickListener {
            var v = safeParseInt(tvReps)
            if (v > 1) v--
            tvReps.text = v.toString()
        }

        btnRemove.setOnClickListener {
            containerExercises.removeView(item)
        }

        containerExercises.addView(item)
        containerExercises.post {
            (containerExercises.parent as? ScrollView)?.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun saveTrainingToFirebase() {
        val title = edtTrainingTitle.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Ingresa el título del entrenamiento", Toast.LENGTH_SHORT).show()
            return
        }
        if (title.length > 80) {
            Toast.makeText(this, "El título es muy largo (máx 80 caracteres)", Toast.LENGTH_SHORT).show()
            return
        }

        val exercises = mutableListOf<Exercise>()
        for (i in 0 until containerExercises.childCount) {
            val item = containerExercises.getChildAt(i)
            val name = (item.findViewById<View>(R.id.edtExerciseName) as EditText)
                .text.toString().trim()
            val series = (item.findViewById<View>(R.id.tvSeries) as TextView)
                .text.toString().toIntOrNull() ?: 0
            val reps = (item.findViewById<View>(R.id.tvReps) as TextView)
                .text.toString().toIntOrNull() ?: 0
            val weightStr = (item.findViewById<View>(R.id.edtWeight) as EditText)
                .text.toString().trim()
            val weight = weightStr.toDoubleOrNull() ?: 0.0

            if (name.isBlank()) {
                Toast.makeText(this, "Llena el nombre de todos los ejercicios", Toast.LENGTH_SHORT).show()
                return
            }

            val ex = Exercise(
                id = "",
                name = name,
                series = series,
                reps = reps,
                weightKg = weight
            )

            if (!ex.isValid()) {
                Toast.makeText(this, "Ejercicio '$name' tiene datos inválidos", Toast.LENGTH_SHORT).show()
                return
            }

            exercises.add(ex)
        }

        if (exercises.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un ejercicio", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }

        val dbRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(user.uid)
            .child("trainings")

        val newRef = dbRef.push()
        val trainingId = newRef.key ?: return

        btnSaveTraining.isEnabled = false

        val currentTime = System.currentTimeMillis()

        val trainingData = hashMapOf<String, Any>(
            "id" to trainingId,
            "title" to title,
            "timestamp" to currentTime,
            "createdAt" to currentTime
        )

        val exercisesMap = hashMapOf<String, Any>()
        exercises.forEachIndexed { index, exercise ->
            val exerciseId = "ex_${System.currentTimeMillis()}_$index"
            exercise.id = exerciseId
            exercisesMap[exerciseId] = exercise.toMap()
        }

        trainingData["exercises"] = exercisesMap

        newRef.setValue(trainingData)
            .addOnSuccessListener {
                updateUserStats(user.uid)
                Toast.makeText(this, "✓ Entrenamiento guardado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { ex ->
                btnSaveTraining.isEnabled = true
                Toast.makeText(this, "Error: ${ex.message}", Toast.LENGTH_LONG).show()
                android.util.Log.e("ExercisesActivity", "Error guardando: ${ex.message}", ex)
            }
    }

    private fun updateUserStats(uid: String) {
        val statsRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(uid)
            .child("stats")

        statsRef.child("trainings").get().addOnSuccessListener { snapshot ->
            val currentCount = snapshot.getValue(Int::class.java) ?: 0
            val updates = hashMapOf<String, Any>(
                "trainings" to (currentCount + 1),
                "lastTrainingAt" to System.currentTimeMillis()
            )
            statsRef.updateChildren(updates)
        }
    }
}
