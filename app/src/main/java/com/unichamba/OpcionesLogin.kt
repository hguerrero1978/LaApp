package com.unichamba

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.unichamba.databinding.ActivityOpcionesLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.unichamba.Fragmentos.FragmentJovenes

class OpcionesLogin : AppCompatActivity() {

    private lateinit var binding: ActivityOpcionesLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignIntent: GoogleSignInClient
    private lateinit var progressDialog:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       /* if (FirebaseAuth.getInstance().currentUser == null) {
            // No logueado, redirigir a la pantalla de login
            startActivity(Intent(this, OpcionesLogin::class.java))
            finish()
            return
        }*/

        binding= ActivityOpcionesLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        progressDialog=ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth=FirebaseAuth.getInstance()
        comprobarSesion()

        val gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignIntent=GoogleSignIn.getClient(this,gso)


        binding.IngresarGoogle.setOnClickListener {
            googleLogin()
        }
        /*binding.registrarmeR.setOnClickListener {
            startActivity(Intent(this@OpcionesLogin,Registro_reclutador::class.java))
        }*/



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun googleLogin() {
        // Primero, cerrar la sesión de cualquier cuenta de Google existente
        mGoogleSignIntent.signOut().addOnCompleteListener {
            // Una vez que la sesión se haya cerrado, iniciar el intento de inicio de sesión
            val googleSignInIntent = mGoogleSignIntent.signInIntent
            googleSignInARL.launch(googleSignInIntent)
        }
    }

    private val googleSignInARL=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){resultado->
        if(resultado.resultCode== RESULT_OK){
            val data=resultado.data
            val task=GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val cuenta=task.getResult(ApiException::class.java)
                authenticacionGoogle(cuenta.idToken)
            }catch (e:Exception){
                val customMessage = "${e.message}"
                val customToast = CustomToast(this, customMessage)
                customToast.show()
            }
        }
    }

    private fun authenticacionGoogle(idToken: String?) {
        if (idToken != null) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    val firebaseUser = firebaseAuth.currentUser
                    val email = firebaseUser!!.email
                    // Verificar si el correo electrónico cumple con el formato de empleado
                    if (email!!.matches(Regex("^[a-zA-Z]+\\.[a-zA-Z]+@ues\\.edu\\.sv$"))) {
                        // Enviar una alerta al usuario si es empleado
                        //Toast.makeText(this@OpcionesLogin, "Inicio de sesión fallido,dirígete al registro del reclutador si deseas ingresar de esa forma", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@OpcionesLogin, MainActivityR::class.java))
                    } else {
                        // Redirigir a MainActivity si no es empleado
                        startActivity(Intent(this@OpcionesLogin, MainActivity::class.java))
                    }
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@OpcionesLogin, "Inicio de sesión fallido", Toast.LENGTH_SHORT).show()
                }
        }
    }
                    /*when {
                        email.matches(Regex("[^\\.]+@ues\\.edu\\.sv")) -> {
                            // Es un estudiante
                            startActivity(Intent(this@OpcionesLogin, MainActivity::class.java))
                        }
                        email.matches(Regex("[^\\.]+\\.[^\\.]+@ues\\.edu\\.sv")) -> {
                            // Es un empleado
                            startActivity(Intent(this@OpcionesLogin, MainActivityR::class.java))
                        }
                        else -> {
                            // Es un usuario externo
                            // Permitir ver el detalle de un perfil de estudiante sin registro o publicación
                            startActivity(Intent(this@OpcionesLogin, MainActivity::class.java))
                        }
                    }
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@OpcionesLogin, "Inicio de sesión fallido", Toast.LENGTH_SHORT)
                        .show()
                }*/

    /*
    private fun llenarInfoBD() {
        progressDialog.setMessage("Guardando información")

        val tiempo=Constantes.obtenerTiempoDis()
        val emailUsuario=firebaseAuth.currentUser!!.email
        val uidUsuario=firebaseAuth.uid
        val nombreUsuario=firebaseAuth.currentUser?.displayName

        val hashMap=HashMap<String,Any>()
        hashMap["nombres"]="${nombreUsuario}"
        hashMap["codigoTelefono"]=""
        hashMap["telefono"]=""
        hashMap["urlImagenPerfil"]=""
        hashMap["proveedor"]="Google"
        hashMap["escribiendo"]=""
        hashMap["tiempo"]=tiempo
        hashMap["online"]=true
        hashMap["email"]="${emailUsuario}"
        hashMap["uid"]="${uidUsuario}"
        hashMap["fecha_nac"]=""

        val ref= FirebaseDatabase.getInstance().getReference("estudiantes")
        ref.child(uidUsuario!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this,
                    "No se registró debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    */

    private fun comprobarSesion(){
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /* */

}