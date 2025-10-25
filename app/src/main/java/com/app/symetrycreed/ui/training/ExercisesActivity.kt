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

        containerExercises = findViewById(R.id.containerExercises)
        btnAddExercise = findViewById(R.id.btnAddExercise)
        btnSaveTraining = findViewById(R.id.btnSaveTraining)
        edtTrainingTitle = findViewById(R.id.edtTrainingTitle)

        // Si se pasó un training por Intent: prellenarlo
        val trainingExtra = intent.getSerializableExtra("training") as? Training
        if (trainingExtra != null) {
            edtTrainingTitle.setText(trainingExtra.title)
            // agregar exercises desde trainingExtra
            for (ex in trainingExtra.exercises) {
                addExerciseView(prefill = ex)
            }
        } else {
            // un ejercicio por defecto
            addExerciseView()
        }

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

        // Referencias dentro del item
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
            v++
            tvSeries.text = v.toString()
        }
        btnSeriesMinus.setOnClickListener {
            var v = safeParseInt(tvSeries)
            if (v > 1) v--
            tvSeries.text = v.toString()
        }
        btnRepsPlus.setOnClickListener {
            var v = safeParseInt(tvReps)
            v++
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
        // Scroll to bottom
        containerExercises.post { (containerExercises.parent as? ScrollView)?.fullScroll(View.FOCUS_DOWN) }
    }

    private fun saveTrainingToFirebase() {
        val title = edtTrainingTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Ingresa el título del entrenamiento", Toast.LENGTH_SHORT).show()
            return
        }

        val exercises = mutableListOf<Exercise>()
        for (i in 0 until containerExercises.childCount) {
            val item = containerExercises.getChildAt(i)
            val name = (item.findViewById<View>(R.id.edtExerciseName) as EditText).text.toString().trim()
            val series = (item.findViewById<View>(R.id.tvSeries) as TextView).text.toString().toIntOrNull() ?: 0
            val reps = (item.findViewById<View>(R.id.tvReps) as TextView).text.toString().toIntOrNull() ?: 0
            val weightStr = (item.findViewById<View>(R.id.edtWeight) as EditText).text.toString().trim()
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
            exercises.add(ex)
        }

        if (exercises.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un ejercicio", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión para guardar el entrenamiento", Toast.LENGTH_SHORT).show()
            return
        }

        val dbRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(user.uid)
            .child("trainings")

        val newRef = dbRef.push()
        val trainingId = newRef.key ?: return

        val training = Training(
            id = trainingId,
            title = title,
            timestamp = System.currentTimeMillis(),
            exercises = exercises
        )

        btnSaveTraining.isEnabled = false

        newRef.setValue(training)
            .addOnSuccessListener {
                Toast.makeText(this, "Entrenamiento guardado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { ex ->
                Toast.makeText(this, "Error al guardar: ${ex.message}", Toast.LENGTH_LONG).show()
                btnSaveTraining.isEnabled = true
            }
    }
}