package com.unichamba.Fragmentos

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.MultiAutoCompleteTextView
import android.widget.Toast
import com.unichamba.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentNuevaOferta.newInstance] factory method to
 * create an instance of this fragment.
 */

class FragmentNuevaOferta : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var telefonoEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var municipioAutoCompleteTextView: AutoCompleteTextView
    private lateinit var carreraMultiAutoCompleteTextView: MultiAutoCompleteTextView
    private lateinit var selectImageButton: Button
    private lateinit var imageView: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()
    private var imageUri: Uri? = null
    private var imageSmallUri: Uri? = null
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nueva_oferta, container, false)

        telefonoEditText = view.findViewById(R.id.telefono)
        descriptionEditText = view.findViewById(R.id.description)
        submitButton = view.findViewById(R.id.Btn_publicar_oferta)
        municipioAutoCompleteTextView = view.findViewById(R.id.Et_municipio)
        carreraMultiAutoCompleteTextView = view.findViewById(R.id.Et_carrera)
        selectImageButton = view.findViewById(R.id.Btn_seleccionar)
        imageView = view.findViewById(R.id.image_view)

        progressDialog = ProgressDialog(context)

        // Agregar TextWatcher para validar el formato del teléfono
        telefonoEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && !s.matches(Regex("\\d{4}-\\d{4}"))) {
                    telefonoEditText.error = "Formato inválido. Ejemplo: 1234-5678"
                }
            }
        })

        submitButton.setOnClickListener {
            val telefono = telefonoEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val municipio = municipioAutoCompleteTextView.text.toString()
            val carreras = carreraMultiAutoCompleteTextView.text.toString()

            if (telefono.isNotEmpty() && description.isNotEmpty() && municipio.isNotEmpty() && carreras.isNotEmpty()) {
                if (!telefono.matches(Regex("\\d{4}-\\d{4}"))) {
                    Toast.makeText(context, "Por favor, ingresa un número de teléfono válido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (imageUri != null) {
                    uploadImageToStorage(description, municipio, carreras, telefono)
                } else {
                    publishOffer(description, municipio, carreras, null, null, telefono)
                }
            } else {
                Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        selectImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        loadMunicipios()
        loadCarreras()

        return view
    }

    private fun loadMunicipios() {
        db.collection("municipios")
            .get()
            .addOnSuccessListener { documents ->
                val municipios = mutableListOf<String>()
                for (document in documents) {
                    document.getString("municipio")?.let { municipios.add(it) }
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, municipios)
                municipioAutoCompleteTextView.setAdapter(adapter)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al cargar los municipios: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadCarreras() {
        db.collection("carreras")
            .get()
            .addOnSuccessListener { documents ->
                val carreras = mutableListOf<String>()
                for (document in documents) {
                    document.getString("carrera")?.let { carreras.add(it) }
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, carreras)
                carreraMultiAutoCompleteTextView.setAdapter(adapter)
                carreraMultiAutoCompleteTextView.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al cargar las carreras: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                imageUri = it
                imageView.setImageURI(it)

                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
                val smallImageFile = File(requireContext().cacheDir, "smallImage.jpg")
                smallImageFile.createNewFile()
                val fos = FileOutputStream(smallImageFile)
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
                imageSmallUri = Uri.fromFile(smallImageFile)
            }
        }
    }

    private fun uploadImageToStorage(description: String, municipio: String, carreras: String, telefono: String) {
        progressDialog.setMessage("Subiendo imagen a Storage")
        progressDialog.show()

        val rutaImagen = "anuncios/${mAuth.uid}/imagenoferta.jpg"
        val ref = FirebaseStorage.getInstance().getReference(rutaImagen)
        val smallRutaImagen = "anuncios/${mAuth.uid}/imagenoferta_small.jpg"
        val smallRef = FirebaseStorage.getInstance().getReference(smallRutaImagen)

        ref.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                uriTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val urlImagenCargada = task.result.toString()

                        smallRef.putFile(imageSmallUri!!)
                            .addOnSuccessListener { smallTaskSnapshot ->
                                val smallUriTask = smallTaskSnapshot.storage.downloadUrl
                                smallUriTask.addOnCompleteListener { smallTask ->
                                    if (smallTask.isSuccessful) {
                                        val urlSmallImagenCargada = smallTask.result.toString()
                                        publishOffer(description, municipio, carreras, urlImagenCargada, urlSmallImagenCargada, telefono)
                                    } else {
                                        progressDialog.dismiss()
                                        Toast.makeText(context, "Error al obtener la URL de la imagen pequeña", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .addOnFailureListener { smallE ->
                                progressDialog.dismiss()
                                Toast.makeText(context, "${smallE.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(context, "Error al obtener la URL de la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun publishOffer(description: String, municipio: String, carreras: String, imageUrl: String?, smallImageUrl: String?, telefono: String) {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val email = currentUser.email
            if (email != null) {
                val carrerasList = carreras.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                val finalCarrerasList = if (carrerasList.isEmpty()) {
                    listOf("Aplican todas las carreras")
                } else {
                    carrerasList
                }

                val anuncio = hashMapOf(
                    "description" to description,
                    "direction" to municipio,
                    "carrera" to finalCarrerasList,
                    "quienPublica" to email,
                    "imagen" to imageUrl,
                    "imagenSmall" to smallImageUrl,
                    "telefono" to telefono
                )

                db.collection("anuncios")
                    .add(anuncio)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(context, "Oferta publicada con éxito", Toast.LENGTH_SHORT).show()
                        descriptionEditText.text.clear()
                        municipioAutoCompleteTextView.text.clear()
                        carreraMultiAutoCompleteTextView.text.clear()
                        telefonoEditText.text.clear()
                        imageView.setImageURI(null)
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(context, "Error al publicar la oferta: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 1234

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentNuevaOferta().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}