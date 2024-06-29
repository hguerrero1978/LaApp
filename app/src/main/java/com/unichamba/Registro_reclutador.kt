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
import com.unichamba.databinding.ActivityRegistroReclutadorBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class Registro_reclutador : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroReclutadorBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignIntent: GoogleSignInClient
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRegistroReclutadorBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        progressDialog=ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth=FirebaseAuth.getInstance()
        comprobarSesion()

        val gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignIntent=GoogleSignIn.getClient(this,gso)



        binding.IngresarGoogleR.setOnClickListener {
            googleLogin()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun googleLogin() {
        val googleSignInIntent=mGoogleSignIntent.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    private val googleSignInARL=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ resultado->
        if(resultado.resultCode== RESULT_OK){
            val data=resultado.data
            val task= GoogleSignIn.getSignedInAccountFromIntent(data)
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
                        // Redirigir a MainActivityR si es empleado
                        startActivity(Intent(this@Registro_reclutador, MainActivityR::class.java))
                    } /*else {
                        // Redirigir a MainActivity si no es empleado
                        startActivity(Intent(this@Registro_reclutador, MainActivity::class.java))
                    }*/
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@Registro_reclutador, "Inicio de sesión fallido", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    /*private fun llenarInfoBD() {
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

        val ref= FirebaseDatabase.getInstance().getReference("usuarios")
        ref.child(uidUsuario!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this,MainActivityR::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                val customMessage = "No se registró debido a ${e.message}"
                val customToast = CustomToast(this, customMessage)
                customToast.show()
            }
    }*/

    private fun comprobarSesion(){
        if(firebaseAuth.currentUser!=null){
            startActivity(Intent(this,MainActivityR::class.java))
            finishAffinity()
        }
    }
}