package com.app.symetrycreed.ui.profile

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.databinding.ActivityPersonalInfoBinding
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow
import kotlin.math.round
import android.content.Intent



class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInfoBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    // Guardamos ISO YYYY-MM-DD
    private val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedSex: String? = null      // "male" | "female"
    private var selectedDateIso: String? = null  // "YYYY-MM-DD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // Toggle de sexo
        binding.genderGroup.addOnButtonCheckedListener { group: MaterialButtonToggleGroup, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            selectedSex = when (checkedId) {
                binding.btnMale.id -> "male"
                binding.btnFemale.id -> "female"
                else -> null
            }
        }

        // DatePicker para fecha de nacimiento
        binding.etBirthdate.setOnClickListener { openDatePicker() }

        // Guardar
        binding.btnSubmit.setOnClickListener {
            savePersonalInfo()
        }
    }


    private fun openDatePicker() {
        val cal = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, y, m, d ->

                // Almacenar y mostrar como dd/MM/yyyy, guardar como ISO
                val calSel = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
                selectedDateIso = isoDate.format(calSel.time)
                binding.etBirthdate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y))
            },
            cal.get(Calendar.YEAR) - 18,  // por defecto 18 años atrás
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        // Cambiar colores de los botones cuando se muestre el diálogo
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)

            // Color rojo para que sean visibles
            positiveButton?.setTextColor(resources.getColor(com.app.symetrycreed.R.color.red_accent, null))
            negativeButton?.setTextColor(resources.getColor(com.app.symetrycreed.R.color.red_accent, null))
        }

        dialog.show()
    }

    private fun savePersonalInfo() {
        // Limpiar errores previos
        binding.tilBirthdate.error = null
        binding.tilWeight.error = null
        binding.tilHeight.error = null

        val sex = selectedSex
        val birthIso = selectedDateIso
        val weightStr = binding.etWeight.text?.toString()?.trim().orEmpty()
        val heightStr = binding.etHeight.text?.toString()?.trim().orEmpty()

        var ok = true

        if (sex == null) {
            Snackbar.make(binding.root, "Selecciona tu sexo biológico", Snackbar.LENGTH_LONG).show()
            ok = false
        }
        if (birthIso.isNullOrEmpty()) {
            binding.tilBirthdate.error = "Selecciona tu fecha"
            ok = false
        }

        val weight = weightStr.toFloatOrNull()
        if (weight == null || weight < 30f || weight > 300f) {
            binding.tilWeight.error = "Ingresa un peso válido (30–300 kg)"
            ok = false
        }

        val height = heightStr.toFloatOrNull()
        if (height == null || height < 120f || height > 250f) {
            binding.tilHeight.error = "Ingresa una altura válida (120–250 cm)"
            ok = false
        }

        if (!ok) return

        // IMC = kg / (m^2)
        val bmi = if (weight != null && height != null) {
            val m = height / 100f
            round((weight / m.pow(2)) * 10) / 10.0  // 1 decimal
        } else null

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Snackbar.make(binding.root, "Sesión no válida", Snackbar.LENGTH_LONG).show()
            return
        }

        binding.btnSubmit.isEnabled = false

        val updates = mutableMapOf<String, Any>(
            "sex" to sex!!,
            "birthdate" to birthIso!!,
            "weightKg" to weight!!,
            "heightCm" to height!!,
            "updatedAt" to ServerValue.TIMESTAMP
        )
        if (bmi != null) updates["bmi"] = bmi

        db.child("users").child(uid).child("profile")
            .updateChildren(updates)
            .addOnCompleteListener { task ->
                binding.btnSubmit.isEnabled = true
                if (task.isSuccessful) {
                    // Feedback opcional
                    // Snackbar.make(binding.root, "Información guardada ✓", Snackbar.LENGTH_SHORT).show()

                    // 👉 Ir al dashboard central
                    val i = Intent(this, com.app.symetrycreed.ui.home.CenterActivity::class.java)
                    // opcional: limpiar el backstack para que no vuelva al onboarding con "atrás"
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(i)
                    finish()
                } else {
                    Snackbar.make(binding.root, "Error al guardar", Snackbar.LENGTH_LONG).show()
                }
            }
    }
}
