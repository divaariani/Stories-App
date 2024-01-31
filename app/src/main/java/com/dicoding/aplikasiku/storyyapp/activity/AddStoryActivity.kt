package com.dicoding.aplikasiku.storyyapp.activity

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dicoding.aplikasiku.storyyapp.R
import com.dicoding.aplikasiku.storyyapp.api.ApiConfig
import com.dicoding.aplikasiku.storyyapp.databinding.ActivityAddStoryBinding
import com.dicoding.aplikasiku.storyyapp.model.AddStoryResponse
import com.dicoding.aplikasiku.storyyapp.utils.SessionManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class AddStoryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AddStoryActivity"

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val REQUEST_CODE = 200
        private const val IMAGE_CHOOSE = 100

        private const val FILE_NAME = "dd-MMM-yyyy"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Do not have permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private var filePhoto: File? = null
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var sharedPref: SessionManager
    private var token: String? = null
    private var timeStamp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SessionManager(this)
        token = sharedPref.getToken

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        timeStamp = SimpleDateFormat(
            FILE_NAME,
            Locale.US
        ).format(System.currentTimeMillis())

        supportActionBar?.title = getString(R.string.add_story)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.buttonCamera.setOnClickListener { openCamera() }
        binding.buttonGallery.setOnClickListener { openGallery() }
        binding.buttonAdd.setOnClickListener { uploadToSever() }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        getPhotoFile(application).also {
            filePhoto = it.absoluteFile
        }

        val providerFile = FileProvider.getUriForFile(
            this,
            "com.dicoding.aplikasiku.androidcamera.fileprovider",
            filePhoto!!
        )
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (cameraIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(cameraIntent, REQUEST_CODE)
        } else {
            Toast.makeText(this, "Camera can not open, check permission!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, REQUEST_CODE_PERMISSIONS)
            } else {
                chooseImageGallery()
            }
        } else {
            chooseImageGallery()
        }
    }

    private fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_CHOOSE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val resultCamera = BitmapFactory.decodeFile(filePhoto?.absolutePath)
            binding.ivPhoto.setImageBitmap(resultCamera)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

        if (requestCode == IMAGE_CHOOSE && resultCode == Activity.RESULT_OK) {
            val selectedImg: Uri = data?.data as Uri
            filePhoto = uriToFile(selectedImg, this@AddStoryActivity)
            binding.ivPhoto.setImageURI(selectedImg)
        }
    }

    private fun uploadToSever() {
        val desc = binding.edAddDescription.text.toString()

        if (filePhoto == null) {
            Toast.makeText(this@AddStoryActivity, "Photo can not be empty", Toast.LENGTH_SHORT)
                .show()
        } else if (desc.isEmpty()) {
            Toast.makeText(
                this@AddStoryActivity,
                "Description can not be empty",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val description = desc.toRequestBody("text/plain".toMediaType())
            val file = reduceFileImage(filePhoto as File)
            val imageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val bodyMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                imageFile
            )

            showLoading(true)
            val apiService =
                ApiConfig.getApiService().addNewStory("Bearer $token", bodyMultipart, description)
            apiService.enqueue(object : Callback<AddStoryResponse> {
                override fun onResponse(
                    call: Call<AddStoryResponse>,
                    response: Response<AddStoryResponse>
                ) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && !responseBody.error) {
                            Toast.makeText(
                                this@AddStoryActivity,
                                response.message(),
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@AddStoryActivity,
                                responseBody!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                    showLoading(false)
                    Log.e(TAG, "onFailure: " + t.message)
                }
            })
        }
    }

    private fun getPhotoFile(context: Context): File {
        val directoryStorage = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp!!, ".jpg", directoryStorage)
    }

    private fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)

        var compressQuality = 100
        var streamLength: Int

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > 1000000)

        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))

        return file
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = getPhotoFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}