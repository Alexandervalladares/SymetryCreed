package com.app.symetrycreed.ui.training

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.model.Plan
import com.google.firebase.database.*


class GenerateTrainingActivity : AppCompatActivity() {

    private lateinit var containerPlans: LinearLayout
    private lateinit var btnCreatePlan: TextView
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_training)

        containerPlans = findViewById(R.id.containerPlans)
        btnCreatePlan = findViewById(R.id.btnCreatePlan)

        btnCreatePlan.setOnClickListener {
            Toast.makeText(this, "Crear nuevo plan (pendiente UI)", Toast.LENGTH_SHORT).show()
        }

        loadPlansFromFirebase()
    }

    private fun loadPlansFromFirebase() {
        val plansRef = db.child("plans")
        plansRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    seedDefaultPlans {
                        loadPlansFromFirebase()
                    }
                    return
                }

                containerPlans.removeAllViews()
                val inflater = LayoutInflater.from(this@GenerateTrainingActivity)

                for (child in snapshot.children) {
                    val plan = child.getValue(Plan::class.java) ?: continue
                    if (plan.id.isBlank()) plan.id = child.key ?: ""

                    // Inflar la tarjeta (MaterialCardView) - rootView es el card completo
                    val cardView = inflater.inflate(R.layout.item_plan_card, containerPlans, false)
                    val tvTitle = cardView.findViewById<TextView>(R.id.tvPlanTitle)
                    val tvLevel = cardView.findViewById<TextView>(R.id.tvPlanLevel)
                    val tvWeeks = cardView.findViewById<TextView>(R.id.tvPlanWeeks)
                    val tvCount = cardView.findViewById<TextView>(R.id.tvPlanCount)
                    val leftStripe = cardView.findViewById<View>(R.id.leftStripe)

                    tvTitle.text = plan.title
                    tvLevel.text = plan.level
                    tvWeeks.text = plan.weeks
                    tvCount.text = plan.trainingsCount

                    try {
                        leftStripe.setBackgroundColor(Color.parseColor(plan.stripeColorHex))
                    } catch (t: Throwable) {
                        leftStripe.setBackgroundColor(Color.parseColor("#FF5160"))
                    }

                    // Buscar el contenedor principal dentro de la tarjeta para añadir detalles dinámicos
                    // item_plan_card.xml tiene un LinearLayout con id planCardRoot
                    val rootLinear = cardView.findViewById<LinearLayout>(R.id.planCardRoot)

                    // Crear contenedor de detalles en tiempo de ejecución si no existe en el XML
                    var detailsContainer = cardView.findViewById<View>(R.id.planDetailsContainer)
                    if (detailsContainer == null) {
                        val details = LinearLayout(this@GenerateTrainingActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(12, 8, 12, 12)
                            visibility = View.GONE
                            id = View.generateViewId() // evita usar ids que no existan
                        }
                        val tvDesc = TextView(this@GenerateTrainingActivity).apply {
                            text = plan.shortDescription.ifBlank { "Descripción no disponible" }
                            setTextColor(Color.parseColor("#9FA3A7"))
                            textSize = 13f
                        }
                        details.addView(tvDesc)

                        // Añadir el details como último hijo del rootLinear (debajo del contenido principal)
                        rootLinear.addView(details)
                        detailsContainer = details
                    } else {
                        // Si existe en el layout (planDetailsContainer), rellenar su texto y esconderlo inicialmente
                        detailsContainer.visibility = View.GONE
                        val tvDesc = detailsContainer.findViewById<TextView?>(R.id.tvPlanPreviewDescription)
                        tvDesc?.text = plan.shortDescription.ifBlank { "Descripción no disponible" }
                    }

                    // Toggle expand/collapse al pulsar la tarjeta completa
                    cardView.setOnClickListener {
                        detailsContainer.visibility = if (detailsContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    }

                    containerPlans.addView(cardView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GenerateTrainingActivity, "Error al leer planes: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun seedDefaultPlans(onComplete: () -> Unit) {
        val plansRef = db.child("plans")
        val defaultList = listOf(
            Plan(
                id = "", title = "Plan de Fuerza", level = "Intermedio",
                weeks = "8 semanas", trainingsCount = "12 entrenamientos",
                stripeColorHex = "#FF5160",
                shortDescription = "Enfocado en fuerza y progresión con pesas. Ideal para ganar masa y fuerza."
            ),
            Plan(
                id = "", title = "Cardio Intensivo", level = "Avanzado",
                weeks = "4 semanas", trainingsCount = "16 entrenamientos",
                stripeColorHex = "#3DE1C8",
                shortDescription = "Entrenamientos cardiovasculares de alta intensidad para quemar grasa y mejorar capacidad."
            ),
            Plan(
                id = "", title = "Principiante Total", level = "Principiante",
                weeks = "6 semanas", trainingsCount = "18 entrenamientos",
                stripeColorHex = "#FF5160",
                shortDescription = "Programa balanceado para empezar, centrado en técnica y constancia."
            )
        )

        val writes = mutableListOf<Pair<DatabaseReference, Plan>>()
        for (p in defaultList) {
            val newRef = plansRef.push()
            p.id = newRef.key ?: ""
            writes.add(Pair(newRef, p))
        }

        var remaining = writes.size
        for ((ref, plan) in writes) {
            ref.setValue(plan).addOnCompleteListener {
                remaining--
                if (remaining <= 0) onComplete()
            }.addOnFailureListener {
                remaining--
                if (remaining <= 0) onComplete()
            }
        }
    }
}