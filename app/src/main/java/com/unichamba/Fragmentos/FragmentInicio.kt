package com.unichamba.Fragmentos

import JovenesAdapterInicio
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unichamba.JovenesDetalleActivity
import com.unichamba.OpcionesLogin
import com.unichamba.R
import com.unichamba.model.Oferta
import com.unichamba.adapter.OfertaAdapterInicio
import com.unichamba.model.Joven
import com.unichamba.model.Trabajo

class FragmentInicio : Fragment(R.layout.fragment_inicio) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ofertaAdapterInicio: OfertaAdapterInicio
    private var ofertasList: MutableList<Oferta> = mutableListOf()

    private lateinit var recyclerViewJovenes: RecyclerView
    private lateinit var jovenesAdapterInicio: JovenesAdapterInicio
    private var listaJovenes: MutableList<Joven> = mutableListOf()
    private lateinit var progressBarJovenesInicio: ProgressBar
    private lateinit var progressBarOfertasInicio: ProgressBar
    private lateinit var auth: FirebaseAuth

    interface OnVerJovenesClickListener {
        fun onVerJovenesClicked()
    }

    interface OnVerOfertasClickListener {
        fun onVerOfertasClicked()
    }

    private var verJovenesListener: OnVerJovenesClickListener? = null
    private var verOfertasListener: OnVerOfertasClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnVerJovenesClickListener) {
            verJovenesListener = context
        } else {
            throw ClassCastException("$context debe implementar OnVerJovenesClickListener")
        }

        if (context is OnVerOfertasClickListener) {
            verOfertasListener = context
        } else {
            throw ClassCastException("$context debe implementar OnVerOfertasClickListener")
        }
    }

    private fun cargarOfertas() {
        progressBarOfertasInicio.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        db.collection("anuncios")
            .limit(6)
            .get()
            .addOnSuccessListener { result ->
                ofertasList.clear()
                for (document in result) {
                    val oferta = document.toObject(Oferta::class.java)
                    ofertasList.add(oferta)
                }

                // Configurar el adaptador del RecyclerView
                ofertaAdapterInicio.notifyDataSetChanged()
                // Ocultar la barra de progreso en el hilo principal
                activity?.runOnUiThread {
                    progressBarOfertasInicio.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                // Manejar errores aquí
                Log.d("FragmentInicio", "Error getting documents: ", exception)
                // Ocultar la barra de progreso en el hilo principal
                activity?.runOnUiThread {
                    progressBarOfertasInicio.visibility = View.GONE
                }
            }
    }

    private fun cargarJovenes() {
        progressBarJovenesInicio.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        db.collection("estudiantes")
            .limit(12)
            .get()
            .addOnSuccessListener { result ->
                listaJovenes.clear()
                for (document in result) {
                    val id = document.id
                    val nombre = document.getString("nombre") ?: ""
                    val trabajosList = document.get("trabajos") as? List<Map<String, String>> ?: emptyList()
                    val trabajos = trabajosList.map { Trabajo(it["icono"] ?: "", it["nombre"] ?: "") }
                    val imagen = document.getString("imageUrl") ?: ""
                    val joven = Joven(id ,nombre, trabajos, imagen, "", "", "")
                    listaJovenes.add(joven)
                }
                jovenesAdapterInicio.notifyDataSetChanged()
                // Ocultar la barra de progreso en el hilo principal
                activity?.runOnUiThread {
                    progressBarJovenesInicio.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                // Manejar errores aquí
                Log.d("FragmentInicio", "Error getting documents: ", exception)
                // Ocultar la barra de progreso en el hilo principal
                activity?.runOnUiThread {
                    progressBarJovenesInicio.visibility = View.GONE
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_inicio, container, false)
        progressBarJovenesInicio = rootView.findViewById(R.id.progressBarJovenesInicio)
        progressBarOfertasInicio = rootView.findViewById(R.id.progressBarOfertasInicio)
        auth = FirebaseAuth.getInstance()


        recyclerView = rootView.findViewById(R.id.recycler_view_ofertas)
        val layoutManager = GridLayoutManager(context, 2, RecyclerView.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager


        ofertaAdapterInicio = OfertaAdapterInicio(ofertasList)
        recyclerView.adapter = ofertaAdapterInicio

        cargarOfertas()

        recyclerViewJovenes = rootView.findViewById(R.id.recycler_jovenes_inicio)
        recyclerViewJovenes.layoutManager = GridLayoutManager(context, 3, GridLayoutManager.HORIZONTAL, false)
        jovenesAdapterInicio = JovenesAdapterInicio(listaJovenes){ joven ->
            if (auth.currentUser != null) {
                // Usuario logeado, redirigir a detalles del joven
                val intent = Intent(activity, JovenesDetalleActivity::class.java).apply {
                    putExtra(JovenesDetalleActivity.EXTRA_ID, joven.id)
                }
                startActivity(intent)
            } else {
                // Usuario no logeado, redirigir a la vista de login
                val intent = Intent(activity, OpcionesLogin::class.java).apply {
                    putExtra(JovenesDetalleActivity.EXTRA_ID, joven.id) // Guardar el ID del joven
                }
                startActivity(intent)
            }
        }
        recyclerViewJovenes.adapter = jovenesAdapterInicio

        cargarJovenes()

        val btnJovenes = rootView.findViewById<Button>(R.id.btnJovenes)
        btnJovenes.setOnClickListener {
            verJovenesListener?.onVerJovenesClicked()
        }

        val btnOfertas = rootView.findViewById<Button>(R.id.btnOfertas)
        btnOfertas.setOnClickListener {
            verOfertasListener?.onVerOfertasClicked()
        }
        return rootView
    }

    override fun onDetach() {
        super.onDetach()
        verJovenesListener = null
        verOfertasListener = null
    }
}

