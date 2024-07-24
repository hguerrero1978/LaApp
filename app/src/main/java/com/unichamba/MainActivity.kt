package com.unichamba

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.unichamba.Fragmentos.FragmentCuenta
import com.unichamba.Fragmentos.FragmentInicio
import com.unichamba.Fragmentos.FragmentMisOfertas
import com.unichamba.Fragmentos.FragmentNuevaOferta
import com.unichamba.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.Button
import com.unichamba.Fragmentos.FragmentJovenes
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.ImageButton
import androidx.core.view.GravityCompat


class MainActivity : AppCompatActivity(),FragmentInicio.OnVerOfertasClickListener , FragmentInicio.OnVerJovenesClickListener{

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()



        drawerLayout = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.mi_menu0)
        val hamburgerButton = findViewById<ImageButton>(R.id.btnBack)
        hamburgerButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        verFragmentInicio()


        binding.BottonNV!!.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Item_Inicio -> {
                    verFragmentInicio()
                    true
                }

                R.id.Item_Filtrar -> {
                    verFragmentFiltrar()
                    true
                }

                R.id.Item_Mis_Ofertas -> {
                    verFragmentMisOfertas()
                    true
                }

                R.id.Item_Cuenta -> {
                    comprobarSesion()
                    true
                }

                else -> {
                    false
                }
            }

        }
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.Item_sitio -> {
                    abrirSitioWeb("https://www.unichamba.com/")
                    true
                }


                R.id.Item_Terminos -> {
                    abrirSitioWeb("https://website-unichamba.netlify.app/policy")
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.Item_Quienes_Somos -> {
                    abrirSitioWeb("https://website-unichamba.netlify.app/details")
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }



    }
    override fun onVerJovenesClicked() {
        binding.BottonNV?.selectedItemId = R.id.Item_Filtrar
        verFragmentFiltrar()
    }
    override fun onVerOfertasClicked() {
        binding.BottonNV?.selectedItemId = R.id.Item_Mis_Ofertas
        verFragmentMisOfertas()
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
                verFragmentCuenta()
            }
        } else {
            // Si el usuario no ha iniciado sesión, redirigirlo a la pantalla de inicio de sesión
            startActivity(Intent(this, OpcionesLogin::class.java)) // Cierra todas las actividades previas
        }

    }


    private fun mostrarTérminosYCondiciones(sharedPreferencesEditor: SharedPreferences.Editor, userId: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_terms)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val acceptButton = dialog.findViewById<Button>(R.id.accept_button)
        acceptButton.setOnClickListener {
            // Guardar el estado de aceptación en SharedPreferences
            sharedPreferencesEditor.putBoolean(userId, true)
            sharedPreferencesEditor.apply()
            dialog.dismiss() // Cerrar el diálogo
            verFragmentInicio() // Mostrar la pantalla principal después de aceptar los términos
        }
        val declineButton = dialog.findViewById<Button>(R.id.decline_button)
        declineButton.setOnClickListener {
            dialog.dismiss() // Cierra el diálogo
        }
        dialog.show()
    }
    private fun abrirSitioWeb(url: String = "") {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }



    private fun verFragmentInicio() {
        binding.TituloRL!!.text = "Inicio"
        val fragment = FragmentInicio()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1!!.id, fragment, "FragmentInicio")
        fragmentTransition.commit()

    }

    private fun verFragmentFiltrar() {
        binding.TituloRL!!.text = "Jovenes"
        val fragment = FragmentJovenes()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1!!.id, fragment, "FragmentFiltral")
        fragmentTransition.commit()
    }

    private fun verFragmentMisOfertas() {
        binding.TituloRL!!.text = "Ofertas"
        val fragment = FragmentMisOfertas()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1!!.id, fragment, "FragmentMisOfertas")
        fragmentTransition.commit()
    }

    private fun verFragmentCuenta() {
        binding.TituloRL!!.text = "Cuenta"
        val fragment = FragmentCuenta()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1!!.id, fragment, "FragmentCuenta")
        fragmentTransition.commit()
    }

    private fun verFragmentNuevaOferta() {
        binding.TituloRL!!.text = "Publicar Oferta"
        val fragment = FragmentNuevaOferta()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1!!.id, fragment, "FragmentNuevaOferta")
        fragmentTransition.commit()
    }


}








private fun Any.replace(id: Int, fragment: FragmentInicio, tag: String) {
}

private fun Any.replace(id: Int, fragment: FragmentJovenes, tag: String) {
}

private fun Any.replace(id: Int, fragment: FragmentMisOfertas, tag: String) {
}

private fun Any.replace(id: Int, fragment: FragmentCuenta, tag: String) {
}

private fun Any.replace(id: Int, fragment: FragmentNuevaOferta, tag: String) {
}