package com.app.symetrycreed.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.model.Goal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.graphics.Color
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.ArrayAdapter

class GoalsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var btnAddGoal: Button
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        container = findViewById(R.id.containerGoals)
        btnAddGoal = findViewById(R.id.btnAddGoal)

        // Verificar autenticación
        if (!checkAuthentication()) {
            return
        }

        btnAddGoal.setOnClickListener {
            showAddGoalDialog()
        }

        loadGoals()
    }

    private fun checkAuthentication(): Boolean {
        val user = auth.currentUser

        if (user == null) {
            android.util.Log.e("GoalsActivity", "❌ Usuario NO autenticado")
            android.widget.Toast.makeText(
                this,
                "No estás autenticado. Por favor inicia sesión.",
                android.widget.Toast.LENGTH_LONG
            ).show()
            showEmptyState()
            btnAddGoal.isEnabled = false
            return false
        }

        android.util.Log.d("GoalsActivity", "✅ Usuario autenticado: ${user.uid}")
        btnAddGoal.isEnabled = true
        return true
    }

    private fun loadGoals() {
        val user = auth.currentUser
        if (user == null) {
            showEmptyState()
            return
        }

        val goalsRef = db.child("users").child(user.uid).child("goals")

        android.util.Log.d("GoalsActivity", "📖 Leyendo: users/${user.uid}/goals")

        goalsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                android.util.Log.d("GoalsActivity", "✅ onDataChange - Existe: ${snapshot.exists()}, Hijos: ${snapshot.childrenCount}")

                container.removeAllViews()

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    showEmptyState()
                    return
                }

                for (child in snapshot.children) {
                    try {
                        val goalId = child.key ?: continue
                        val title = child.child("title").getValue(String::class.java) ?: continue
                        val description = child.child("description").getValue(String::class.java) ?: ""
                        val targetValue = child.child("targetValue").getValue(Double::class.java) ?: 0.0
                        val currentValue = child.child("currentValue").getValue(Double::class.java) ?: 0.0
                        val unit = child.child("unit").getValue(String::class.java) ?: ""
                        val period = child.child("period").getValue(String::class.java) ?: "total"
                        val colorHex = child.child("colorHex").getValue(String::class.java) ?: "#FF5160"

                        val goal = Goal(
                            id = goalId,
                            title = title,
                            description = description,
                            target = targetValue,
                            current = currentValue,
                            unit = unit,
                            colorHex = colorHex
                        )

                        addGoalCard(goal)
                    } catch (e: Exception) {
                        android.util.Log.e("GoalsActivity", "❌ Error: ${e.message}", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("GoalsActivity", "❌ Firebase Error:")
                android.util.Log.e("GoalsActivity", "   Mensaje: ${error.message}")
                android.util.Log.e("GoalsActivity", "   Código: ${error.code}")
                android.util.Log.e("GoalsActivity", "   Detalles: ${error.details}")

                android.widget.Toast.makeText(
                    this@GoalsActivity,
                    "Error: ${error.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()

                showEmptyState()
            }
        })
    }

    private fun addGoalCard(goal: Goal) {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.item_goal_card, container, false)

        val tvTitle = cardView.findViewById<TextView>(R.id.tvGoalTitle)
        val tvDescription = cardView.findViewById<TextView>(R.id.tvGoalDescription)
        val tvProgress = cardView.findViewById<TextView>(R.id.tvGoalProgress)
        val progressBar = cardView.findViewById<ProgressBar>(R.id.progressBarGoal)
        val tvPercentage = cardView.findViewById<TextView>(R.id.tvGoalPercentage)
        val iconView = cardView.findViewById<android.view.View>(R.id.goalIcon)

        tvTitle.text = goal.title
        tvDescription.text = goal.description

        val progressValue = if (goal.target > 0) {
            ((goal.current / goal.target) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        val currentText = if (goal.current % 1.0 == 0.0) {
            goal.current.toInt().toString()
        } else {
            goal.current.toString()
        }

        val targetText = if (goal.target % 1.0 == 0.0) {
            goal.target.toInt().toString()
        } else {
            goal.target.toString()
        }

        tvProgress.text = "$currentText${goal.unit} de $targetText${goal.unit}"
        progressBar.progress = progressValue
        tvPercentage.text = "$progressValue%"

        try {
            val color = Color.parseColor(goal.colorHex)
            iconView.setBackgroundColor(color)
            progressBar.progressTintList = android.content.res.ColorStateList.valueOf(color)
        } catch (e: Exception) {
            iconView.setBackgroundColor(Color.parseColor("#FF5160"))
        }

        cardView.setOnClickListener {
            showUpdateProgressDialog(goal)
        }

        container.addView(cardView)
    }

    private fun showAddGoalDialog() {
        val user = auth.currentUser
        if (user == null) {
            android.widget.Toast.makeText(this, "Debes iniciar sesión", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goal, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etGoalTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etGoalDescription)
        val etTarget = dialogView.findViewById<EditText>(R.id.etGoalTarget)
        val etUnit = dialogView.findViewById<EditText>(R.id.etGoalUnit)

        // Agregar Spinner para period si lo tienes en el layout, si no, usar "total" por defecto
        // val spinnerPeriod = dialogView.findViewById<Spinner>(R.id.spinnerPeriod)
        // val periods = arrayOf("total", "daily", "weekly", "monthly")
        // spinnerPeriod.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, periods)

        AlertDialog.Builder(this)
            .setTitle("Nuevo Objetivo")
            .setView(dialogView)
            .setPositiveButton("Crear") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val targetStr = etTarget.text.toString().trim()
                val unit = etUnit.text.toString().trim()
                // val period = spinnerPeriod.selectedItem.toString()

                if (title.isEmpty() || targetStr.isEmpty()) {
                    android.widget.Toast.makeText(this, "Completa los campos obligatorios", android.widget.Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val target = targetStr.toDoubleOrNull()
                if (target == null || target <= 0) {
                    android.widget.Toast.makeText(this, "Meta inválida", android.widget.Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                createGoal(title, description, target, unit, "total") // Usar el valor del spinner si lo agregaste
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun createGoal(title: String, description: String, target: Double, unit: String, period: String) {
        val user = auth.currentUser
        if (user == null) {
            android.widget.Toast.makeText(this, "Debes iniciar sesión", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val goalsRef = db.child("users").child(user.uid).child("goals")
        val newGoalRef = goalsRef.push()
        val goalId = newGoalRef.key ?: return

        val colors = listOf("#FF5160", "#3DE1C8", "#FFD700", "#9B59B6", "#E67E22")
        val randomColor = colors.random()

        // Estructura según las reglas de Firebase
        val goalData = hashMapOf<String, Any>(
            "id" to goalId,
            "title" to title,
            "targetValue" to target,  // ✅ Campo requerido
            "period" to period,        // ✅ Campo requerido (total, daily, weekly, monthly)
            "createdAt" to System.currentTimeMillis()  // ✅ Campo requerido
        )

        // Agregar campos opcionales solo si tienen valor
        if (description.isNotEmpty()) {
            goalData["description"] = description
        }

        if (unit.isNotEmpty()) {
            goalData["unit"] = unit
        }

        // currentValue es opcional, pero es útil inicializarlo en 0
        goalData["currentValue"] = 0.0

        // colorHex no está en las reglas, pero puedes agregarlo si actualizas las reglas
        // o quitarlo si no es necesario
        // goalData["colorHex"] = randomColor

        android.util.Log.d("GoalsActivity", "📝 Creando objetivo: $goalData")

        newGoalRef.setValue(goalData)
            .addOnSuccessListener {
                android.util.Log.d("GoalsActivity", "✅ Objetivo creado exitosamente")
                android.widget.Toast.makeText(this, "✓ Objetivo creado", android.widget.Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("GoalsActivity", "❌ Error creando objetivo: ${e.message}", e)
                android.widget.Toast.makeText(this, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
    }

    private fun showUpdateProgressDialog(goal: Goal) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_progress, null)
        val etProgress = dialogView.findViewById<EditText>(R.id.etProgress)
        val tvCurrentProgress = dialogView.findViewById<TextView>(R.id.tvCurrentProgress)

        tvCurrentProgress.text = "Progreso actual: ${goal.current}${goal.unit}"
        etProgress.hint = "Nuevo valor"

        AlertDialog.Builder(this)
            .setTitle("Actualizar Progreso")
            .setView(dialogView)
            .setPositiveButton("Actualizar") { _, _ ->
                val newValueStr = etProgress.text.toString().trim()
                val newValue = newValueStr.toDoubleOrNull()

                if (newValue == null || newValue < 0) {
                    android.widget.Toast.makeText(this, "Valor inválido", android.widget.Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                updateGoalProgress(goal.id, newValue)
            }
            .setNeutralButton("Eliminar") { _, _ ->
                confirmDeleteGoal(goal.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateGoalProgress(goalId: String, newValue: Double) {
        val user = auth.currentUser ?: return
        val goalRef = db.child("users").child(user.uid).child("goals").child(goalId)

        val updates = hashMapOf<String, Any>(
            "currentValue" to newValue,  // ✅ Usar "currentValue" en lugar de "current"
            "updatedAt" to System.currentTimeMillis()  // ✅ Agregar timestamp de actualización
        )

        goalRef.updateChildren(updates)
            .addOnSuccessListener {
                android.widget.Toast.makeText(this, "✓ Progreso actualizado", android.widget.Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                android.widget.Toast.makeText(this, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
    }

    private fun confirmDeleteGoal(goalId: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Objetivo")
            .setMessage("¿Estás seguro de que deseas eliminar este objetivo?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteGoal(goalId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteGoal(goalId: String) {
        val user = auth.currentUser ?: return
        val goalRef = db.child("users").child(user.uid).child("goals").child(goalId)

        goalRef.removeValue()
            .addOnSuccessListener {
                android.widget.Toast.makeText(this, "✓ Objetivo eliminado", android.widget.Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                android.widget.Toast.makeText(this, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
    }

    private fun showEmptyState() {
        container.removeAllViews()
        val emptyView = TextView(this).apply {
            text = "No tienes objetivos aún.\n\nPresiona '+' para crear uno"
            textSize = 16f
            setTextColor(getColor(R.color.textSecondary))
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
        }
        container.addView(emptyView)
    }
}