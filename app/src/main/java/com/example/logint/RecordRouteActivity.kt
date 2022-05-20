package com.example.logint

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.android.synthetic.main.activity_contact.*
import kotlinx.android.synthetic.main.activity_main_panel.*
import kotlinx.android.synthetic.main.activity_main_panel.bottomNavigationView
import kotlinx.android.synthetic.main.activity_map_distance.*
import kotlinx.android.synthetic.main.activity_record_route.*
import kotlinx.android.synthetic.main.activity_record_route.map

class RecordRouteActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_route)
        var imageBool = false

        if(isMyServiceRunning(RecordRoute::class.java)==true){
            //imageBool = recordAnimation(recordImageView,R.raw.routefinder,imageBool)
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        /*recordImageView.setOnClickListener {

            if(isMyServiceRunning(RecordRoute::class.java)==false){
                imageBool = recordAnimation(recordImageView,R.raw.routefinder,imageBool)
                et_nombreRuta.isFocusable = false
                et_nombreRuta.isEnabled = false
                et_nombreRuta.isCursorVisible = false
                et_nombreRuta.setBackgroundResource(R.drawable.ic_field_none)
                et_nombreRuta.setOnKeyListener(null)

                locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                locationPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                val intentRecord = Intent(this,RecordRoute::class.java)
                intentRecord.putExtra("nameRoute", et_nombreRuta.text.toString())
                startService(intentRecord)

            }else{
                imageBool = recordAnimation(recordImageView,R.raw.routefinder,imageBool)
                val intentRecord = Intent(this,RecordRoute::class.java)
                stopService(intentRecord)

            }


        }*/






        //****************************** Nav Bar *************************************
    }

    /*private fun recordAnimation(imageView: LottieAnimationView,animation: Int,image: Boolean):Boolean{
        if(!image){
            imageView.setAnimation(animation)
            imageView.repeatCount = 99999999
            imageView.scale = 500.0F
            imageView.playAnimation()

        }
        else{
            imageView.setImageResource(R.drawable.ic_record2)

        }
        return !image
    }*/

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted) println("acepto") //Toast.makeText(this, "acepto", Toast.LENGTH_SHORT).show()
        else println("no acepto")//Toast.makeText(this, "No acepto", Toast.LENGTH_SHORT).show()
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setMinZoomPreference(10f)
        map.setMaxZoomPreference(17f)
        // Add a marker in Sydney and move the camera

        map.uiSettings.isZoomControlsEnabled = true


        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.style));

        } catch (e: Resources.NotFoundException) {
            //Log.e(TAG, "Can't find style. Error: ", e)
        }
        if(!isLocationPermissionGranted()){
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                LOCATION_REQUEST_CODE
            )
        } else {    //Si ya teniamos acceso a la ubicacion
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            map.isMyLocationEnabled = true
            // Obtener la ultimas ubicacion conocida
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if(it != null) {
                    with(map) {
                        val latlng = LatLng(it.latitude, it.longitude)
                        currentLocation = LatLng(latlng.latitude, latlng.longitude)

                    }
                }
            }
        }
    }
}