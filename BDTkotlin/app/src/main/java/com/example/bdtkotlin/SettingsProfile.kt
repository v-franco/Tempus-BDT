package com.example.bdtkotlin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.audiofx.HapticGenerator
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log

import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.example.bdtkotlin.databinding.ActivitySettingsProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*


private const val REQUEST_CODE = 100

class SettingsProfile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: ActivitySettingsProfileBinding
    private lateinit var activityForResultLauncher: ActivityResultLauncher<Intent>

    private var database = AppDatabase.getDatabase(this)
    private var personalData: sqlContainerPersonal = sqlContainerPersonal(0,"","","","","","",0)
    private lateinit var homeData: sqlContainerHome
    private var newUser = false

    private var picFlag:Boolean = false
    private lateinit var pic:Bitmap

    private var documentFlag:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage


        newUser = intent.getBooleanExtra("newUser", false)
        loadInterface()

        binding.btnRgClose13.setOnClickListener {
            if(newUser) {
                Toast.makeText(this, "Aun no completa sus datos", Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(this, SettingsMain::class.java)
                startActivity(intent)
            }

        }

        binding.btnStPrEmail.setOnClickListener {
            binding.txtStPrEmail.isEnabled = true
            binding.txtStPrReemail.isEnabled = true
            Toast.makeText(this, "Edicion de correo activada", Toast.LENGTH_SHORT).show()
        }

        binding.btnStPrPass.setOnClickListener {
            binding.txtStPrPass.isEnabled = true
            binding.txtStPrRepass.isEnabled = true
            Toast.makeText(this, "Edicion de contraseña activada", Toast.LENGTH_SHORT).show()
        }

        binding.imageStrPrAvatar.setOnClickListener {
            selectPic()
        }

        binding.btnStPrSave.setOnClickListener {
            saveData()
        }

        binding.btnUpdateDocumentos2.setOnClickListener {
            val intent = Intent(this, login_subirDocumentos::class.java)
            activityForResultLauncher.launch(intent)
        }

        activityForResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result!!.resultCode == Activity.RESULT_OK) {
                documentFlag = result.data!!.extras!!.getBoolean("reply")

                Log.d("DBGR", "reply | $documentFlag")

                if(documentFlag) {
                    binding.btnUpdateDocumentos2.isEnabled = false
                }
            }
        }

    }

    override fun onBackPressed() {
        if(newUser) {
            Toast.makeText(this, "Termine de llenar su informacion", Toast.LENGTH_SHORT).show()
            HapticGenerator.ERROR
        }
        else {
            super.onBackPressed()
        }
    }

    private fun loadInterface() {
        GlobalScope.launch(Dispatchers.Main) {
            personalData = withContext(Dispatchers.IO) {database.userDataPersonal().getStatic()}
            homeData = withContext(Dispatchers.IO) {database.userDataHome().getStatic()}

            if(newUser) {
                binding.txtStPrAddress.isEnabled = true
            }
            else {
                binding.radioGroupStPr.visibility = View.GONE
                binding.btnUpdateDocumentos2.visibility = View.GONE
            }

            binding.txtStPrNames.setText(personalData.Name)
            binding.txtStPrAppa.setText(personalData.AP)
            binding.txtStPrApma.setText(personalData.AM)
            binding.txtStPrAddress.setText(personalData.Address)
            binding.txtStPrPhone.setText(personalData.Phone)

            val storageRef = storage.reference
            var pathRef = storageRef.child("${auth.currentUser!!.uid}/profilePic.jpg")

            val ONE_MEGABYTE: Int = 1024*1024
            pathRef.getBytes(ONE_MEGABYTE.toLong())
                .addOnSuccessListener { imgBytes ->
                    Log.d("DBG", "Image loaded")
                    picFlag = true
                    pic = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size)
                    binding.imageStrPrAvatar.setImageBitmap(pic)
                    binding.btnStPrSave.isEnabled = true
                }
                .addOnFailureListener { _ ->
                    binding.imageStrPrAvatar.setBackgroundResource(R.color.teal_700)
                    if(newUser) {
                        binding.btnStPrSave.isEnabled = true
                    }
                    else {
                        Toast.makeText(this@SettingsProfile, "Error al cargar la foto", Toast.LENGTH_SHORT).show()
                    }
                }

            binding.txtStPrEmail.setText(personalData.Email)
        }
    }

    private fun saveData() {
        val user = auth.currentUser
        var canBeSaved = true
        val gender:Int


        if(newUser) {
            if(binding.radbtnHombre.isChecked) {
                gender = 1
            }
            else if (binding.radbtnMujer.isChecked) {
                gender = 2
            }
            else {
                gender = 0
                canBeSaved = false
            }
        }
        else {
            gender = personalData.Gender
        }


        if(canBeSaved) {
            val email = binding.txtStPrEmail.text.toString().trim()
            val name = binding.txtStPrNames.text.toString().trim()
            val ap = binding.txtStPrAppa.text.toString().trim()
            val am = binding.txtStPrApma.text.toString().trim()
            val phone = binding.txtStPrPhone.text.toString().trim()
            val address = binding.txtStPrAddress.text.toString().trim()


            if(name.isNotEmpty()) {
                if(ap.isNotEmpty()) {
                    if(am.isNotEmpty()) {
                        if(address.isNotEmpty()) {
                            if(phone.isNotEmpty() && phone.length == 10) {
                                if(picFlag) {
                                    if(newUser) {
                                        if(documentFlag) {
                                            if(user != null) {
                                                val userRef = db.collection("Users").document(user.uid)

                                                val personal = mapOf(
                                                    "Email" to email,
                                                    "Name" to name,
                                                    "Gender" to gender,
                                                    "AP" to ap,
                                                    "AM" to am,
                                                    "Phone" to phone,
                                                    "Address" to address
                                                )

                                                val home = mapOf(
                                                    "Name" to name,
                                                    "Credits" to homeData.Credits,
                                                    "Score" to homeData.Score,
                                                    "JobsDone" to homeData.JobsDone,
                                                    "Banner" to homeData.Banner,
                                                    "Tags" to arrayListOf(homeData.Tag1, homeData.Tag2 ,homeData.Tag3)
                                                )

                                                val dataSTC = mapOf(
                                                    "newUser" to false,
                                                    "Home" to home,
                                                    "Personal" to personal
                                                )

                                                userRef
                                                    .update(dataSTC)
                                                    .addOnSuccessListener {
                                                        Log.d("DBGS", "Info Updated")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.d("DBGS", "Error Updating", e)
                                                    }

                                                if(picFlag) {
                                                    ClassesContainer().uploadImage(storage, user.uid, "profilePic", pic, false, this)
                                                }

                                                val job = GlobalScope.launch(Dispatchers.IO) {
                                                    database.userDataPersonal().update(sqlContainerPersonal(0, name, email, ap, am, phone, address, gender))
                                                    database.userDataHome().update(sqlContainerHome(0, homeData.Name, homeData.Banner, homeData.Score, homeData.JobsDone, homeData.Credits, homeData.Tag1, homeData.Tag2, homeData.Tag3))
                                                }
                                                runBlocking {
                                                    job.join()
                                                }

                                                binding.btnStPrSave.isEnabled = false

                                                Toast.makeText(this, "Datos guardados", Toast.LENGTH_SHORT).show()

                                                if(newUser) {
                                                    val intent = Intent(this,  HomeView::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                                else {
                                                    this.finish()
                                                }
                                            }
                                        }
                                        else {
                                            Toast.makeText(this, "Faltan sus documentos", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else {
                                        if(user != null) {
                                            val userRef = db.collection("Users").document(user.uid)

                                            val personal = mapOf(
                                                "Email" to email,
                                                "Name" to name,
                                                "Gender" to gender,
                                                "AP" to ap,
                                                "AM" to am,
                                                "Phone" to phone,
                                                "Address" to address
                                            )

                                            val home = mapOf(
                                                "Name" to name,
                                                "Credits" to homeData.Credits,
                                                "Score" to homeData.Score,
                                                "JobsDone" to homeData.JobsDone,
                                                "Banner" to homeData.Banner,
                                                "Tags" to arrayListOf(homeData.Tag1, homeData.Tag2 ,homeData.Tag3)
                                            )

                                            val dataSTC = mapOf(
                                                "newUser" to false,
                                                "Home" to home,
                                                "Personal" to personal
                                            )

                                            userRef
                                                .update(dataSTC)
                                                .addOnSuccessListener {
                                                    Log.d("DBGS", "Info Updated")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.d("DBGS", "Error Updating", e)
                                                }

                                            if(picFlag) {
                                                ClassesContainer().uploadImage(storage, user.uid, "profilePic", pic, false, this)
                                            }

                                            val job = GlobalScope.launch(Dispatchers.IO) {
                                                database.userDataPersonal().update(sqlContainerPersonal(0, name, email, ap, am, phone, address, gender))
                                                database.userDataHome().update(sqlContainerHome(0, homeData.Name, homeData.Banner, homeData.Score, homeData.JobsDone, homeData.Credits, homeData.Tag1, homeData.Tag2, homeData.Tag3))
                                            }
                                            runBlocking {
                                                job.join()
                                            }

                                            binding.btnStPrSave.isEnabled = false

                                            Toast.makeText(this, "Datos guardados", Toast.LENGTH_SHORT).show()

                                            if(newUser) {
                                                val intent = Intent(this,  HomeView::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                            else {
                                                this.finish()
                                            }
                                        }
                                    }
                                }
                                else {
                                    Toast.makeText(this, "Debe de poner su foto", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else {
                                Toast.makeText(this, "Debe de poner numero de telefono de 10 digitos", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else {
                            Toast.makeText(this, "Debe de poner su dirección", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        Toast.makeText(this, "El Apellido Materno esta vacio", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Toast.makeText(this, "El Apellido Paterno esta vacio", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(this, "El Nombre esta vacio", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            Toast.makeText(this, "Necesita asignar un genero", Toast.LENGTH_SHORT).show()
        }

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
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageURI)
            pic = bitmap
            picFlag = true
            binding.imageStrPrAvatar.setBackgroundResource(0)
            binding.imageStrPrAvatar.setImageBitmap(bitmap)
        }
    }

}