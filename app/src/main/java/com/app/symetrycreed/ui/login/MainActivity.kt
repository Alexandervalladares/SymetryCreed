package com.app.symetrycreed.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.R
import com.app.symetrycreed.ui.home.HolaMundoActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val oneTapClient by lazy { Identity.getSignInClient(this) }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    if (user != null) {
                                        val db = FirebaseDatabase.getInstance().reference
                                        val usersRef = db.child("users").child(user.uid)
                                        val baseData = mapOf(
                                            "uid" to user.uid,
                                            "name" to (user.displayName ?: ""),
                                            "email" to (user.email ?: ""),
                                            "photoUrl" to (user.photoUrl?.toString() ?: ""),
                                            "provider" to "google"
                                        )
                                        // Crear si no existe; si existe actualiza lastLoginAt y datos básicos
                                        usersRef.get().addOnSuccessListener { snap ->
                                            if (!snap.exists()) {
                                                usersRef.setValue(
                                                    baseData + mapOf(
                                                        "createdAt" to ServerValue.TIMESTAMP,
                                                        "lastLoginAt" to ServerValue.TIMESTAMP
                                                    )
                                                ).addOnCompleteListener { _ ->
                                                    goHome()
                                                }
                                            } else {
                                                usersRef.updateChildren(
                                                    (baseData + mapOf(
                                                        "lastLoginAt" to ServerValue.TIMESTAMP
                                                    )) as Map<String, Any>
                                                ).addOnCompleteListener { _ ->
                                                    goHome()
                                                }
                                            }
                                        }.addOnFailureListener {
                                            goHome()
                                        }
                                    } else {
                                        goHome()
                                    }
                                } else {
                                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "No se obtuvo ID Token.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()

        findViewById<SignInButton>(R.id.btnGoogle).setOnClickListener {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                ).build()

            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    signInLauncher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    )
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al iniciar sesión: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun goHome() {
        startActivity(Intent(this, HolaMundoActivity::class.java))
        finish()
    }
}