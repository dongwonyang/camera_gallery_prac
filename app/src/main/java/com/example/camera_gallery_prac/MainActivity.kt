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
import android.content.ContentValues
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var photoUri: Uri? = null
    private val REQ_CAMERA_PERMISSION = 1111
    private val REQ_IMAGE_CAPTURE = 1112
    private val REQ_GALLERY = 1113
    private lateinit var ivSelectImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivSelectImage = findViewById(R.id.image)
        val buttonCamera = findViewById<Button>(R.id.btn_camera)
        buttonCamera.setOnClickListener {
            selectCamera()
        }

        val buttonGallery = findViewById<Button>(R.id.btn_gallery)
        buttonGallery.setOnClickListener {
            selectGallery()
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

    private fun selectGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(intent, REQ_GALLERY)
        galleryLauncher.launch(intent)
    }


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

    private fun deleteFilesFromDirectory(directory: File) { //val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) 위치 파일 제거
        if (directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    // 파일을 삭제하거나 필요한 정리 작업 수행
                    file.delete()
                }
            }
        }
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
                        saveImageToGallery(uri)
                    }
                }

                REQ_GALLERY ->{
                    val uri: Uri? = data?.data
                    uri?.let { uri->
                        ivSelectImage.setImageURI(uri)
                    }
                }
            }
        }
    }

    val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri: Uri? = data?.data
            uri?.let { uri ->
                ivSelectImage.setImageURI(uri)
            }
        }
    }


    val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                ivSelectImage.setImageURI(uri)
//                saveImageToGallery(uri)
            }
        }
    }
    private fun saveImageToGallery(imageUri: Uri) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "fileName.jpg") // 파일 이름
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // MIME 타입
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000) // 추가된 날짜 및 시간
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis()) // 촬영된 날짜 및 시간
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES) // 저장할 디렉터리
        }

        val contentResolver = applicationContext.contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { outputStreamUri ->
            contentResolver.openOutputStream(outputStreamUri)?.use { outputStream ->
                contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    inputStream.copyTo(outputStream) // 이미지 복사
                }
            }
            Toast.makeText(this, "이미지가 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(this, "이미지를 갤러리에 저장하는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
    }

}