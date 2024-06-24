package com.unichamba.Opciones_login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unichamba.MainActivity
import com.unichamba.R
import com.unichamba.RecuperarPassword
import com.unichamba.Registro_email
import com.unichamba.databinding.ActivityLoginEmailBinding
import com.google.firebase.auth.FirebaseAuth

class Login_email : AppCompatActivity() {

    private lateinit var binding : ActivityLoginEmailBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginEmailBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth=FirebaseAuth.getInstance()
        progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.BtnIngresar.setOnClickListener {
            validarinfo()
        }

        binding.TxtRegistrarme.setOnClickListener{
            startActivity(Intent(this@Login_email, Registro_email::class.java))
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.TvRecuperar.setOnClickListener {
            startActivity(Intent(this@Login_email, RecuperarPassword::class.java))
        }

    }
    private var email=""
    private var password=""
    private fun validarinfo() {
        email=binding.ETEmail.text.toString().trim()
        password=binding.ETPassword.text.toString().trim()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.ETEmail.error="Email inválido"
            binding.ETEmail.requestFocus()
        }
        else if(email.isEmpty()){
            binding.ETEmail.error="Ingrese email"
            binding.ETEmail.requestFocus()
        }
        else if(password.isEmpty()){
            binding.ETPassword.error="Ingrese password"
            binding.ETPassword.requestFocus()
        }else{
            loginUsusario()
        }
    }

    private fun loginUsusario() {
        progressDialog.setMessage("")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity()
                Toast.makeText(this,
                    "Bienvenido(a)",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this,
                    "No se pudo iniciar sesion debido a ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }
}