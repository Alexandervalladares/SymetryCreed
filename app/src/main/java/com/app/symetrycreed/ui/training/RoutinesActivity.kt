package com.app.symetrycreed.ui.training

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.model.Training
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RoutinesActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private val db by lazy { FirebaseDatabase.getInstance().reference }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routines)
        container = findViewById(R.id.containerRoutines)

        loadRoutines()
    }

    private fun loadRoutines() {
        val user = auth.currentUser ?: return
        val ref = db.child("users").child(user.uid).child("trainings")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                container.removeAllViews()
                for (child in snapshot.children) {
                    val t = child.getValue(Training::class.java) ?: continue
                    addRoutineRow(t)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun addRoutineRow(training: Training) {
        val inflater = LayoutInflater.from(this)
        val row = inflater.inflate(R.layout.item_routine_row, container, false)
        val tvTitle = row.findViewById<TextView>(R.id.tvRoutineTitle)
        val tvCount = row.findViewById<TextView>(R.id.tvRoutineCount)
        tvTitle.text = training.title
        tvCount.text = "${training.exercises.size} ejercicios"

        row.setOnClickListener {
            // abrir ExercisesActivity con training cargado (podemos pasar Serializable)
            val i = Intent(this, ExercisesActivity::class.java)
            i.putExtra("training", training)
            startActivity(i)
        }
        container.addView(row)
    }
}