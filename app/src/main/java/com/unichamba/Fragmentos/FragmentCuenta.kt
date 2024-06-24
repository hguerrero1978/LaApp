package com.unichamba.Fragmentos
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.unichamba.OpcionesLogin
import com.unichamba.R
import com.unichamba.databinding.FragmentCuentaBinding
import android.widget.Toast
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class FragmentCuenta : Fragment() {
    private lateinit var binding: FragmentCuentaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext: Context
    private val REQUEST_CODE_EDIT_PROFILE = 1



    override fun onAttach(context: Context) {
        mContext=context
        super.onAttach(context)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        binding=FragmentCuentaBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth=FirebaseAuth.getInstance()

        binding.BtnCerrarSesion.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(mContext,OpcionesLogin::class.java))
            activity?.finishAffinity()
        }
        val btnEditarPerfil: Button = view.findViewById(R.id.btn_editar_perfil)
        btnEditarPerfil.setOnClickListener {
            val intent = Intent(mContext, FragmentPerfil::class.java)
            startActivity(intent)
        }

        val btnVerCV: Button = view.findViewById(R.id.btn_ver_cv)
        btnVerCV.setOnClickListener {
            verCV()
        }

        leerInfo()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == Activity.RESULT_OK) {
            // Si se ha realizado algún cambio en FragmentPerfil, actualizar la información
            leerInfo()
        }
    }
    private fun leerInfo() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("estudiantes")
            .document(firebaseAuth.uid!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val nombre = document.getString("nombre") ?: "Nombre"
                    val apellido = document.getString("apellido") ?: "Apellido"
                    val email = firebaseAuth.currentUser?.email ?: "ab00000@gmail.com"
                    val imagen = document.getString("imageUrl") ?: ""
                    val carrera = document.getString("carrera") ?: "Ingeniería en Sistemas (ejemplo)"
                    val telefono = document.getString("telefono") ?: "0000-0000"
                    val municipio = document.getString("municipio") ?: "Ahuachapán"
                    val fechaNacimiento = document.getString("fechaNacimiento") ?: ""
                    val acercaDe = document.getString("acercaDe") ?: "Descripción no disponible"
                    val trabajosSeleccionados = document.get("trabajos") as? List<Map<String, String>> ?: emptyList()
                    val fechaRegistro = document.getString("fecRegistro") ?: ""
                    val whatsapp = document.getString("whatsapp") ?: ""

                    
                    binding.nombreEditText.setText(nombre)
                    binding.apellidoEditText.setText(apellido)
                    binding.TvEmail.setText(email)
                    binding.carreraEditText.setText("Carrera: $carrera")
                    binding.telefonoEditText.setText("Teléfono: $telefono")
                    binding.municipioSpinner.setText("Ubicación: $municipio")
                    binding.descripcionEditText.setText(acercaDe)
                    binding.whatsappEditText.setText("Whatsapp: $whatsapp")
                    binding.fechaNacimientoEditText.setText("Fecha Nacimiento: $fechaNacimiento")
                    binding.fechaRegistroEditText.setText("Fecha de Registro: $fechaRegistro")
                    val cvUrl = document.getString("hojadevida")

                    // Para la imagen
                    try {
                        Glide.with(mContext)
                            .load(imagen)
                            .placeholder(R.drawable.user)
                            .into(binding.imgPerfil)
                    } catch (e: Exception) {
                        Toast.makeText(mContext, "${e.message}", Toast.LENGTH_SHORT).show()
                    }


                    // Limpiar el LinearLayout antes de agregar nuevas vistas
                    binding.linearTrabajos.removeAllViews()

                    // Agregar trabajos al LinearLayout
                    for (trabajo in trabajosSeleccionados) {
                        val icono = trabajo["icono"] ?: ""
                        val nombreTrabajo = trabajo["nombre"] ?: "Nombre del trabajo"

                        // Crear una nueva vista para cada trabajo
                        val trabajoView = LayoutInflater.from(mContext).inflate(R.layout.item_trabajo, binding.linearTrabajos, false)

                        // Referencias a los elementos de la vista personalizada
                        val iconoImageView: ImageView = trabajoView.findViewById(R.id.iconoImageView)
                        val nombreTextView: TextView = trabajoView.findViewById(R.id.nombreTextView)

                        if (icono.isNotEmpty()) {
                            // Cargar la imagen del icono usando Glide si hay una URL de icono
                            Glide.with(mContext)
                                .load(icono)
                                .into(iconoImageView)
                        } else {
                            // Si no hay URL de icono, ocultar el ImageView
                            iconoImageView.visibility = View.GONE
                        }

                        // Establecer el nombre del trabajo
                        nombreTextView.text = nombreTrabajo

                        // Agregar la vista al LinearLayout
                        binding.linearTrabajos.addView(trabajoView)
                    }

                    // Configurar el botón Ver CV
                    if (cvUrl.isNullOrEmpty()) {
                        binding.btnVerCv.visibility = View.GONE
                    } else {
                        binding.btnVerCv.visibility = View.VISIBLE
                        binding.btnVerCv.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cvUrl))
                            startActivity(intent)
                        }
                    }


                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, "Error al cargar los datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun verCV() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("estudiantes")
            .document(firebaseAuth.uid!!)
            .get()
            .addOnSuccessListener { document ->
                val cvUrl = document.getString("hojadevida")
                if (!cvUrl.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cvUrl))
                    startActivity(intent)
                } else {
                    Toast.makeText(mContext, "No hay currículum disponible", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, "Error al cargar el currículum: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}




