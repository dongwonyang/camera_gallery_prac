package com.example.camera_gallery_prac

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var photoUri: Uri? = null
    private val REQ_CAMERA_PERMISSION = 1111
    private val REQ_IMAGE_CAPTURE = 1112
    private lateinit var ivSelectImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivSelectImage = findViewById(R.id.image)
        val buttonCamera = findViewById<Button>(R.id.btn_camera)
        buttonCamera.setOnClickListener {
            selectCamera()
        }
    }

//    private fun selectCamera(){
//        var permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//        if(permission == PackageManager.PERMISSION_DENIED){
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CAMERA_PERMISSION)
//        } else{
//            var state = Environment.getExternalStorageState()
//            if(TextUtils.equals(state, Environment.MEDIA_MOUNTED)){
//                var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                intent.resolveActivity(packageManager)?.let{
//                    var photoFile: File? = createImageFile()
//                    photoFile?.let{
//                        var photoUri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", it)
//                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
//                        startActivityForResult(intent, REQ_IMAGE_CAPTURE)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun createImageFile(): File {
//        val fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
//        if (!fileDir.exists()) fileDir.mkdirs()
//
//        val imageName = "fileName.jpeg"
//        val imageFile = File(fileDir, imageName)
//        imagePath = imageFile.absolutePath
//        return imageFile
//    }

    //SAF
    private fun selectCamera() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CAMERA_PERMISSION)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(packageManager) != null) {
                val photoFile: File? = createImageFile()
                photoFile?.let {
                    photoUri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", it)
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(cameraIntent, REQ_IMAGE_CAPTURE)
                }
            }
        }
    }


    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) { //registerForActivityResult 변경, 사용 방식 변경됨.
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_IMAGE_CAPTURE -> {
//                    imagePath?.apply{
//                        val bitmap = BitmapFactory.decodeFile(imagePath)
//                        ivSelectImage.setImageBitmap(bitmap)
//                    }
                    photoUri?.let { uri ->
                        ivSelectImage.setImageURI(uri)
                    }
                }
            }
        }
    }

}