package com.unichamba.Fragmentos

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.SubMenu
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.unichamba.R
import com.unichamba.adapter.OfertaAdapter
import com.unichamba.model.Oferta
import android.widget.ProgressBar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore

class FragmentMisOfertas : Fragment(R.layout.fragment_mis_ofertas) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var ofertaAdapter: OfertaAdapter
    private lateinit var progressBar: ProgressBar
    private var ofertasList: MutableList<Oferta> = mutableListOf()
    private val selectedOptions = mutableSetOf<String>()

    private fun cargarOfertas() {
        progressBar.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("anuncios")
            .get()
            .addOnSuccessListener { result ->
                ofertasList.clear()
                for (document in result) {
                    Log.d("FragmentMisOfertas", "Document ID: ${document.id}, Data: ${document.data}")
                    try {
                        val oferta = document.toObject(Oferta::class.java)
                        ofertasList.add(oferta)
                    } catch (e: Exception) {
                        Log.e("FragmentMisOfertas", "Error deserializando oferta: ${document.id}", e)
                    }
                }

                // Ordenar la lista alfabéticamente por el nombre de la oferta
                ofertasList.sortBy { it.quienPublica }

                // Configurar el adaptador del RecyclerView
                ofertaAdapter = OfertaAdapter(ofertasList)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = ofertaAdapter
                progressBar.visibility = View.GONE

                // Filtrar ofertas por carrera después de que se hayan cargado los datos
                filterOffersByCarrera()
            }
            .addOnFailureListener { exception ->
                Log.e("FragmentMisOfertas", "Error obteniendo documentos", exception)
                progressBar.visibility = View.GONE
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener referencias a los elementos de la vista
        recyclerView = view.findViewById(R.id.recycler_view_ofertas)
        searchView = view.findViewById(R.id.search_view)
        progressBar = view.findViewById(R.id.progress_bar)
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawer_layout)
        val btnOpenMenu: ImageButton = view.findViewById(R.id.btn_open_menu)
        val navView: NavigationView = view.findViewById(R.id.nav_view)

        // Configurar el clic del botón para abrir el menú
        btnOpenMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Configurar los CheckBoxes
        obtenerCheckBoxesFromFirebase(navView)

        // Configurar el SearchView para filtrar las ofertas
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                ofertaAdapter.filter(newText ?: "")
                val noOffersTextView = view.findViewById<TextView>(R.id.no_offers_text)
                noOffersTextView.visibility = if (ofertaAdapter.itemCount == 0 && newText?.isNotEmpty() == true) View.VISIBLE else View.GONE
                return true
            }
        })

        // Cargar ofertas desde Firebase
        cargarOfertas()
    }

    private fun addItemsToMenu(
        menu: SubMenu,
        items: List<String>,
        context: Context,
        isExpanded: Boolean = false
    ) {
        val maxInitialItems = 12
        val displayedItems = if (isExpanded) items else items.take(maxInitialItems)
        val remainingItems = if (isExpanded) emptyList() else items.drop(maxInitialItems)

        menu.clear()

        displayedItems.forEach { item ->
            val checkBox = CheckBox(context).apply {
                text = ""
                isChecked = item in selectedOptions
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedOptions.add(item) else selectedOptions.remove(item)
                    filterOffersByCarrera()
                }
            }
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, item).actionView = checkBox
        }

        val textView = TextView(context).apply {
            text = if (isExpanded) "Ver menos" else "Ver más"
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setOnClickListener {
                addItemsToMenu(menu, items, context, !isExpanded)
            }
        }
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, " ").actionView = textView

        val clearButton = Button(context).apply {
            text = "Limpiar"
            setOnClickListener {
                selectedOptions.clear()
                filterOffersByCarrera()
                menu.clear()  // Clear the menu to remove all existing checkboxes
                addItemsToMenu(menu, items, context, false)  // Re-add the items to the menu
            }
        }
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, " ").actionView = clearButton
    }

    private fun obtenerCheckBoxesFromFirebase(navView: NavigationView) {
        val menu = navView.menu
        val carrerasRef = FirebaseFirestore.getInstance().collection("carreras")

        val carrerasSubMenu = menu.addSubMenu("Carreras")

        carrerasRef.get()
            .addOnSuccessListener { result ->
                val carrerasList = result.map { it.getString("carrera") ?: "" }.sorted()
                context?.let { ctx ->
                    addItemsToMenu(
                        carrerasSubMenu,
                        carrerasList,
                        ctx
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FragmentMisOfertas", "Error getting documents: ", exception)
            }
    }

    private fun filterOffersByCarrera() {
        val filteredList = if (selectedOptions.isEmpty()) {
            ofertasList
        } else {
            ofertasList.filter { oferta ->
                val carrerasList = (oferta.carrera as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                carrerasList.any { it in selectedOptions }
            }
        }

        ofertaAdapter.updateList(filteredList)

        val noOffersTextView = view?.findViewById<TextView>(R.id.no_offers_text)
        if (filteredList.isEmpty()) {
            noOffersTextView?.visibility = View.VISIBLE
        } else {
            noOffersTextView?.visibility = View.GONE
        }
    }
}
