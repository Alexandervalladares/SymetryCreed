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

    private val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedSex: String? = null
    private var selectedDateIso: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.genderGroup.addOnButtonCheckedListener {
                group: MaterialButtonToggleGroup, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            selectedSex = when (checkedId) {
                binding.btnMale.id -> "male"
                binding.btnFemale.id -> "female"
                else -> null
            }
        }

        binding.etBirthdate.setOnClickListener { openDatePicker() }
        binding.btnSubmit.setOnClickListener { savePersonalInfo() }
    }

    private fun openDatePicker() {
        val cal = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, y, m, d ->
                val calSel = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
                selectedDateIso = isoDate.format(calSel.time)
                binding.etBirthdate.setText(
                    String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)
                )
            },
            cal.get(Calendar.YEAR) - 18,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
            positiveButton?.setTextColor(resources.getColor(com.app.symetrycreed.R.color.red_accent, null))
            negativeButton?.setTextColor(resources.getColor(com.app.symetrycreed.R.color.red_accent, null))
        }

        dialog.show()
    }

    private fun savePersonalInfo() {
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
        } else if (!birthIso.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) {
            binding.tilBirthdate.error = "Formato de fecha inválido"
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

        val bmi = if (weight != null && height != null) {
            val m = height / 100f
            round((weight / m.pow(2)) * 10) / 10.0
        } else null

        if (bmi != null && (bmi < 10 || bmi > 60)) {
            Snackbar.make(binding.root, "Los valores generan un IMC inválido", Snackbar.LENGTH_LONG).show()
            return
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Snackbar.make(binding.root, "Sesión no válida", Snackbar.LENGTH_LONG).show()
            return
        }

        binding.btnSubmit.isEnabled = false

        val userRef = db.child("users").child(uid)

        userRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                // CREAR: Estructura inicial del usuario
                val newUser = mutableMapOf<String, Any>(
                    "uid" to uid,
                    "createdAt" to ServerValue.TIMESTAMP,
                    "lastLoginAt" to ServerValue.TIMESTAMP
                )

                val profileData = mutableMapOf<String, Any>(
                    "sex" to sex!!,
                    "birthdate" to birthIso!!,
                    "weightKg" to weight!!,
                    "heightCm" to height!!
                )

                if (bmi != null) {
                    profileData["bmi"] = bmi
                }
                profileData["updatedAt"] = ServerValue.TIMESTAMP

                newUser["profile"] = profileData

                userRef.setValue(newUser)
                    .addOnSuccessListener {
                        goToCenterActivity()
                    }
                    .addOnFailureListener { e ->
                        binding.btnSubmit.isEnabled = true
                        Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
            } else {
                // ACTUALIZAR: Usar setValue para cada campo individualmente
                val profileRef = userRef.child("profile")

                profileRef.child("sex").setValue(sex!!)
                    .continueWithTask { profileRef.child("birthdate").setValue(birthIso!!) }
                    .continueWithTask { profileRef.child("weightKg").setValue(weight!!) }
                    .continueWithTask { profileRef.child("heightCm").setValue(height!!) }
                    .continueWithTask {
                        if (bmi != null) {
                            profileRef.child("bmi").setValue(bmi)
                        } else {
                            com.google.android.gms.tasks.Tasks.forResult(null)
                        }
                    }
                    .continueWithTask { profileRef.child("updatedAt").setValue(ServerValue.TIMESTAMP) }
                    .continueWithTask { userRef.child("lastLoginAt").setValue(ServerValue.TIMESTAMP) }
                    .addOnSuccessListener {
                        goToCenterActivity()
                    }
                    .addOnFailureListener { e ->
                        binding.btnSubmit.isEnabled = true
                        Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
            }
        }.addOnFailureListener { e ->
            binding.btnSubmit.isEnabled = true
            Snackbar.make(binding.root, "Error de conexión: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun goToCenterActivity() {
        val i = Intent(this, com.app.symetrycreed.ui.home.CenterActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        finish()
    }
}