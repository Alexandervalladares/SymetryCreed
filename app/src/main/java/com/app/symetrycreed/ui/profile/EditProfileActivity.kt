package com.app.symetrycreed.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var imgAvatar: ImageView
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var btnBack: ImageView

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initViews()
        loadUserData()
        setupClickListeners()
    }

    private fun initViews() {
        imgAvatar = findViewById(R.id.imgAvatar)
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user == null) {
            android.widget.Toast.makeText(this, "No estás autenticado", android.widget.Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cargar datos básicos
        etFullName.setText(user.displayName ?: "")
        etEmail.setText(user.email ?: "")
        etEmail.isEnabled = false // El email no se puede cambiar

        // Cargar teléfono desde Firebase Database
        db.child("users").child(user.uid).child("phone")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val phone = snapshot.getValue(String::class.java) ?: ""
                    etPhone.setText(phone)
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("EditProfile", "Error: ${error.message}")
                }
            })
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        imgAvatar.setOnClickListener {
            android.widget.Toast.makeText(
                this,
                "Cambiar foto (próximamente)",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val user = auth.currentUser ?: return

        val newName = etFullName.text.toString().trim()
        val newPhone = etPhone.text.toString().trim()

        if (newName.isEmpty()) {
            etFullName.error = "El nombre no puede estar vacío"
            return
        }

        btnSaveChanges.isEnabled = false
        btnSaveChanges.text = "Guardando..."

        // Actualizar nombre en Firebase Auth
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnSuccessListener {
                // Actualizar teléfono en Firebase Database
                val updates = hashMapOf<String, Any>(
                    "name" to newName,
                    "phone" to newPhone
                )

                db.child("users").child(user.uid).updateChildren(updates)
                    .addOnSuccessListener {
                        android.widget.Toast.makeText(
                            this,
                            "✓ Cambios guardados",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        android.widget.Toast.makeText(
                            this,
                            "Error: ${e.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        btnSaveChanges.isEnabled = true
                        btnSaveChanges.text = "Guardar Cambios"
                    }
            }
            .addOnFailureListener { e ->
                android.widget.Toast.makeText(
                    this,
                    "Error: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                btnSaveChanges.isEnabled = true
                btnSaveChanges.text = "Guardar Cambios"
            }
    }
}