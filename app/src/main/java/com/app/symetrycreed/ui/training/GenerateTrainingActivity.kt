package com.app.symetrycreed.ui.training

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.model.Plan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter

class GenerateTrainingActivity : AppCompatActivity() {
    private fun showCreatePlanDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_plan, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etPlanTitle)
        val spinnerLevel = dialogView.findViewById<Spinner>(R.id.spinnerLevel)
        val etWeeks = dialogView.findViewById<EditText>(R.id.etWeeks)
        val etTrainingsCount = dialogView.findViewById<EditText>(R.id.etTrainingsCount)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

        // Configurar spinner de nivel con colores correctos
        val levels = arrayOf("Principiante", "Intermedio", "Avanzado")
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            levels
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(getColor(R.color.white))
                return view
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(getColor(R.color.white))
                view.setBackgroundColor(getColor(R.color.background_dark))
                return view
            }
        }
        spinnerLevel.adapter = adapter
        spinnerLevel.setSelection(1) // Intermedio por defecto

        // ✅ Crear diálogo con tema oscuro y botones rojos
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Crear Nuevo Plan")
            .setView(dialogView)
            .setPositiveButton("CREAR", null) // null para manejar manualmente
            .setNegativeButton("CANCELAR", null)
            .create()

        dialog.setOnShowListener {
            // ✅ Cambiar color de botones a rojo
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.apply {
                setTextColor(getColor(R.color.red_accent))
                setOnClickListener {
                    val title = etTitle.text.toString().trim()
                    val levelPosition = spinnerLevel.selectedItemPosition
                    val level = when(levelPosition) {
                        0 -> "beginner"
                        1 -> "intermediate"
                        2 -> "advanced"
                        else -> "intermediate"
                    }
                    val weeks = etWeeks.text.toString().toIntOrNull() ?: 0
                    val trainingsCount = etTrainingsCount.text.toString().toIntOrNull() ?: 0
                    val description = etDescription.text.toString().trim()

                    if (title.isBlank()) {
                        Toast.makeText(this@GenerateTrainingActivity, "Ingresa un título", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (weeks < 1 || weeks > 52) {
                        Toast.makeText(this@GenerateTrainingActivity, "Las semanas deben estar entre 1 y 52", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    createPlan(title, level, weeks, trainingsCount, description)
                    dialog.dismiss()
                }
            }

            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.apply {
                setTextColor(getColor(R.color.red_accent))
            }

            // ✅ Cambiar color del título
            val titleTextView = dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleTextView?.setTextColor(getColor(R.color.red_accent))
        }

        dialog.show()
    }

    private fun createPlan(
        title: String,
        level: String,
        weeks: Int,
        trainingsCount: Int,
        description: String
    ) {
        val plansRef = db.child("plans")
        val newPlanRef = plansRef.push()
        val planId = newPlanRef.key ?: return

        val colors = listOf("#FF5160", "#3DE1C8", "#FF9500", "#5856D6", "#FF2D55")
        val randomColor = colors.random()

        val planData = hashMapOf<String, Any>(
            "id" to planId,
            "title" to title,
            "level" to level,
            "weeks" to weeks,
            "trainingsCount" to trainingsCount,
            "stripeColorHex" to randomColor,
            "shortDescription" to description,
            "createdAt" to System.currentTimeMillis()
        )

        newPlanRef.setValue(planData)
            .addOnSuccessListener {
                Toast.makeText(this, "✓ Plan creado exitosamente", Toast.LENGTH_SHORT).show()
                loadPlansFromFirebase() // Recargar lista
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                android.util.Log.e("GenerateTraining", "Error creando plan: ${e.message}", e)
            }
    }
    private lateinit var containerPlans: LinearLayout
    private lateinit var btnCreatePlan: TextView
    private val db by lazy { FirebaseDatabase.getInstance().reference }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_training)

        containerPlans = findViewById(R.id.containerPlans)
        btnCreatePlan = findViewById(R.id.btnCreatePlan)

        btnCreatePlan.setOnClickListener {
            showCreatePlanDialog()
        }

        loadPlansFromFirebase()
    }

    private fun loadPlansFromFirebase() {
        // ✅ Leer desde /plans (donde están guardados actualmente)
        val plansRef = db.child("plans")

        plansRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                containerPlans.removeAllViews()

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    android.util.Log.d("GenerateTraining", "No hay planes en Firebase")
                    showEmptyState()
                    return
                }

                android.util.Log.d("GenerateTraining", "Planes encontrados: ${snapshot.childrenCount}")

                val inflater = LayoutInflater.from(this@GenerateTrainingActivity)
                var successCount = 0

                for (child in snapshot.children) {
                    try {
                        val planId = child.key ?: continue

                        // Leer cada campo individualmente
                        val title = child.child("title").getValue(String::class.java)
                        val level = child.child("level").getValue(String::class.java)
                        val shortDescription = child.child("shortDescription").getValue(String::class.java) ?: ""
                        val stripeColorHex = child.child("stripeColorHex").getValue(String::class.java) ?: "#FF5160"

                        // ✅ IMPORTANTE: Leer weeks y trainingsCount como String primero
                        val weeksRaw = child.child("weeks").value
                        val trainingsCountRaw = child.child("trainingsCount").value

                        // Extraer números (manejar "8 semanas" o 8)
                        val weeks = when (weeksRaw) {
                            is String -> weeksRaw.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                            is Long -> weeksRaw.toInt()
                            is Int -> weeksRaw
                            else -> 0
                        }

                        val trainingsCount = when (trainingsCountRaw) {
                            is String -> trainingsCountRaw.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                            is Long -> trainingsCountRaw.toInt()
                            is Int -> trainingsCountRaw
                            else -> 0
                        }

                        if (title == null || level == null) {
                            android.util.Log.w("GenerateTraining", "Plan $planId tiene datos incompletos")
                            continue
                        }

                        val plan = Plan(
                            id = planId,
                            title = title,
                            level = level,
                            weeks = weeks,
                            trainingsCount = trainingsCount,
                            stripeColorHex = stripeColorHex,
                            shortDescription = shortDescription
                        )

                        addPlanCard(plan, inflater)
                        successCount++

                    } catch (e: Exception) {
                        android.util.Log.e("GenerateTraining", "Error parseando plan: ${e.message}", e)
                    }
                }

                android.util.Log.d("GenerateTraining", "Planes mostrados exitosamente: $successCount")

                if (successCount == 0) {
                    showEmptyState()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@GenerateTrainingActivity,
                    "Error al leer planes: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                android.util.Log.e("GenerateTraining", "Error de Firebase: ${error.message}")
                showEmptyState()
            }
        })
    }

    private fun addPlanCard(plan: Plan, inflater: LayoutInflater) {
        val cardView = inflater.inflate(R.layout.item_plan_card, containerPlans, false)
        val tvTitle = cardView.findViewById<TextView>(R.id.tvPlanTitle)
        val tvLevel = cardView.findViewById<TextView>(R.id.tvPlanLevel)
        val tvWeeks = cardView.findViewById<TextView>(R.id.tvPlanWeeks)
        val tvCount = cardView.findViewById<TextView>(R.id.tvPlanCount)
        val leftStripe = cardView.findViewById<android.view.View>(R.id.leftStripe)

        tvTitle.text = plan.title

        // ✅ Traducir y capitalizar nivel
        tvLevel.text = when(plan.level.lowercase()) {
            "beginner", "principiante" -> "Principiante"
            "intermediate", "intermedio" -> "Intermedio"
            "advanced", "avanzado" -> "Avanzado"
            else -> plan.level.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }

        // ✅ Formatear números a texto legible
        tvWeeks.text = if (plan.weeks > 0) {
            "${plan.weeks} semana${if (plan.weeks != 1) "s" else ""}"
        } else {
            "N/A"
        }

        tvCount.text = if (plan.trainingsCount > 0) {
            "${plan.trainingsCount} entrenamiento${if (plan.trainingsCount != 1) "s" else ""}"
        } else {
            "N/A"
        }

        // ✅ Color de la banda lateral
        try {
            leftStripe.setBackgroundColor(Color.parseColor(plan.stripeColorHex))
        } catch (t: Throwable) {
            android.util.Log.w("GenerateTraining", "Color inválido: ${plan.stripeColorHex}")
            leftStripe.setBackgroundColor(Color.parseColor("#FF5160"))
        }

        val rootLinear = cardView.findViewById<LinearLayout>(R.id.planCardRoot)
        var detailsContainer = cardView.findViewById<android.view.View>(R.id.planDetailsContainer)

        // ✅ Crear o encontrar el contenedor de descripción
        if (detailsContainer == null) {
            val details = LinearLayout(this@GenerateTrainingActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 8, 12, 12)
                visibility = android.view.View.GONE
                id = android.view.View.generateViewId()
            }
            val tvDesc = TextView(this@GenerateTrainingActivity).apply {
                text = plan.shortDescription.ifBlank { "Sin descripción" }
                setTextColor(Color.parseColor("#9FA3A7"))
                textSize = 13f
                setPadding(6, 0, 6, 0)
            }
            details.addView(tvDesc)
            rootLinear.addView(details)
            detailsContainer = details
        } else {
            detailsContainer.visibility = android.view.View.GONE
            val tvDesc = detailsContainer.findViewById<TextView?>(R.id.tvPlanPreviewDescription)
            tvDesc?.text = plan.shortDescription.ifBlank { "Sin descripción" }
        }

        // ✅ Click: Mostrar/ocultar descripción
        cardView.setOnClickListener {
            detailsContainer.visibility = if (detailsContainer.visibility == android.view.View.VISIBLE) {
                android.view.View.GONE
            } else {
                android.view.View.VISIBLE
            }
        }

        // ✅ Long Press: Copiar plan al usuario
        cardView.setOnLongClickListener {
            copyPlanToUser(plan)
            true
        }

        containerPlans.addView(cardView)
    }

    private fun copyPlanToUser(plan: Plan) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión para copiar planes", Toast.LENGTH_LONG).show()
            return
        }

        // Confirmar con el usuario
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Copiar Plan")
            .setMessage("¿Deseas agregar '${plan.title}' a tus planes personales?")
            .setPositiveButton("Sí") { _, _ ->
                performCopyPlan(user.uid, plan)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performCopyPlan(uid: String, plan: Plan) {
        val userPlansRef = db.child("users").child(uid).child("plans")
        val newPlanRef = userPlansRef.push()
        val newPlanId = newPlanRef.key ?: return

        // Crear copia del plan para el usuario
        val userPlan = plan.copy(
            id = newPlanId,
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            startedAt = null,
            completedAt = null,
            isActive = false
        )

        if (!userPlan.isValid()) {
            Toast.makeText(this, "El plan tiene datos inválidos", Toast.LENGTH_SHORT).show()
            android.util.Log.e("GenerateTraining", "Plan inválido: $userPlan")
            return
        }

        newPlanRef.setValue(userPlan.toMap())
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "✓ '${plan.title}' agregado a tus planes",
                    Toast.LENGTH_SHORT
                ).show()
                android.util.Log.d("GenerateTraining", "Plan copiado exitosamente: $newPlanId")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al copiar: ${e.message}", Toast.LENGTH_LONG).show()
                android.util.Log.e("GenerateTraining", "Error copiando plan: ${e.message}", e)
            }
    }

    private fun showEmptyState() {
        containerPlans.removeAllViews()
        val emptyView = TextView(this).apply {
            text = "No hay planes disponibles.\nAgrega algunos desde Firebase Console."
            textSize = 15f
            setTextColor(getColor(R.color.textSecondary))
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
        }
        containerPlans.addView(emptyView)
    }
}