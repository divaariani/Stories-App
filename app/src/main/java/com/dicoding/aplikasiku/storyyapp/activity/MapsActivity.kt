package com.dicoding.aplikasiku.storyyapp.activity

import android.Manifest
import androidx.activity.viewModels
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.dicoding.aplikasiku.storyyapp.R
import com.dicoding.aplikasiku.storyyapp.activity.vm.MapsViewModel
import com.dicoding.aplikasiku.storyyapp.databinding.ActivityMapsBinding
import com.dicoding.aplikasiku.storyyapp.model.ListStories
import com.dicoding.aplikasiku.storyyapp.utils.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var sharedPref: SessionManager
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SessionManager(this)

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

        val myHome = LatLng(-6.512260, 106.829440)
        mMap.addMarker(
            MarkerOptions()
                .position(myHome)
                .title("My Home")
                .snippet("Griya Cibinong Indah")
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myHome, 15f))

        getMyLocation()
        getUsersLocation(googleMap)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){isGranted: Boolean->
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

    private fun addManyMarker( googleMap: GoogleMap, listStories: List<ListStories>){
        listStories.forEach{
                listStoryMap ->
            val markerMap = MarkerOptions()
            markerMap.position(LatLng(listStoryMap.lat, listStoryMap.lon))
                .title(listStoryMap.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))

            val marker = googleMap.addMarker(markerMap)
            marker?.tag = listStoryMap
        }
    }


    private fun getUsersLocation(googleMap: GoogleMap) {
        val  mapsViewModel: MapsViewModel by viewModels {
            MapsViewModel.ViewModelFactory(this)
        }

        mapsViewModel.getUsersLocation().observe(this){
            when(it){
                is com.dicoding.aplikasiku.storyyapp.data.Result.Success -> {
                    addManyMarker(googleMap, it.data)
                }

                is com.dicoding.aplikasiku.storyyapp.data.Result.Loading -> {}

                is com.dicoding.aplikasiku.storyyapp.data.Result.Error ->{}
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}