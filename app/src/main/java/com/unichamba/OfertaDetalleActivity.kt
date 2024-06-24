package com.unichamba

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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

        // Verifica si el usuario está autenticado
        if (mAuth.currentUser != null) {
            // Si el usuario está autenticado, habilita el botón "Aplicar"
            btnApply.isEnabled = true
        } else {
            val customMessage = "Inicia sesión para aplicar a esta oferta!"
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
            obtenerTelefono()
        }
    }

    private fun obtenerTelefono() {
        val db = FirebaseFirestore.getInstance()
        db.collection("estudiantes")
            .whereEqualTo("email", quienPublicaText)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val telefono = documents.documents[0].getString("telefono")
                    telefono?.let { phoneNumber ->
                        Log.d(TAG, phoneNumber)
                        openWhatsApp(phoneNumber)
                    }
                } else {
                    Log.d(TAG, "No se encontró el documento correspondiente al usuario.")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error obteniendo documentos: ", exception)
            }
    }

    private fun openWhatsApp(phoneNumber: String) {
        val message = "Hola, estoy interesado en aplicar a la oferta"

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
