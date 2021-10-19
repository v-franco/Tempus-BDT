package com.example.bdtkotlin

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.Observer
import com.example.bdtkotlin.databinding.ActivityCreateJobBinding
import com.example.bdtkotlin.databinding.ActivityRegisterViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val REQUEST_CODE = 100

class CreateJob : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: ActivityCreateJobBinding

    private var database = AppDatabase.getDatabase(this)

    //Spinners
    private var spinner1 = ""
    private var spinner2 = ""
    private var spinner3 = ""

    //Select pic
    private var picFlag:Boolean = false
    private lateinit var pic: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage
        database = AppDatabase.getDatabase(this)

        val list : MutableList<String> = ArrayList()
        list.add("Pintura")
        list.add("Hogar")
        list.add("Mantenimiento")
        list.add("Mascotas")
        list.add("Clases")
        list.add("Idiomas")
        list.add("Habilidades")
        list.add("Virtual")
        list.add("Jardinería")
        list.add("Cuidado")
        list.add("Musica")
        list.add("Reparación")
        list.add("Carpintería")
        list.add("Entretenimiento")
        list.add("Actividades")


        val spnTags = findViewById<Spinner>(R.id.tags_spinner_crj)
        val spnTags2 = findViewById<Spinner>(R.id.tags_spinner2_crj)
        val spnTags3 = findViewById<Spinner>(R.id.tags_spinner3_crj)
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list)

        spnTags.adapter = adapter
        spnTags2.adapter = adapter
        spnTags3.adapter = adapter

        spnTags.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinner1 = list[position]
                //Toast.makeText(this@CreateJob, "$position Selected", Toast.LENGTH_SHORT).show()
            }
        }

        spnTags2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinner2 = list[position]
                //Toast.makeText(this@CreateJob, "$position Selected", Toast.LENGTH_SHORT).show()
            }
        }

        spnTags3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinner3 = list[position]
                //Toast.makeText(this@CreateJob, "$position Selected", Toast.LENGTH_SHORT).show()
            }
        }

        limitDropDownHeight(spnTags)
        limitDropDownHeight2(spnTags2)
        limitDropDownHeight3(spnTags3)

        binding.btnCrjJobImage.setOnClickListener {
            selectPic()
        }

        binding.btnCrjPublish.setOnClickListener {
            createJob()
        }

        binding.btnCrjClose.setOnClickListener {
            val intent = Intent(this, JobsMain::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun createJob() {
        val user = auth.currentUser

        GlobalScope.launch(Dispatchers.Main) {
            val personalData = withContext(Dispatchers.IO) {database.userDataPersonal().getStatic()}
            val homeData = withContext(Dispatchers.IO) {database.userDataHome().getStatic()}
            if(homeData.Credits != -1) {
                if(picFlag) {
                    if(user != null) {
                        val jobName = binding.txtCrjJobName.text.toString().trim()
                        val jobDesc = binding.txtCrjJobDesc.text.toString().trim()
                        val jobLoca = binding.txtCrjJobLocation.text.toString().trim()

                        if(jobName.isEmpty() || jobDesc.isEmpty() || jobLoca.isEmpty()){
                            Toast.makeText(this@CreateJob, "Llene todos los datos", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            val containerFireStore = hashMapOf(
                                "title" to jobName,
                                "description" to jobDesc,
                                "location" to jobLoca,
                                "name" to homeData.Name,
                                "email" to personalData.Email,
                                "phone" to personalData.Phone,
                                "tag1" to spinner1,
                                "tag2" to spinner2,
                                "tag3" to spinner3,
                                "idCreator" to user.uid,
                                "idTaker" to "",
                                "isLocked" to false,
                                "state" to 0
                            )

                            db.collection("Jobs").document(user.uid)
                                .set(containerFireStore)
                                .addOnSuccessListener {
                                    Log.d("DBGR", "Successfully written!")
                                }
                                .addOnFailureListener { e ->
                                    Log.d("DBGR", "Error writing document", e)
                                    Toast.makeText(this@CreateJob, "No se pudo crear", Toast.LENGTH_SHORT).show()
                                }

                            updateCredits()

                            //Picture upload
                            ClassesContainer().uploadImage(storage, user.uid, "Job", pic, false, this@CreateJob)

                            binding.btnCrjPublish.visibility = View.INVISIBLE
                            Toast.makeText(this@CreateJob, "Trabajo creado", Toast.LENGTH_SHORT).show()

                            finish()
                        }

                    }
                }
                else {
                    Toast.makeText(this@CreateJob, "Seleccione una imagen", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(this@CreateJob, "Le faltan creditos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateCredits() {
        val user = auth.currentUser

        GlobalScope.launch(Dispatchers.Main) {
            val homeData = withContext(Dispatchers.IO) {database.userDataHome().getStatic()}

            val containerFireStore = mapOf(
                "Banner" to homeData.Banner,
                "Credits" to -1,
                "Name" to homeData.Name,
                "Score" to homeData.Score,
                "JobsDone" to homeData.JobsDone,
                "Tags" to arrayListOf(homeData.Tag1, homeData.Tag2, homeData.Tag3)
            )

            val dataSTC = mapOf(
                "Home" to containerFireStore
            )

            db.collection("Users").document(user!!.uid)
                .update(dataSTC)
                .addOnSuccessListener {
                    Log.d("DBG", "Data updated")
                }
                .addOnFailureListener { e ->
                    Log.d("DBG", "Error fetching document ${e.localizedMessage}")
                }
        }
    }


    fun limitDropDownHeight(spnTags: Spinner){
        val popup = Spinner::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val popupWindow = popup.get(spnTags) as ListPopupWindow
        popupWindow.height = (200 * resources.displayMetrics.density).toInt()
    }

    fun limitDropDownHeight2(spnTags2: Spinner){
        val popup = Spinner::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val popupWindow = popup.get(spnTags2) as ListPopupWindow
        popupWindow.height = (200 * resources.displayMetrics.density).toInt()
    }

    fun limitDropDownHeight3(spnTags3: Spinner){
        val popup = Spinner::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val popupWindow = popup.get(spnTags3) as ListPopupWindow
        popupWindow.height = (200 * resources.displayMetrics.density).toInt()
    }


    //Picture management
    private fun selectPic() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val imageURI = data?.data
            pic = MediaStore.Images.Media.getBitmap(this.contentResolver, imageURI)
            picFlag = true

            binding.imageCrjCheck.visibility = View.VISIBLE
        }
    }

}