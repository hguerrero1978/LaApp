package com.unichamba.Fragmentos

import JovenesAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.SubMenu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unichamba.R
import com.unichamba.model.Joven
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unichamba.JovenesDetalleActivity
import com.unichamba.OpcionesLogin
import com.unichamba.model.Trabajo

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FragmentJovenes : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var jovenesAdapter: JovenesAdapter
    private var listaJovenes: MutableList<Joven> = mutableListOf()
    private val selectedOptions = mutableSetOf<String>()
    private lateinit var progressBarJovenes: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_jovenes, container, false)

        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawer_layout)
        val btnOpenMenu: ImageButton = view.findViewById(R.id.btn_open_menu)
        val navView: NavigationView = view.findViewById(R.id.nav_view)
        val recyclerJovenes: RecyclerView = view.findViewById(R.id.recycler_jovenes)
        val searchView: SearchView = view.findViewById(R.id.search_joven)  // Añadido SearchView
        progressBarJovenes = view.findViewById(R.id.progressBarJovenes)


        // Configurar el RecyclerView
        recyclerJovenes.layoutManager = LinearLayoutManager(context)

        // Inicializar el adaptador y asignarlo al RecyclerView
        jovenesAdapter = JovenesAdapter(listaJovenes) { id ->
            // Verificar si el usuario está autenticado
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                // Usuario autenticado, abrir la actividad de detalles del joven
                val intent = Intent(activity, JovenesDetalleActivity::class.java)
                intent.putExtra(JovenesDetalleActivity.EXTRA_ID, id)
                startActivity(intent)
            } else {
                // Usuario no autenticado, redirigir a la actividad de opciones de login
                val intent = Intent(activity, OpcionesLogin::class.java)
                startActivity(intent)
            }
        }
        recyclerJovenes.adapter = jovenesAdapter

        // Obtener los datos de los jóvenes desde Firebase
        obtenerJovenes()

        // Configurar el clic del botón para abrir el menú
        btnOpenMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Configurar los CheckBoxes dinámicamente desde Firebase
        obtenerCheckBoxesFromFirebase(navView)

        // Configurar el SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // No se necesita acción aquí
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                jovenesAdapter.filter(newText ?: "")
                return true
            }
        })

        return view
    }

    private fun addItemsToMenu(
        menu: SubMenu,
        items: List<String>,
        context: Context,
        collectionName: String,
        isExpanded: Boolean = false
    ) {
        val maxInitialItems = 6
        val displayedItems = if (isExpanded) items else items.take(maxInitialItems)
        val remainingItems = if (isExpanded) emptyList() else items.drop(maxInitialItems)

        menu.clear()

        displayedItems.forEach { item ->
            val checkBox = CheckBox(context).apply {
                text = "" // Oculta el nombre del CheckBox
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedOptions.add(item) else selectedOptions.remove(item)
                    filterRecyclerView()
                }
            }
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, item).actionView = checkBox
        }

        if (remainingItems.isNotEmpty()) {
            val textView = TextView(context).apply {
                text = "Ver más"
                setOnClickListener {
                    addItemsToMenu(menu, items, context, collectionName, isExpanded = true)
                }
            }
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, " ").actionView = textView
        }

        if (isExpanded) {
            val textView = TextView(context).apply {
                text = "Ver menos"
                setOnClickListener {
                    addItemsToMenu(menu, items, context, collectionName, isExpanded = false)
                }
            }
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, " ").actionView = textView
        }

        val clearButton = Button(context).apply {
            text = "Limpiar"
            setOnClickListener {
                clearSelections(menu)
            }
        }
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, " ").actionView = clearButton
    }

    private fun clearSelections(menu: SubMenu) {
        selectedOptions.clear()
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val checkBox = item.actionView as? CheckBox
            checkBox?.isChecked = false
        }
        filterRecyclerView()
    }

    private fun obtenerCheckBoxesFromFirebase(navView: NavigationView) {
        val menu = navView.menu
        val carrerasRef = FirebaseFirestore.getInstance().collection("carreras")
        val trabajosRef = FirebaseFirestore.getInstance().collection("trabajos")
        val municipiosRef = FirebaseFirestore.getInstance().collection("municipios")

        menu.clear()

        val carrerasSubMenu = menu.addSubMenu("Carreras")
        val trabajosSubMenu = menu.addSubMenu("Trabajos")
        val municipiosSubMenu = menu.addSubMenu("Municipios")

        carrerasRef.get()
            .addOnSuccessListener { result ->
                val carrerasList = result.map { it.getString("carrera") ?: "" }.sorted()
                context?.let { ctx ->
                    addItemsToMenu(
                        carrerasSubMenu,
                        carrerasList,
                        ctx,
                        "carreras"
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FragmentJovenes", "Error getting documents: ", exception)
            }

        trabajosRef.get()
            .addOnSuccessListener { result ->
                val trabajosList = result.map { it.getString("nombre") ?: "" }.sorted()
                context?.let { ctx ->
                    addItemsToMenu(
                        trabajosSubMenu,
                        trabajosList,
                        ctx,
                        "trabajos"
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FragmentJovenes", "Error getting documents: ", exception)
            }

        municipiosRef.get()
            .addOnSuccessListener { result ->
                val municipiosList = result.map { it.getString("municipio") ?: "" }.sorted()
                context?.let { ctx ->
                    addItemsToMenu(
                        municipiosSubMenu,
                        municipiosList,
                        ctx,
                        "municipios"
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FragmentJovenes", "Error getting documents: ", exception)
            }
    }

    private fun obtenerJovenes() {
        progressBarJovenes.visibility = View.VISIBLE
        val estudiantesRef = FirebaseFirestore.getInstance().collection("estudiantes")
        estudiantesRef.get()
            .addOnSuccessListener { result ->
                val lista = mutableListOf<Joven>()
                for (document in result) {
                    val id = document.id
                    val nombre = document.getString("nombre") ?: ""
                    val trabajosList = document.get("trabajos") as? List<Map<String, String>> ?: emptyList()
                    val trabajos = trabajosList.map { Trabajo(it["icono"] ?: "", it["nombre"] ?: "") }
                    val carrera = document.getString("carrera") ?: ""
                    val municipio = document.getString("municipio") ?: ""
                    val descripcion = document.getString("acercaDe") ?: ""
                    val imagen = document.getString("imageUrl") ?: ""
                    val joven = Joven(id,nombre, trabajos, imagen, carrera, municipio, descripcion)
                    lista.add(joven)
                }
                listaJovenes.clear()
                listaJovenes.addAll(lista)
                jovenesAdapter.notifyDataSetChanged()
                // Ocultar la barra de progreso en el hilo principal
                activity?.runOnUiThread {
                    progressBarJovenes.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FragmentJovenes", "Error getting documents: ", exception)
                // Ocultar la barra de progreso en el hilo principal
                activity?.runOnUiThread {
                    progressBarJovenes.visibility = View.GONE
                }
            }
    }



    private fun filterRecyclerView() {
        val filteredList = listaJovenes.filter { joven ->
            selectedOptions.isEmpty() ||
                    selectedOptions.contains(joven.carrera) ||
                    joven.trabajos.any { trabajo -> selectedOptions.contains(trabajo.nombre) } ||
                    selectedOptions.contains(joven.municipio)
        }
        Log.d("Filtro", "Opciones seleccionadas: $selectedOptions")
        Log.d("Filtro", "Lista filtrada: $filteredList")
        jovenesAdapter.updateList(filteredList)
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentJovenes().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
