package com.unichamba

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class CustomToast(private val context: Context, private val message: String) : Toast(context) {

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.toast_layout, null)

        val textView = view.findViewById<TextView>(R.id.txtMensaje)
        textView.text = message

        val imageView = view.findViewById<ImageView>(R.id.imgIcono)
        imageView.setImageResource(R.drawable.logo4)

        setView(view)
        setDuration(Toast.LENGTH_SHORT)
    }
}
