package com.unichamba

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unichamba.databinding.ActivityRegistroEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Registro_email : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRegistroEmailBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth= FirebaseAuth.getInstance()
        progressDialog=ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)
        binding.BtnRegistrar.setOnClickListener {
            validarInfo()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private var email=""
    private var password=""
    private var r_password=""
    private fun validarInfo() {
        email=binding.ETEmail.text.toString().trim()
        password=binding.ETPassword.text.toString().trim()
        r_password=binding.EtRPassword.text.toString().trim()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.ETEmail.error="Email Invalido"
            binding.ETEmail.requestFocus()
        }
        else if(email.isEmpty()){
            binding.ETEmail.error="Ingrese email"
            binding.ETEmail.requestFocus()
        }
        else if(password.isEmpty()){
            binding.ETPassword.error="Ingrese password"
            binding.ETPassword.requestFocus()
        }
        else if(r_password.isEmpty()){
            binding.EtRPassword.error="Repita el password"
            binding.EtRPassword.requestFocus()
        }
        else if(password != r_password){
            binding.EtRPassword.error="No coinicden"
            binding.EtRPassword.requestFocus()
        }
        else{
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        progressDialog.setMessage("Creando cuenta")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                llenarInfoBD()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                val customMessage = "No se registró debido a ${e.message}"
                val customToast = CustomToast(this, customMessage)
                customToast.show()
            }
    }

    private fun llenarInfoBD(){
        progressDialog.setMessage("Guardando información")

        val tiempo=Constantes.obtenerTiempoDis()
        val emailUsuario=firebaseAuth.currentUser!!.email
        val uidUsuario=firebaseAuth.uid

        val hashMap=HashMap<String,Any>()
        hashMap["nombres"]=""
        hashMap["codigoTelefono"]=""
        hashMap["telefono"]=""
        hashMap["urlImagenPerfil"]=""
        hashMap["proveedor"]="Email"
        hashMap["escribiendo"]=""
        hashMap["tiempo"]=tiempo
        hashMap["online"]=true
        hashMap["email"]="${emailUsuario}"
        hashMap["uid"]="${uidUsuario}"
        hashMap["fecha_nac"]=""

        val ref=FirebaseDatabase.getInstance().getReference("usuarios")
        ref.child(uidUsuario!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                val customMessage = "No se registró debido a ${e.message}"
                val customToast = CustomToast(this, customMessage)
                customToast.show()
            }
    }
}