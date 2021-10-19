package com.example.bdtkotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bdtkotlin.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage


        //SQL Database
        val database = AppDatabase.getDatabase(this)

        //Activar Guardado para modo Offline
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings



        //Check if its logged before
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        if(sharedPreferences.getBoolean("isLogged", false)) {
            val intent = Intent(this, HomeView::class.java)
            startActivity(intent)
            finish()
        }

        //BTN Login
        binding.buttonAcceder.setOnClickListener {
            val email = binding.inputAccederUser.text.toString()
            val pass = binding.inputAccederPassword.text.toString()

            if (email.isNotEmpty() and pass.isNotEmpty()) {
                login(email, pass)
            }
        }

        //BTN Register
        binding.textViewRegistro.setOnClickListener {
            val intent = Intent(this, RegisterView::class.java)
            startActivity(intent)
        }

        //BTN Recover password
        binding.textViewRecuperar.setOnClickListener {
            val intent = Intent(this, RecoverPassword::class.java)
            startActivity(intent)
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("DBG", "signInWithEmail:success")
                    isVerified()
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d("DBG", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Credenciales invalidas", Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }
            }
    }

    private fun isVerified() {
        val user = auth.currentUser
        if (user != null) {
            if (user.isEmailVerified) {
                Log.d("DBG", "Is verified")
                fetchUserData(user.uid)
            }
            else {
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Verificación")
                    .setMessage("Necesita verificarse para continuar")
                    .setNeutralButton("OK") { view, _ ->
                        view.dismiss()

                        val intent = Intent(this, RegisterVerificationView::class.java)
                        startActivity(intent)
                    }
                    .setCancelable(false)
                    .create()
                dialog.show()
            }
        }
    }

    private fun fetchUserData(UID:String) {
        val user = auth.currentUser
        val docRef = db.collection("Users").document(UID)
        docRef.get()
            .addOnSuccessListener { document ->
                Log.d("DBG", "Document ${document.data}")

                val job = GlobalScope.launch(Dispatchers.Default) {
                    downloadImage(user!!.uid, document.data?.get("Personal") as HashMap<String, Object>, document.data?.get("Home") as HashMap<String, Object>, document.data?.get("newUser").toString().toBoolean())
                }
                runBlocking {
                    job.join()
                }
            }
            .addOnFailureListener { e ->
                Log.d("DBG", "Error fetching document ${e.localizedMessage}")
            }
    }

    private fun downloadImage(userUID: String, personal:HashMap<String, Object>, home:HashMap<String, Object>, newUser: Boolean) {
        val storageRef = storage.reference
        var pathRef = storageRef.child("$userUID/profilePic.jpg")

        val ONE_MEGABYTE: Int = 1024*1024

        pathRef.getBytes(ONE_MEGABYTE.toLong())
            .addOnSuccessListener {
                Log.d("DBG", "Image downloaded")

                val myByteArray = it
                val imgString = myByteArray.toString()

                sqlManager(personal, home, newUser, imgString)
            }
            .addOnFailureListener {
                // Handle any errors
                Log.d("DBGF", "Descarga fallida")
                val imgString = ""
                sqlManager(personal, home, newUser, imgString)
            }
    }

    private fun sqlManager(personal:HashMap<String, Object>, home:HashMap<String, Object>, newUser: Boolean, img:String) {
        val database = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {
            async {
                try {
                    database.userDataPersonal().insertAll(
                        sqlContainerPersonal(
                            0,
                            personal["Name"].toString(),
                            personal["Email"].toString(),
                            personal["AP"].toString(),
                            personal["AM"].toString(),
                            personal["Phone"].toString(),
                            personal["Address"].toString(),
                            personal["Gender"].toString().toInt()))
                } catch (e: Exception) {
                    Log.d("DBG", "SQL: Error Personal ${e.localizedMessage}")
                }


                val tags = home["Tags"] as List<String>
                try {
                    database.userDataHome().insertAll(
                        sqlContainerHome(
                            0,
                            home["Name"].toString(),
                            home["Banner"].toString(),
                            home["Score"].toString().toFloat(),
                            home["JobsDone"].toString().toInt(),
                            home["Credits"].toString().toInt(),
                            tags[0],
                            tags[1],
                            tags[2]))
                } catch (e: Exception) {
                    Log.d("DBG", "SQL: Error Home ${e.localizedMessage}")
                }
            }.await()
        }

        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply {
            putBoolean("isLogged", true)
        }.apply()


        if(!newUser) {
            //Not new user
            val intent = Intent(this, HomeView::class.java)
            startActivity(intent)
            finish()
        }
        else {
            //New user
            val intent = Intent(this, SettingsProfile::class.java)
            intent.putExtra("newUser", newUser)
            startActivity(intent)
            finish()
        }

        Log.d("DBG", "SQL Success")
    }



}