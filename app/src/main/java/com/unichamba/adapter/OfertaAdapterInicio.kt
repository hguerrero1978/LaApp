package com.unichamba.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unichamba.OfertaDetalleActivity
import com.unichamba.databinding.ItemOfertaBinding
import com.unichamba.model.Oferta
import com.unichamba.R

class OfertaAdapterInicio(private var ofertas: List<Oferta>) : RecyclerView.Adapter<OfertaAdapterInicio.OfertaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfertaViewHolder {
        val binding = ItemOfertaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OfertaViewHolder(binding)
    }

    override fun getItemCount(): Int = ofertas.size

    override fun onBindViewHolder(holder: OfertaViewHolder, position: Int) {
        val oferta = ofertas[position]
        holder.bind(oferta)
    }

    inner class OfertaViewHolder(private val binding: ItemOfertaBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val oferta = ofertas[position]
                    val context = binding.root.context
                    val intent = Intent(context, OfertaDetalleActivity::class.java).apply {
                        putExtra(OfertaDetalleActivity.EXTRA_quienPublica, oferta.quienPublica)
                        putExtra(OfertaDetalleActivity.EXTRA_DESCRIPTION, oferta.description)
                        putExtra(OfertaDetalleActivity.EXTRA_TELEFONO, oferta.telefono)
                        putExtra(OfertaDetalleActivity.EXTRA_CARRERA, oferta.carrera.toTypedArray())
                        putExtra(OfertaDetalleActivity.EXTRA_IMAGEN, oferta.imagen)
                    }

                    context.startActivity(intent)
                }
            }
        }

        fun bind(oferta: Oferta) {
            binding.description.text = oferta.description
            binding.quienPublica.text = oferta.quienPublica

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
}

