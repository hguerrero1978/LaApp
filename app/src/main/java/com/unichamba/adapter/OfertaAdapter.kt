package com.unichamba.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unichamba.OfertaDetalleActivity
import com.unichamba.R
import com.unichamba.databinding.ItemOfertaBinding
import com.unichamba.model.Oferta
import com.bumptech.glide.Glide

class OfertaAdapter(private var ofertas: List<Oferta>) : RecyclerView.Adapter<OfertaAdapter.OfertaViewHolder>() {

    private var originalOfertas: List<Oferta> = ofertas

    inner class OfertaViewHolder(val binding: ItemOfertaBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val oferta = ofertas[position]
                val carrerasString = oferta.carrera.joinToString(", ") // Concatena las carreras en un solo String
                val carrerasList = carrerasString.split(",").map { it.trim() }
                val context = v?.context
                val intent = Intent(context, OfertaDetalleActivity::class.java).apply {
                    putExtra(OfertaDetalleActivity.EXTRA_quienPublica, oferta.quienPublica)
                    putExtra(OfertaDetalleActivity.EXTRA_TELEFONO, oferta.telefono)
                    putExtra(OfertaDetalleActivity.EXTRA_DESCRIPTION, oferta.description)
                    putExtra(OfertaDetalleActivity.EXTRA_CARRERA, oferta.carrera.toTypedArray())
                    putExtra(OfertaDetalleActivity.EXTRA_IMAGEN, oferta.imagenSmall) // Asegúrate de que este campo existe
                }
                context?.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfertaViewHolder {
        val binding = ItemOfertaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OfertaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfertaViewHolder, position: Int) {
        val oferta = ofertas[position]
        holder.bind(oferta)
    }

    override fun getItemCount(): Int = ofertas.size

    fun updateList(newList: List<Oferta>) {
        ofertas = newList
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalOfertas
        } else {
            originalOfertas.filter {
                it.quienPublica.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            }
        }
        updateList(filteredList)
    }

    private fun OfertaViewHolder.bind(oferta: Oferta) {
        binding.quienPublica.text = oferta.quienPublica
        binding.description.text = oferta.description

        // Formatea la lista de carreras como una cadena separada por comas
        val carrerasText = oferta.carrera.joinToString(", ")

        // Muestra la lista de carreras en el TextView correspondiente
        binding.carrera.text = carrerasText

        Glide.with(binding.root.context)
            .load(oferta.imagenSmall)
            .placeholder(R.drawable.ic_cuenta) // Placeholder mientras se carga la imagen
            .error(R.drawable.barra) // Imagen de error si falla la carga
            .into(binding.imagenSmall) // ImageView donde se carga la imagen
    }
}

