package com.unichamba

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unichamba.Fragmentos.FragmentPerfil
import com.unichamba.databinding.FragmentCuentaBinding
import com.unichamba.model.Trabajo

class JovenesDetalleActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var btnApply: Button
    private lateinit var binding: JovenesDetalleActivity
    private lateinit var progressBarJovenesDetalle: ProgressBar



    companion object {
        const val EXTRA_ID = "extra_id"    }



    @SuppressLint("WrongViewCast", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_jovenes_detalle)

        // Obtén la raíz del layout
        val rootView = findViewById<View>(android.R.id.content).rootView

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Detalles del Joven"
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(toolbar)

        // Inicializa Firebase Authentication
        mAuth = FirebaseAuth.getInstance()



        val nombre: TextView = findViewById(R.id.nombre)
        val apellido: TextView = findViewById(R.id.apellido)
        val municipio: TextView = findViewById(R.id.municipio)
        val acercaDe: TextView = findViewById(R.id.acercaDe)
        val carrera: TextView = findViewById(R.id.carrera)
        val email: TextView = findViewById(R.id.email)
        val image: ImageView = findViewById(R.id.imagen)
        val telefono: TextView = findViewById(R.id.telefono)
        val fecRegistro: TextView = findViewById(R.id.fecRegistro)
        val fecNacimiento: TextView = findViewById(R.id.fecNacimiento)
        val whatsapp: TextView = findViewById(R.id.whatsapp)
        val trabajosIconosContainer: ViewGroup = findViewById(R.id.trabajosIconosContainer)
        val btnVerCV: Button = findViewById(R.id.btn_ver_cv)
        val btnWhatsapp: Button = findViewById(R.id.btn_whatsapp)

        progressBarJovenesDetalle = findViewById(R.id.progressBarJovenesDetalle)

        btnVerCV.setOnClickListener {
            verCV()
        }





        // Habilitar el botón de retroceso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val intent = intent
        val documentId = intent.getStringExtra(EXTRA_ID) ?: ""

        progressBarJovenesDetalle.visibility = View.VISIBLE

        // Obtener datos del estudiante desde Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("estudiantes").document(documentId).get()
            .addOnSuccessListener { document ->
                progressBarJovenesDetalle.visibility = View.GONE // Ocultar ProgressBar

                if (document != null) {
                    nombre.text = document.getString("nombre")
                    apellido.text = document.getString("apellido")
                    municipio.text = document.getString("municipio")
                    acercaDe.text = document.getString("acercaDe")
                    carrera.text = document.getString("carrera")
                    email.text = document.getString("email")
                    telefono.text = document.getString("telefono")
                    fecRegistro.text = document.getString("fecRegistro")
                    fecNacimiento.text = document.getString("fechaNacimiento")
                    whatsapp.text = document.getString("whatsapp")
                    val cvUrl = document.getString("hojadevida")

                    val imageUrl = document.getString("imageUrl")
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_cuenta) // Placeholder mientras se carga la imagen
                        .error(R.drawable.barra) // Imagen de error si falla la carga
                        .into(image) // ImageView donde se carga la imagen

                    // Cargar y mostrar los iconos de trabajos
                    val trabajosList = document.get("trabajos") as? List<Map<String, String>> ?: emptyList()
                    val trabajos = trabajosList.map { Trabajo(it["icono"] ?: "", it["nombre"] ?: "") }
                    //mostrarIconosDeTrabajos(trabajosList)

                    trabajosIconosContainer.removeAllViews()  // Limpiar contenedor antes de agregar nuevos iconos

                    val iconSize = resources.getDimensionPixelSize(R.dimen.icon_size_detalle)

                    trabajos.forEach { trabajo ->
                        val iconName = trabajo.icono
                        val iconResourceId = iconName?.let { getIconResource(it) } ?: 0

                        if (iconResourceId != 0) {
                            val iconoView = ImageView(this)

                            Glide.with(this)
                                .load(iconResourceId)
                                .override(iconSize, iconSize)
                                .into(iconoView)

                            iconoView.layoutParams = ViewGroup.LayoutParams(iconSize, iconSize)
                            trabajosIconosContainer.addView(iconoView)
                        }
                    }
                    // Configurar el botón de contacto de WhatsApp
                    val numeroWhatsapp = document.getString("whatsapp")
                    if (!numeroWhatsapp.isNullOrEmpty()) {
                        btnWhatsapp.setOnClickListener {
                            val uri = Uri.parse("https://wa.me/$numeroWhatsapp")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                        }
                    } else {
                        //btnContactarWhatsapp.visibility = View.GONE // Ocultar el botón si no hay número de WhatsApp
                        Toast.makeText(this, "Número de WhatsApp no disponible", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Manejar caso donde el documento no existe
                }
            }
            .addOnFailureListener { exception ->
                // Manejar error
                Log.d("JovenesDetalleActivity", "Error obteniendo los documentos: ", exception)
                progressBarJovenesDetalle.visibility = View.GONE // Ocultar ProgressBar
            }

        // Configurar onClickListener para el botón "Aplicar"


    }

    private fun verCV() {
        val intent = intent
        val documentId = intent.getStringExtra(EXTRA_ID) ?: ""

        // Obtener datos del estudiante desde Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("estudiantes")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                val cvUrl = document.getString("hojadevida")
                if (!cvUrl.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cvUrl))
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "No hay currículum disponible", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar el currículum: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getIconResource(iconName: String): Int {
        return resources.getIdentifier(iconName, "drawable", packageName)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
