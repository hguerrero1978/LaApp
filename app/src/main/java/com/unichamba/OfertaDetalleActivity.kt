package com.unichamba

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder

class OfertaDetalleActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var btnApply: Button
    private lateinit var quienPublicaText: String // Variable para almacenar el correo del publicador

    companion object {
        const val EXTRA_quienPublica = "extra_quienPublica"
        const val EXTRA_TELEFONO = "extra_telefono"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_CARRERA = "extra_carrera"
        const val EXTRA_IMAGEN = "extra_imagen"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oferta_detalle)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Detalles de oferta"
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(toolbar)

        // Inicializa Firebase Authentication
        mAuth = FirebaseAuth.getInstance()

        // Obtén una referencia al botón "Aplicar"
        btnApply = findViewById(R.id.btnApply)
        val userEmail = mAuth.currentUser?.email
        // Verifica si el usuario está autenticado
        if (mAuth.currentUser != null && esEstudiante(userEmail)) {
            // Si el usuario está autenticado, habilita el botón "Aplicar"
            btnApply.isEnabled = true
        } else {
            val customMessage = "Debes ser estudiante para aplicar a esta oferta!"
            val customToast = CustomToast(this, customMessage)
            customToast.show()
            btnApply.isEnabled = false
        }

        val description: TextView = findViewById(R.id.description)
        val quienPublica: TextView = findViewById(R.id.quienPublica)
        val carrera: TextView = findViewById(R.id.carrera)
        val image: ImageView = findViewById(R.id.imagen)

        // Habilitar el botón de retroceso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val intent = intent
        description.text = intent.getStringExtra(EXTRA_DESCRIPTION)
        quienPublicaText = intent.getStringExtra(EXTRA_quienPublica) ?: ""
        quienPublica.text = quienPublicaText
        val carrerasArray = intent.getStringArrayExtra(EXTRA_CARRERA) ?: emptyArray()
        val carrerasList = ArrayList(carrerasArray.toList())
        val carrerasText = carrerasList.joinToString(", ")
        carrera.text = carrerasText

        // Cargar la imagen usando Glide
        val imagenUrl = intent.getStringExtra(EXTRA_IMAGEN)
        Glide.with(this)
            .load(imagenUrl)
            .placeholder(R.drawable.ic_cuenta) // Placeholder mientras se carga la imagen
            .error(R.drawable.barra) // Imagen de error si falla la carga
            .into(image) // ImageView donde se carga la imagen

        // Configurar onClickListener para el botón "Aplicar"
        btnApply.setOnClickListener {
            openWhatsApp()
        }
    }


    private fun esEstudiante(email: String?): Boolean {
        // Verifica si el correo electrónico contiene un punto y termina con "@ues.edu.sv"
        return email?.contains(".") == true && email.endsWith("@ues.edu.sv")
    }
   private fun openWhatsApp() {

        val phoneNumber = intent.getStringExtra(EXTRA_TELEFONO) // Reemplaza con el número de teléfono al que deseas enviar el mensaje
       val message = "Hola, estoy interesado en aplicar a la oferta" // Mensaje predeterminado

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/$phoneNumber/?text=${URLEncoder.encode(message, "UTF-8")}")
        startActivity(intent)
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
