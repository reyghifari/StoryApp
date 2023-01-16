package com.example.submisionintermediate.story

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.submisionintermediate.R
import com.example.submisionintermediate.ViewModelFactory
import com.example.submisionintermediate.databinding.ActivityAddStoryBinding
import com.example.submisionintermediate.main.MainActivity
import com.example.submisionintermediate.main.MainViewModel
import com.example.submisionintermediate.model.UserPreference
import com.example.submisionintermediate.service.ApiConfig
import com.example.submisionintermediate.service.ApiResponse
import com.example.submisionintermediate.utils.Helper
import com.google.android.gms.location.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var fusedLocation: FusedLocationProviderClient

    companion object{
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        var upload = false
    }

    private lateinit var viewModelMain: MainViewModel


    private var getFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        setupViewModel()

        binding.btnCamera.setOnClickListener { startCameraX() }
        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnUpload.setOnClickListener {
            upload = true
            uploadImage()
        }

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        binding.addLocation.setOnClickListener {
            getMyLocation()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLocation()
                }
                else -> {

                }
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ){
            fusedLocation.lastLocation.addOnSuccessListener {
                if (it != null) {
                    binding.etLatitude.setText(it.latitude.toString())
                    binding.etLongitude.setText(it.longitude.toString())
                }
                else {
                    Toast.makeText(this, resources.getText(R.string.location_failed), Toast.LENGTH_SHORT).show()


                    val mLocationRequest: LocationRequest = LocationRequest.create()
                    mLocationRequest.interval = 60000
                    mLocationRequest.fastestInterval = 5000
                    mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    val mLocationCallback: LocationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            for (location in locationResult.locations) {
                                if (location != null) {
                                    binding.etLatitude.setText(location.latitude.toString())
                                    binding.etLongitude.setText(location.longitude.toString())
                                }
                            }
                        }
                    }
                    LocationServices.getFusedLocationProviderClient(this)
                        .requestLocationUpdates(mLocationRequest, mLocationCallback, mainLooper)
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    private fun setupViewModel() {
        viewModelMain = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore),this)
        )[MainViewModel::class.java]
    }

    private fun uploadImage() {

        if (getFile != null) {
            val file = Helper.reduceFileImage(getFile as File)
            val description = binding.etDescription.text.toString().toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )
            val lat = binding.etLatitude.text.toString()
            val lng = binding.etLongitude.text.toString()


            if(lat == "" || lng == ""){
                viewModelMain.getUser().observe(this){ user ->
                    val service = ApiConfig().getApiService().uploadImage(imageMultipart, description, "Bearer ${user.token}")
                    service.enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(
                            call: Call<ApiResponse>,
                            response: Response<ApiResponse>
                        ){
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                if (responseBody != null && !responseBody.error) {
                                    Toast.makeText(this@AddStoryActivity, "SUKSES", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            } else {
                                Toast.makeText(this@AddStoryActivity, response.message(), Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Toast.makeText(this@AddStoryActivity, "failed", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }else{
                viewModelMain.getUser().observe(this){ user ->
                    val service = ApiConfig().getApiService().addStory("Bearer ${user.token}",imageMultipart, description, lat.toDouble(), lng.toDouble())
                    service.enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(
                            call: Call<ApiResponse>,
                            response: Response<ApiResponse>
                        ){
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                if (responseBody != null && !responseBody.error) {
                                    Toast.makeText(this@AddStoryActivity, "SUKSES", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            } else {
                                Toast.makeText(this@AddStoryActivity, response.message(), Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Toast.makeText(this@AddStoryActivity, "failed", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

        } else {
            Helper.showToast(this@AddStoryActivity, "Silahkan masukan gambar terlebih dahulu")
        }


    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }


    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {

            val myFile = it.data?.getSerializableExtra("picture") as File
            getFile = myFile
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            val result = Helper.rotateBitmap(
                BitmapFactory.decodeFile(myFile.path),
                isBackCamera
            )

            binding.ivAddStoryImg.setImageBitmap(result)
        }
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
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = Helper.uriToFile(selectedImg, this@AddStoryActivity)
            getFile = myFile
            binding.ivAddStoryImg.setImageURI(selectedImg)
        }
    }


}