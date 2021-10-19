package com.example.bdtkotlin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.audiofx.HapticGenerator
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.example.bdtkotlin.databinding.ActivityLoginSubirDocumentosBinding
import com.example.bdtkotlin.databinding.ActivitySettingsProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

private const val REQUEST_CODE_PIC = 100
private const val REQUEST_CODE_FILE = 1

class login_subirDocumentos : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: ActivityLoginSubirDocumentosBinding

    private var currentPic = -1
    private lateinit var pic:Bitmap

    private var flags = arrayOf(false, false, false, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginSubirDocumentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        storage = Firebase.storage


        requestPermissions()

        binding.checkIneFrontal.isVisible = false
        binding.checkIneTrasera.isVisible = false
        binding.checkComprobanteDomicilio.isVisible = false
        binding.checkCartaAntedecentes.isVisible = false

        binding.btnLgImageIneF.setOnClickListener {
            currentPic = 0
            selectPic()
        }

        binding.btnLgImageIneT.setOnClickListener {
            currentPic = 1
            selectPic()
        }

        binding.btnLgImageDom.setOnClickListener {
            currentPic = 2
            selectFile()
        }

        binding.btnLgImagePen.setOnClickListener {
            currentPic = 3
            selectFile()
        }

        binding.btnRgClose2.setOnClickListener {
            val intent = Intent()
            intent.putExtra("reply", false)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onBackPressed() {
            Toast.makeText(this, "Termine de cargar sus documentos", Toast.LENGTH_SHORT).show()
            HapticGenerator.ERROR
    }

    private fun checkDocuments() {
        Log.d("DBGR", "Documents function f1:${flags[0]}, f2:${flags[1]}, f3:${flags[2]}, f4:${flags[3]}")
        if(flags[0] && flags[1] && flags[2] && flags[3]) {
            val intent = Intent()
            intent.putExtra("reply", true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun hasReadExternalStorage() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun hasCamera() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun hasAccessMediaLocation() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED


    private fun requestPermissions() {
        var permissions = mutableListOf<String>()

        if(!hasReadExternalStorage()) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(!hasCamera()) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if(!hasAccessMediaLocation()) {
            permissions.add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }

        if(permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 0)
        }
    }

    //Picture management
    private fun selectPic() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PIC)
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, REQUEST_CODE_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Select Image
        if(requestCode == REQUEST_CODE_PIC && resultCode == RESULT_OK) {
            val imageURI = data?.data
            pic = MediaStore.Images.Media.getBitmap(this.contentResolver, imageURI)

            when (currentPic) {
                0 -> {
                    ClassesContainer().uploadImage(storage, auth.currentUser!!.uid, "INE-Frontal", pic, true, this)
                    flags[0] = true
                    binding.checkIneFrontal.isVisible = true
                    checkDocuments()
                    }
                1 -> {
                    ClassesContainer().uploadImage(storage, auth.currentUser!!.uid, "INE-Trasera", pic, true, this)
                    flags[1] = true
                    binding.checkIneTrasera.isVisible = true
                    checkDocuments()
                    }
            }
        }

        //Select File
        if(requestCode == REQUEST_CODE_FILE && resultCode == RESULT_OK) {
            //val fileUri = data?.data
            when (currentPic) {
                2 -> {
                    ClassesContainer().uploadFile(storage, auth.currentUser!!.uid, "ComprobanteDomicilio", data!!.data!!, true, this)
                    flags[2] = true
                    binding.checkComprobanteDomicilio.isVisible = true
                    checkDocuments()
                    }
                3 -> {
                    ClassesContainer().uploadFile(storage, auth.currentUser!!.uid, "AntecedentesPenales", data!!.data!!, true, this)
                    flags[3] = true
                    binding.checkCartaAntedecentes.isVisible = true
                    checkDocuments()
                    }
            }
        }


    }
}