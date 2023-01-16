package com.example.submisionintermediate.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.submisionintermediate.R
import com.example.submisionintermediate.ViewModelFactory
import com.example.submisionintermediate.databinding.ActivityMapsBinding
import com.example.submisionintermediate.login.LoginActivity
import com.example.submisionintermediate.model.StoryModel
import com.example.submisionintermediate.model.UserPreference
import com.example.submisionintermediate.service.ListStoryItem

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapsViewModel: MapStoryViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var ListStories = ArrayList<StoryModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        setupViewModel()

        mapsViewModel.listStories.observe(this) { user ->
            setStoriesData(user)
        }

        getMyLocation()
    }

    private fun setStoriesData(stories: List<ListStoryItem>) {

        for (story in stories.reversed()){
            val newStory = StoryModel(
                name = story.name,
                photoUrl = story.photoUrl,
                description = story.description,
                lat = story.lat,
                lng = story.lon
            )
            this.ListStories.add(newStory)
            val latLng = LatLng(story.lat!!, story.lon!!)

            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(story.name)
                    .snippet(story.description)
            )
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(story.lat, story.lon), 5f))
        }

    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setupViewModel() {
        mapsViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), this)
        )[MapStoryViewModel::class.java]

        mapsViewModel.getUser().observe(this) { user ->
            if (user.token?.isEmpty() == true) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }else{
                val tokenAuth = user.token
                ListStories.clear()

                mapsViewModel.getAllStoryMap("Bearer $tokenAuth")
            }
        }
    }


}