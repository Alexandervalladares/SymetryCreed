package com.app.symetrycreed.ui.plans

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.model.Plan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.graphics.Color

class UserPlansActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var tvTitle: TextView
    private val db by lazy { FirebaseDatabase.getInstance().reference }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_plans)

        container = findViewById(R.id.containerUserPlans)
        tvTitle = findViewById(R.id.tvTitle)

        tvTitle.text = "Planes Disponibles"

        loadGlobalPlans()
    }

    private fun loadGlobalPlans() {
        val ref = db.child("plans")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                container.removeAllViews()

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    showEmptyState("No hay planes disponibles")
                    return
                }

                val plans = mutableListOf<Plan>()

                for (child in snapshot.children) {
                    try {
                        val planId = child.key ?: continue
                        val title = child.child("title").getValue(String::class.java) ?: continue
                        val level = child.child("level").getValue(String::class.java) ?: "intermediate"
                        val shortDescription = child.child("shortDescription").getValue(String::class.java) ?: ""
                        val stripeColorHex = child.child("stripeColorHex").getValue(String::class.java) ?: "#FF5160"

                        // Manejar weeks y trainingsCount como String o Int
                        val weeksRaw = child.child("weeks").value
                        val trainingsCountRaw = child.child("trainingsCount").value

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

                        val plan = Plan(
                            id = planId,
                            title = title,
                            level = level,
                            weeks = weeks,
                            trainingsCount = trainingsCount,
                            stripeColorHex = stripeColorHex,
                            shortDescription = shortDescription
                        )

                        plans.add(plan)

                    } catch (e: Exception) {
                        android.util.Log.e("UserPlansActivity", "Error parseando plan: ${e.message}", e)
                    }
                }

                if (plans.isEmpty()) {
                    showEmptyState("No se pudieron cargar los planes")
                    return
                }

                for (plan in plans) {
                    addPlanCard(plan)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("UserPlansActivity", "Error: ${error.message}")
                showEmptyState("Error al cargar: ${error.message}")
            }
        })
    }

    private fun addPlanCard(plan: Plan) {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.item_plan_card_detailed, container, false)

        val tvTitle = cardView.findViewById<TextView>(R.id.tvPlanTitle)
        val tvLevel = cardView.findViewById<TextView>(R.id.tvPlanLevel)
        val tvWeeks = cardView.findViewById<TextView>(R.id.tvPlanWeeks)
        val tvCount = cardView.findViewById<TextView>(R.id.tvPlanCount)
        val leftStripe = cardView.findViewById<android.view.View>(R.id.leftStripe)

        tvTitle.text = plan.title

        tvLevel.text = when(plan.level.lowercase()) {
            "beginner", "principiante" -> "Principiante"
            "intermediate", "intermedio" -> "Intermedio"
            "advanced", "avanzado" -> "Avanzado"
            else -> plan.level
        }

        tvWeeks.text = "📅 ${plan.weeks} semanas"
        tvCount.text = "💪 ${plan.trainingsCount} entrenamientos"

        try {
            leftStripe.setBackgroundColor(Color.parseColor(plan.stripeColorHex))
        } catch (t: Throwable) {
            leftStripe.setBackgroundColor(Color.parseColor("#FF5160"))
        }

        // ✅ Click: Copiar plan al usuario
        cardView.setOnClickListener {
            copyPlanToUser(plan)
        }

        container.addView(cardView)
    }

    private fun copyPlanToUser(plan: Plan) {
        val user = auth.currentUser
        if (user == null) {
            android.widget.Toast.makeText(this, "Debes iniciar sesión", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Agregar Plan")
            .setMessage("¿Deseas agregar '${plan.title}' a tus planes personales?")
            .setPositiveButton("Sí") { _, _ ->
                val userPlansRef = db.child("users").child(user.uid).child("plans")
                val newPlanRef = userPlansRef.push()
                val newPlanId = newPlanRef.key ?: return@setPositiveButton

                val userPlan = plan.copy(
                    id = newPlanId,
                    createdAt = System.currentTimeMillis(),
                    isActive = false
                )

                newPlanRef.setValue(userPlan.toMap())
                    .addOnSuccessListener {
                        android.widget.Toast.makeText(this, "✓ Plan agregado exitosamente", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        android.widget.Toast.makeText(this, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showEmptyState(message: String) {
        container.removeAllViews()
        val emptyView = TextView(this).apply {
            text = message
            textSize = 16f
            setTextColor(getColor(R.color.textSecondary))
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
        }
        container.addView(emptyView)
    }
}