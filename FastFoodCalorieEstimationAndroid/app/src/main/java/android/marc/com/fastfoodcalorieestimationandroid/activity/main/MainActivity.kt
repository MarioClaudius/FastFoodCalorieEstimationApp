package android.marc.com.fastfoodcalorieestimationandroid.activity.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.marc.com.fastfoodcalorieestimationandroid.activity.ViewModelFactory
import android.marc.com.fastfoodcalorieestimationandroid.databinding.ActivityMainBinding
import android.marc.com.fastfoodcalorieestimationandroid.helper.createCustomTempFile
import android.marc.com.fastfoodcalorieestimationandroid.helper.reduceFileImage
import android.marc.com.fastfoodcalorieestimationandroid.helper.rotateBitmap
import android.marc.com.fastfoodcalorieestimationandroid.helper.uriToFile
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var currentPhotoPath: String
    private var uploadFile: File? = null

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            uploadFile = myFile
            val result = rotateBitmap(BitmapFactory.decodeFile(myFile.path), true)
            binding.previewImageInput.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@MainActivity)
            uploadFile = myFile
            binding.previewImageInput.setImageURI(selectedImg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        hideActionBar()
        setupViewModel()
        setupClickListener()
    }

    private fun setupClickListener() {
        binding.btnCamera.setOnClickListener {
            takePhoto()
        }
        binding.btnGallery.setOnClickListener {
            getPhotoFromGallery()
        }
        binding.btnCalculate.setOnClickListener {
            if (uploadFile != null) {
                mainViewModel.startLoading()
                val file = reduceFileImage(uploadFile as File)
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )
                mainViewModel.testApi()
            } else {
                // dialog error
            }
        }
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@MainActivity,
                "android.marc.com.fastfoodcalorieestimationandroid",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun getPhotoFromGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun setupViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory()
        )[MainViewModel::class.java]

        mainViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        mainViewModel.testString.observe(this) { str ->
            binding.previewImageOutput.visibility = View.VISIBLE
            binding.tvResultTitle.visibility = View.VISIBLE
            binding.tvResultContentType.visibility = View.VISIBLE
            binding.tvResultContentTypePrediction.text = str
            binding.tvResultContentTypePrediction.visibility = View.VISIBLE
            binding.tvResultContentTotalCalorie.visibility = View.VISIBLE
            binding.tvResultContentTotalCaloriePrediction.text = "0 cal"
            binding.tvResultContentTotalCaloriePrediction.visibility = View.VISIBLE
        }

        mainViewModel.isError.observe(this) { isError ->
            if (isError) {
                Log.d("ERROR BACKEND", "ERROR")
            }
        }
    }

    private fun hideActionBar() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionGranted()) {
                Toast.makeText(
                    this,
                    "Permission is not granted",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}