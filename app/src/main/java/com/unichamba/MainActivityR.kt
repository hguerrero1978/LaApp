package com.unichamba

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.unichamba.Fragmentos.FragmentCuenta
import com.unichamba.Fragmentos.FragmentNuevaOferta
import com.google.firebase.auth.FirebaseAuth
import android.app.Dialog
import android.widget.Button
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.ImageButton
import androidx.core.view.GravityCompat
import com.unichamba.databinding.ActivityMainRBinding


class MainActivityR : AppCompatActivity(){

    private lateinit var binding: ActivityMainRBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainRBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()



        drawerLayout = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.mi_menu1)
        val hamburgerButton = findViewById<ImageButton>(R.id.btnBack)
        hamburgerButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        verFragmentCuentaR()


        binding.BottonNV!!.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Item_Cuenta -> {
                    verFragmentCuentaR()
                    true
                }

                R.id.Item_Publicar_Oferta-> {
                    comprobarSesion()
                    true
                }

                else -> {
                    false
                }
            }

        }


    }

    private fun comprobarSesion() {
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val user = firebaseAuth.currentUser

        if (user != null) {
            val userId = user.uid
            val termsAccepted = sharedPreferences.getBoolean(userId, false)

            if (!termsAccepted) {
                mostrarTérminosYCondiciones(sharedPreferences.edit(), userId)
            } else {
                // El usuario ya ha aceptado los términos y condiciones
                verFragmentNuevaOfertaR()
            }
        } else {
            // Si el usuario no ha iniciado sesión, redirigirlo a la pantalla de inicio de sesión
            startActivity(Intent(this, OpcionesLogin::class.java)) // Cierra todas las actividades previas
        }
    }

    private fun mostrarTérminosYCondiciones(sharedPreferencesEditor: SharedPreferences.Editor, userId: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_terms)
        val acceptButton = dialog.findViewById<Button>(R.id.accept_button)
        acceptButton.setOnClickListener {
            // Guardar el estado de aceptación en SharedPreferences
            sharedPreferencesEditor.putBoolean(userId, true)
            sharedPreferencesEditor.apply()
            dialog.dismiss() // Cerrar el diálogo
            verFragmentCuentaR() // Mostrar la pantalla principal después de aceptar los términos
        }
        dialog.show()
    }



    private fun verFragmentCuentaR() {
        binding.TituloRL!!.text = "Cuenta"
        val fragment = FragmentCuenta()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1!!.id, fragment, "FragmentCuenta")
        fragmentTransition.commit()
    }

    private fun verFragmentNuevaOfertaR() {
        binding.TituloRL!!.text = "Publicar Oferta"
        val fragment = FragmentNuevaOferta()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1!!.id, fragment, "FragmentNuevaOferta")
        fragmentTransition.commit()
    }


}











private fun Any.replace(id: Int, fragment: FragmentCuenta, tag: String) {
}

private fun Any.replace(id: Int, fragment: FragmentNuevaOferta, tag: String) {
}