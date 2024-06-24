package com.unichamba.Fragmentos

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import com.unichamba.R
import com.unichamba.databinding.ActivityFragmentPerfilBinding
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.unichamba.model.Trabajo
import android.app.DatePickerDialog
import com.unichamba.CustomToast
import java.text.SimpleDateFormat
import java.util.*


class FragmentPerfil : AppCompatActivity() {
    private lateinit var binding: ActivityFragmentPerfilBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var imageUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    private var trabajosSeleccionados: MutableList<Map<String, String>> = mutableListOf()
    private var fechaNacimiento: String = ""
    private var fechaNacimientoSeleccionada: String? = null
    private var cvUri: Uri? = null
    private var cvUrlActual: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFragmentPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        cargarTrabajos()
        cargarInfo()
        inicializarSpinnerMunicipios()
        inicializarSpinnerCarreras()

        binding.btnGuardarCambios.setOnClickListener {
            validarInfo()
        }

        binding.FABCambiarImg.setOnClickListener {
            selec_imagen_de()
        }

        binding.btnSeleccionarFecha.setOnClickListener {
            mostrarDatePicker()
        }

        binding.fechaNacimientoEditText.setOnClickListener {
            mostrarDatePicker()
        }
        binding.btnCargarCV.setOnClickListener {
            seleccionarCV()
        }

    }

    private fun inicializarSpinnerMunicipios() {
        firestore.collection("municipios")
            .get()
            .addOnSuccessListener { result ->
                val municipios = result.documents.mapNotNull { it.getString("municipio") }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, municipios)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
                binding.municipioSpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar municipios: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun inicializarSpinnerCarreras() {
        firestore.collection("carreras")
            .get()
            .addOnSuccessListener { result ->
                val carreras = result.documents.mapNotNull { it.getString("carrera") }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, carreras)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.carreraSpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar carreras: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        val añoActual = calendario.get(Calendar.YEAR)
        val mesActual = calendario.get(Calendar.MONTH)
        val diaActual = calendario.get(Calendar.DAY_OF_MONTH)

        var edadMinima = Calendar.getInstance()
        edadMinima.add(Calendar.YEAR, -18)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, año, mes, dia ->
                val calendarSelected = Calendar.getInstance()
                calendarSelected.set(año, mes, dia)

                if (calendarSelected.after(edadMinima)) {
                    Toast.makeText(this, "Debe tener al menos 18 años", Toast.LENGTH_SHORT).show()
                } else {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    fechaNacimientoSeleccionada = dateFormat.format(calendarSelected.time)
                    binding.fechaNacimientoEditText.setText(fechaNacimientoSeleccionada)
                }
            },
            añoActual, mesActual, diaActual
        )

        datePickerDialog.datePicker.maxDate = edadMinima.timeInMillis
        datePickerDialog.show()
    }
    private fun seleccionarCV() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, REQUEST_CODE_CV)
    }

    companion object {
        private const val REQUEST_CODE_CV = 100
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CV && resultCode == Activity.RESULT_OK) {
            cvUri = data?.data
            if (cvUri != null) {
                subirCVStorage(cvUri!!)
            } else {
                val customMessage = "Error al seleccionar el archivo"
                val customToast = CustomToast(this, customMessage)
                customToast.show()
            }
        }
    }

    private fun subirCVStorage(uri: Uri) {
        progressDialog.setMessage("Subiendo currículum")
        progressDialog.show()

        val userId = firebaseAuth.uid!!
        val filePathAndName = "cvPerfil/$userId/$userId.pdf"
        val storageReference = FirebaseStorage.getInstance().reference.child(filePathAndName)

        storageReference.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    cvUrlActual = downloadUri.toString()
                    guardarCVEnFirestore(cvUrlActual!!)
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                val customMessage = "Error al subir el currículum: ${e.message}"
                val customToast = CustomToast(this, customMessage)
                customToast.show()
            }
    }

    private fun guardarCVEnFirestore(cvUrl: String) {
        val userId = firebaseAuth.uid!!
        val userMap = mapOf(
            "hojadevida" to cvUrl
        )
        firestore.collection("estudiantes")
            .document(userId)
            .update(userMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                val customMessage = "Error al actualizar el currículum: ${e.message}"
                val customToast = CustomToast(this, customMessage)
                customToast.show()
            }
    }

    private fun cargarTrabajos() {
        firestore.collection("trabajos")
            .get()
            .addOnSuccessListener { result ->
                val trabajosList = result.documents.map { document ->
                    Trabajos(
                        icono = document.getString("icono") ?: "",
                        nombre = document.getString("nombre") ?: ""
                    )
                }
                mostrarCheckBoxesTrabajos(trabajosList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar trabajos: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    data class Trabajos(val icono: String, val nombre: String)

    private fun mostrarCheckBoxesTrabajos(trabajosList: List<Trabajos>) {
        val checkboxContainer = findViewById<LinearLayout>(R.id.checkboxContainer)
        checkboxContainer.removeAllViews()

        trabajosList.forEach { trabajo ->
            val checkBox = CheckBox(this)
            checkBox.text = trabajo.nombre
            checkBox.tag = trabajo // Guarda el trabajo completo en el tag del CheckBox
            checkboxContainer.addView(checkBox)


            val seleccionado = trabajosSeleccionados.any { it["nombre"] == trabajo.nombre }
            checkBox.isChecked = seleccionado

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    trabajosSeleccionados.add(
                        mapOf(
                            "icono" to trabajo.icono,
                            "nombre" to trabajo.nombre
                        )
                    )
                } else {
                    trabajosSeleccionados.removeAll { it["nombre"] == trabajo.nombre }
                }
            }
        }
    }


    private var nombre = ""
    private var apellido = ""
    private var carrera = ""
    private var telefono = ""
    private var acercaDe = ""
    private var municipio = ""
    private var whatsapp= ""
    private fun validarInfo() {
        nombre = binding.nombreEditText.text.toString().trim()
        apellido = binding.apellidoEditText.text.toString().trim()
        carrera = binding.carreraSpinner.selectedItem.toString()
        telefono = binding.telefonoEditText.text.toString().trim()
        acercaDe = binding.descripcionEditText.text.toString().trim()
        municipio = binding.municipioSpinner.selectedItem.toString()
        fechaNacimientoSeleccionada = binding.fechaNacimientoEditText.text.toString().trim()
        whatsapp = binding.whatsappEditText.text.toString().trim()

        val checkboxContainer = findViewById<LinearLayout>(R.id.checkboxContainer)

        if (checkboxContainer.childCount > 0) {
            trabajosSeleccionados.clear()

            for (i in 0 until checkboxContainer.childCount) {
                val checkBox = checkboxContainer.getChildAt(i) as CheckBox
                if (checkBox.isChecked) {
                    val trabajoMap = checkBox.tag as? Map<String, String>
                    trabajoMap?.let { trabajosSeleccionados.add(it) }
                }
            }
        }


        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese sus nombres", Toast.LENGTH_SHORT).show()
        } else if (apellido.isEmpty()) {
            Toast.makeText(this, "Ingrese sus apellidos", Toast.LENGTH_SHORT).show()
        } else if (carrera.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre de su carrera", Toast.LENGTH_SHORT).show()
        } else if (telefono.isEmpty()) {
            Toast.makeText(this, "Ingrese su número de teléfono", Toast.LENGTH_SHORT).show()
        } else if (municipio.isEmpty()) {
            Toast.makeText(this, "Seleccione un municipio", Toast.LENGTH_SHORT).show()
        } else if (acercaDe.isEmpty()) {
            Toast.makeText(this, "Agrega una descripción", Toast.LENGTH_SHORT).show()
        } else if (trabajosSeleccionados.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos una opción", Toast.LENGTH_SHORT).show()
        } else if (fechaNacimientoSeleccionada.isNullOrEmpty()) {
            Toast.makeText(this, "Seleccione una fecha de nacimiento", Toast.LENGTH_SHORT).show()
        } else {
            actualizarTrabajosSeleccionadosFirestore(trabajosSeleccionados)
            actualizarInfo()
            setResult(Activity.RESULT_OK)
            subirImagenStorage()
        }
    }

    private fun actualizarTrabajosSeleccionadosFirestore(trabajosSeleccionados: List<Map<String, String>>) {
        progressDialog.setMessage("Actualizando trabajos seleccionados")
        progressDialog.show()

        firestore.collection("estudiantes")
            .document(firebaseAuth.uid!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // El documento existe, procedemos a actualizar los trabajos
                    firestore.collection("estudiantes")
                        .document(firebaseAuth.uid!!)
                        .update("trabajos", trabajosSeleccionados)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Se actualizó la información", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(
                                this,
                                "Error al actualizar trabajos: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    progressDialog.dismiss()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Error al verificar documento: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun obtenerFechaActual(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        return dateFormat.format(cal.time)
    }

    private fun actualizarInfo() {
        progressDialog.setMessage("Actualizando información")
        progressDialog.show()

        firestore.collection("estudiantes")
            .document(firebaseAuth.uid!!)
            .get()
            .addOnSuccessListener { document ->
                val currentImageUrl = document.getString("imageUrl")

                val hashMap = hashMapOf(
                    "nombre" to nombre,
                    "apellido" to apellido,
                    "email" to firebaseAuth.currentUser?.email,
                    "carrera" to carrera,
                    "telefono" to telefono,
                    "municipio" to municipio,
                    "acercaDe" to acercaDe,
                    "trabajos" to trabajosSeleccionados,
                    "fechaNacimiento" to fechaNacimientoSeleccionada,
                    "fecRegistro" to obtenerFechaActual(),
                    "imageUrl" to (imageUri?.toString() ?: currentImageUrl),
                    "hojadevida" to cvUrlActual, // Usar siempre la URL pública actual del CV
                    "whatsapp" to whatsapp,
                )

                firestore.collection("estudiantes")
                    .document(firebaseAuth.uid!!)
                    .set(hashMap)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Se actualizó su información", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarInfo() {
        firestore.collection("estudiantes")
            .document(firebaseAuth.uid!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val nombre = document.getString("nombre") ?: ""
                    val apellido = document.getString("apellido") ?: ""
                    val imagen = document.getString("imageUrl") ?: ""
                    val carrera = document.getString("carrera") ?: ""
                    val telefono = document.getString("telefono") ?: ""
                    val acercaDe = document.getString("acercaDe") ?: ""
                    val municipio = document.getString("municipio") ?: ""
                    val trabajosList = document.get("trabajos") as? List<Map<String, String>> ?: emptyList()
                    val trabajos = trabajosList.map { Trabajo(it["icono"] ?: "", it["nombre"] ?: "") }
                    val fechaNacimiento = document.getString("fechaNacimiento") ?: ""
                    cvUrlActual = document.getString("hojadevida") ?: ""
                    val whatsapp = document.getString("whatsapp")

                    binding.nombreEditText.setText(nombre)
                    binding.apellidoEditText.setText(apellido)
                    binding.telefonoEditText.setText(telefono)
                    binding.descripcionEditText.setText(acercaDe)
                    binding.fechaNacimientoEditText.setText(fechaNacimiento)
                    binding.whatsappEditText.setText(whatsapp)

                    // Setear el valor del Spinner
                    firestore.collection("municipios")
                        .get()
                        .addOnSuccessListener { result ->
                            val municipios = result.documents.map { it.getString("municipio") ?: "" }
                            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, municipios)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
                            binding.municipioSpinner.adapter = adapter

                            val municipioIndex = municipios.indexOf(municipio)
                            if (municipioIndex >= 0) {
                                binding.municipioSpinner.setSelection(municipioIndex)
                            }
                        }

                    // Setear el valor del Spinner de Carrera
                    firestore.collection("carreras")
                        .get()
                        .addOnSuccessListener { result ->
                            val carreras = result.documents.map { it.getString("carrera") ?: "" }
                            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, carreras)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            binding.carreraSpinner.adapter = adapter

                            val carreraIndex = carreras.indexOf(carrera)
                            if (carreraIndex >= 0) {
                                binding.carreraSpinner.setSelection(carreraIndex)
                            }
                        }

                    firestore.collection("trabajos")
                        .get()
                        .addOnSuccessListener { trabajosResult ->
                            val trabajosDisponibles = trabajosResult.documents.mapNotNull { doc ->
                                val icono = doc.getString("icono") ?: ""
                                val nombreTrabajo = doc.getString("nombre") ?: ""
                                if (icono.isNotEmpty() && nombreTrabajo.isNotEmpty()) {
                                    mapOf("icono" to icono, "nombre" to nombreTrabajo)
                                } else {
                                    null
                                }
                            }

                            val trabajosSeleccionadosList = document.get("trabajos") as? MutableList<Map<String, String>> ?: mutableListOf()
                            trabajosSeleccionados.clear()
                            trabajosSeleccionados.addAll(trabajosSeleccionadosList)

                            mostrarLayoutTrabajos(trabajosDisponibles, trabajosSeleccionados)
                        }
                        .addOnFailureListener { e ->
                            val customMessage = "Error al cargar trabajos: ${e.message}"
                            val customToast = CustomToast(this, customMessage)
                            customToast.show()
                        }

                    try {
                        Glide.with(applicationContext)
                            .load(imagen)
                            .placeholder(R.drawable.user)
                            .into(binding.imgPerfil)
                    } catch (e: Exception) {
                        // Manejar la excepción
                    }
                }
            }
            .addOnFailureListener { e ->
                val customMessage = "Error al cargar información: ${e.message}"
                val customToast = CustomToast(this, customMessage)
                customToast.show()
            }
    }


    private fun mostrarLayoutTrabajos(
        trabajosDisponibles: List<Map<String, String>>,
        trabajosSeleccionados: List<Map<String, String>>
    ) {
        val checkboxContainer = findViewById<LinearLayout>(R.id.checkboxContainer)
        checkboxContainer.removeAllViews()

        trabajosDisponibles.forEach { trabajoMap ->
            val checkBox = CheckBox(this)
            checkBox.text = trabajoMap["nombre"]
            checkBox.tag = trabajoMap
            checkboxContainer.addView(checkBox)

            val seleccionado = trabajosSeleccionados.contains(trabajoMap)
            checkBox.isChecked = seleccionado
        }
    }


    private fun subirImagenStorage() {
        if (imageUri != null) {
            val userId = firebaseAuth.uid!!
            val filePathAndName = "imagenesPerfil/$userId/$userId"
            val storageReference = FirebaseStorage.getInstance().reference.child(filePathAndName)

            storageReference.putFile(imageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        val userMap = mapOf(
                            "imageUrl" to imageUrl
                        )
                        firestore.collection("estudiantes")
                            .document(userId)
                            .update(userMap)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun selec_imagen_de() {
        val popupMenu = PopupMenu(this, binding.FABCambiarImg)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Galería")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemID = item.itemId
            if (itemID == 1) {
                imagenGaleria()
            }
            return@setOnMenuItemClickListener true
        }
    }


    private fun imagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultadoGaleria_ARL.launch(intent)
    }

    private val resultadoGaleria_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                val data = resultado.data
                imageUri = data!!.data
                // Mostrar la imagen seleccionada inmediatamente en la interfaz de usuario
                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.user)
                        .into(binding.imgPerfil)
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Error al cargar la imagen: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}
