import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unichamba.R
import com.unichamba.model.Joven
import com.unichamba.model.Trabajo

class JovenesAdapter(private var listaJovenes: List<Joven>, private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<JovenesAdapter.JovenViewHolder>() {

    private var listaFiltrada: List<Joven> = listaJovenes

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JovenViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_joven, parent, false)
        return JovenViewHolder(view)
    }

    override fun onBindViewHolder(holder: JovenViewHolder, position: Int) {
        val joven = listaFiltrada[position]
        holder.bind(joven)

        holder.itemView.setOnClickListener {
            onItemClick(joven.id) // Aquí usamos el ID del documento
        }
    }

    override fun getItemCount(): Int {
        return listaFiltrada.size
    }

    fun updateList(nuevaLista: List<Joven>) {
        listaJovenes = nuevaLista
        listaFiltrada = nuevaLista
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        listaFiltrada = if (query.isEmpty()) {
            listaJovenes
        } else {
            listaJovenes.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.trabajos.any { trabajo ->
                            trabajo.nombre.contains(
                                query,
                                ignoreCase = true
                            )
                        } ||
                        it.carrera.contains(query, ignoreCase = true) ||
                        it.municipio.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    class JovenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.textViewNombre)

        //private val trabajosTextView: TextView = itemView.findViewById(R.id.textViewTrabajo)
        private val carreraTextView: TextView = itemView.findViewById(R.id.textViewCarrera)
        private val municipioTextView: TextView = itemView.findViewById(R.id.textViewMunicipio)
        private val descripcionTextView: TextView = itemView.findViewById(R.id.textViewDescriptionJoven)
        private val imageViewJoven: ImageView = itemView.findViewById(R.id.imageViewJoven)
        private val trabajosIconosContainer: ViewGroup = itemView.findViewById(R.id.trabajosIconosContainer)

        fun bind(joven: Joven) {
            nombreTextView.text = joven.nombre
            //trabajosTextView.text = joven.trabajos.joinToString { it.nombre }
            carreraTextView.text = joven.carrera
            municipioTextView.text = joven.municipio
            descripcionTextView.text = joven.descripcion

            // Usar Glide para cargar la imagen
            Glide.with(itemView.context)
                .load(joven.imagen)
                .into(imageViewJoven)

            // Limpiar el contenedor de iconos antes de agregar nuevos
            trabajosIconosContainer.removeAllViews()

            // Tamaño deseado en dp (24dp en este caso)
            val iconSize = itemView.context.resources.getDimensionPixelSize(R.dimen.icon_size)

            // Agregar iconos dinámicamente
            joven.trabajos.forEach { trabajo ->
                val iconName = trabajo.icono  // Nombre del icono obtenido de Firebase
                val iconResourceId = iconName.let { getIconResource(it) }

                if (iconResourceId != 0) {
                    // Crear un ImageView para el icono
                    val iconoView = ImageView(itemView.context)

                    // Cargar el icono utilizando Glide y especificando el tamaño
                    Glide.with(itemView.context)
                        .load(iconResourceId)
                        .override(iconSize, iconSize)  // Especificar el tamaño deseado
                        .into(iconoView)

                    // Ajustar el tamaño del ImageView manualmente (opcional)
                    iconoView.layoutParams = ViewGroup.LayoutParams(iconSize, iconSize)

                    // Agregar el icono al contenedor
                    trabajosIconosContainer.addView(iconoView)
                }
            }
        }
        // Método para obtener el ID del recurso drawable según el nombre del icono
        private fun getIconResource(iconName: String): Int {
            return itemView.context.resources.getIdentifier(
                iconName,
                "drawable",
                itemView.context.packageName
            )
        }
    }
}
