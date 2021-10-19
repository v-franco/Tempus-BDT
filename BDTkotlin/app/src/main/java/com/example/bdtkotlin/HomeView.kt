package com.example.bdtkotlin

import android.content.Context
import android.content.Intent
import android.opengl.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.bdtkotlin.databinding.ActivityHomeViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.lang.Exception
import java.sql.SQLException
import kotlin.collections.HashMap
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.typeOf


class HomeView : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityHomeViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore


        /*  //Dynamic Lists
        val myItems = Items("Nueva Peticion", "Comprar", 0)
        val listMyItems = listOf(myItems)

        val adapter = AdapterMainActivityNotifications(this, listMyItems)

        binding.listHmNotifications.adapter = adapter
        */


        //SQL
        reloadData()


        binding.menuHmFab.setOnItemClickListener { menuButton ->
            if(menuButton == 0) {
                val intent = Intent(this, SearchView::class.java)
                startActivity(intent)
            }

            if(menuButton == 1) {
                val intent = Intent(this, JobsMain::class.java)
                startActivity(intent)
            }

            if(menuButton == 2) {
                reloadData()
            }

            if(menuButton == 3) {
                val intent = Intent(this, SettingsMain::class.java)
                startActivity(intent)
            }
        }

    }


    private fun loadInterfaceDynamics() {
        val user = auth.currentUser
        val docRef = db.collection("Jobs")

        //Al que trabajo
        docRef
            .whereEqualTo("idTaker", user!!.uid)
            .get()
            .addOnSuccessListener { document ->
                val processData = ClassesContainer().processDynamicsJobs(document)
                val adapter = AdapterMainActivityJobs(this, processData.items, 0)

                binding.listHmJobsExt.adapter = adapter

                binding.listHmJobsExt.setOnItemClickListener { parent, view, position, id ->
                    val state = processData.dataArray[5][position].toInt()

                    if(state == 1) {
                        val intent = Intent(this, JobStatus2User::class.java)
                        intent.putExtra("title", processData.dataArray[0][position])
                        intent.putExtra("name", processData.dataArray[1][position])
                        intent.putExtra("email", processData.dataArray[2][position])
                        intent.putExtra("phone", processData.dataArray[3][position])

                        startActivity(intent)
                    }
                    if(state == 2) {
                        //added state 2
                        val intent = Intent(this, JobStatus3User::class.java)
                        intent.putExtra("title", processData.dataArray[0][position])
                        intent.putExtra("desc", processData.dataArray[4][position])
                        intent.putExtra("UID", processData.dataArray[7][position])
                        startActivity(intent)
                    }
                    if(state == 3) {
                        //added state 2
                        val intent = Intent(this, JobStatus4User::class.java)
                        intent.putExtra("title", processData.dataArray[0][position])
                        startActivity(intent)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.d("DBG", "No hay trabajos")
            }

        //Mi trabajo
        docRef
            .whereEqualTo("idCreator", user!!.uid)
            .get()
            .addOnSuccessListener { document ->
                val processData = ClassesContainer().processDynamicsJobs(document)
                val adapter = AdapterMainActivityJobs(this, processData.items, 1)

                binding.listHmJobsInt.adapter = adapter

                binding.listHmJobsInt.setOnItemClickListener { parent, view, position, id ->
                    val state = processData.dataArray[5][position].toInt()

                    if(state == 0) {
                        val intent = Intent(this, JobStatus1OP::class.java)
                        intent.putExtra("title", processData.dataArray[0][position])
                        startActivity(intent)
                    }

                    else if(state == 1) {
                        db.collection("Users").document(processData.dataArray[6][position]).get()
                            .addOnSuccessListener { response ->
                                val intent = Intent(this, JobStatus2OP::class.java)
                                val _personal = response["Personal"] as HashMap<String, Object>

                                intent.putExtra("name", _personal["Name"].toString())
                                intent.putExtra("phone", _personal["Phone"].toString())
                                intent.putExtra("email", _personal["Email"].toString())
                                intent.putExtra("title", processData.dataArray[0][position])
                                intent.putExtra("desc", processData.dataArray[4][position])
                                intent.putExtra("UID", processData.dataArray[6][position])
                                startActivity(intent)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "No se pudo cargar la información", Toast.LENGTH_SHORT).show()
                            }
                    }

                    else if(state == 2) {
                        val intent = Intent(this, JobStatus3OP::class.java)
                        intent.putExtra("title", processData.dataArray[0][position])
                        intent.putExtra("desc", processData.dataArray[4][position])
                        intent.putExtra("UID", processData.dataArray[6][position])
                        startActivity(intent)
                    }

                    else if(state == 3) {
                        val intent = Intent(this, JobStatus4OP::class.java)
                        intent.putExtra("title", processData.dataArray[0][position])
                        intent.putExtra("UID", processData.dataArray[6][position])
                        startActivity(intent)
                    }

                }
            }
            .addOnFailureListener { e ->
                Log.d("DBG", "No hay trabajos")
            }
    }

    override fun onResume() {
        super.onResume()

        reloadData()
    }

    private fun reloadData() {
        val user = auth.currentUser
        val database = AppDatabase.getDatabase(this)
        val docRef = db.collection("Users").document(user!!.uid)
        docRef.get()

            .addOnSuccessListener { document ->
                val _home = document.data?.get("Home") as HashMap<String, Object>

                val job = GlobalScope.launch(Dispatchers.Default) {
                    val tags = _home["Tags"] as ArrayList<String>
                    database.userDataHome().update(sqlContainerHome(0, _home["Name"].toString(), _home["Banner"].toString(),  _home["Score"].toString().toFloat(),  _home["JobsDone"].toString().toInt(),  _home["Credits"].toString().toInt(),  tags[0],  tags[1],  tags[2]))
                }
                runBlocking {
                    job.join()
                }
            }
            .addOnFailureListener { e ->
                Log.d("DBG", "Error fetching document ${e.localizedMessage}")
            }

        GlobalScope.launch(Dispatchers.Main) {
            loadInterface()
            loadInterfaceDynamics()
        }
    }

    private fun loadInterface() {
        val database = AppDatabase.getDatabase(this)
        var quotes = arrayOf<String>(
            "Siempre parece imposible... hasta que se hace - Nelson Mandela",
            "No cuentes los días, haz que los días cuenten - Muhammad Ali",
            "Si te caes siete veces, levántate ocho - Proverbio chino",
            "No pares cuando estés cansado. Para cuando hayas terminado -  Marilyn Monroe",
            "Solo aquellos que se arriesgan a caer pueden conseguir grandes cosas - Robert F. Kennedy",
            "No podemos ayudar a todos, pero todo el mundo puede ayudar a alguien - Ronald Reagan",
            "El secreto para salir adelante es comenzar - Mark Twain",
            "La música es libertad – Kaori Miyazono",
            "Sólo se vive una vez. Pero si lo haces bien, una vez basta - Mae West",
            "La grandeza nace de pequeños comienzos - Sir Francis Drake",
            "Si no pierdes, no puedes disfrutar de las victorias - Rafael Nadal",
            "No dejes que el miedo se interponga en tu camino - Babe Ruth",
            "El éxito es encontrar satisfacción en dar un poco más de lo que se recibe  - Christopher Reeve",
        )

        GlobalScope.launch(Dispatchers.Main) {
            val tmpPersonal = withContext(Dispatchers.IO) {database.userDataPersonal().getStatic()}
            val tmpHome = withContext(Dispatchers.IO) {database.userDataHome().getStatic()}

            try {
                val initials = tmpHome.Name.first().toString() + tmpPersonal.AP.first()
                binding.textHmInitials.text = initials
            } catch (e:Exception) {
                Toast.makeText(this@HomeView, "Vuelva a iniciar sesion", Toast.LENGTH_SHORT).show()
                logout()
            }
            
            binding.textHmName.text = tmpHome.Name
            binding.textHmScore.text = tmpHome.Score.toString()
            binding.textHmCredits.text = tmpHome.Credits.toString()

            val banner = tmpHome.Banner
            if(banner.isEmpty()) {

                binding.textHmDescription.text = quotes.random()
            }
            else {
                binding.textHmDescription.text = tmpHome.Banner
            }

            binding.textHmAb1.text = tmpHome.Tag1
            binding.textHmAb2.text = tmpHome.Tag2
            binding.textHmAb3.text = tmpHome.Tag3
        }
    }

    private fun logout() {
        val database = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {
            database.userDataPersonal().nuke()
            database.userDataHome().nuke()
        }

        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply {
            putBoolean("isLogged", false)
        }.apply()

        auth.signOut()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }


    /*
    private fun loadData() {
        val user = Firebase.auth.currentUser
        var dataSTR = ""
        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl

            // Check if user's email is verified
            val emailVerified = user.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            val uid = user.uid

            dataSTR = "Name: $name email: $email uid: $uid"
        }

        Log.d("DBG1", dataSTR)
    }

    */
}