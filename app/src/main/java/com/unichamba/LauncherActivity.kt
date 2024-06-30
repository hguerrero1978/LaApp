package com.unichamba

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LauncherActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser

        if (user != null) {
            // Verificar si el correo electrónico cumple con el formato de empleado
            if (user.email!!.matches(Regex("^[a-zA-Z]+\\.[a-zA-Z]+@ues\\.edu\\.sv$"))) {
                // Redirigir a MainActivityR si es empleado
                startActivity(Intent(this, MainActivityR::class.java))
            } else {
                // Redirigir a MainActivity si no es empleado
                startActivity(Intent(this, MainActivity::class.java))
            }
        } else {
            // Si el usuario no ha iniciado sesión, redirigirlo a la pantalla de inicio de sesión
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}