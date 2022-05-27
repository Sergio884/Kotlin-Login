package com.example.logint

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.android.synthetic.main.activity_on_route.*

class OnRouteActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: LatLng
    private val LOG_TAG = "EnviarUbicacion"
    private var haConcedidoPermisos = false
    private lateinit var locationCallback: LocationCallback
    private val CODIGO_PERMISOS_UBICACION_SEGUNDO_PLANO = 2106
    private lateinit var pathPolyLine: ArrayList<PathLocation>
    private var  radioTolerancia = 0
    var tiempoTolerancia =0
    var latitud = 19.47991613867424
    var longitud =-99.1377547739467

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_route)


        if (intent != null){
            try{
                pathPolyLine = intent.getParcelableArrayListExtra("path")!!
                radioTolerancia  = intent.getIntExtra("radioTolerancia",50)
                tiempoTolerancia  = intent.getIntExtra("tiempoTolerancia",5)
                latitud = intent.getDoubleExtra("latitud",19.47991613867424)
                longitud = intent.getDoubleExtra("longitud",-99.1377547739467)
                if(pathPolyLine.isNotEmpty())
                    pathPolyLine.forEach {
                        //Log.d("Coordinates","lat:"+it.lat+" long:"+it.long)

                        GlobalClass.polyLine.add(it.position)
                    }
            }catch (e: java.lang.NullPointerException){
                e.printStackTrace()
            }

        }





        if(isMyServiceRunning(OnRoute::class.java) == false){
            val intentOnRoute = Intent(this,OnRoute::class.java)
            intentOnRoute.putExtra("radioTolerancia",radioTolerancia)
            intentOnRoute.putExtra("tiempoTolerancia",tiempoTolerancia)
            intentOnRoute.putExtra("latitud", latitud)
            intentOnRoute.putExtra("longitud",longitud)
            startService(intentOnRoute)
        }

        Log.d("radioTolerancia: ",""+radioTolerancia.toString())
        Log.d("tiempoTolerancia: ",tiempoTolerancia.toString())
        verificarPermisos()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        textView2.setOnClickListener {
            val intentOnRoute = Intent(this,OnRoute::class.java)
            stopService(intentOnRoute)
            val intentRedirection = Intent(this,MainPanel::class.java)
            intentRedirection.putExtra("ruteGooal",true)
            startActivity(intentRedirection)
        }


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
        map.clear()
        map.setMinZoomPreference(10f)
        map.setMaxZoomPreference(17f)
        // Add a marker in Sydney and move the camera
        val optionsPolyLine = PolylineOptions()

        GlobalClass.polyLine.forEach{
            optionsPolyLine.add(it)
            optionsPolyLine.width(6f)
            optionsPolyLine.color(Color.rgb(0, 255, 185 ))
        }

        map.addPolyline(optionsPolyLine)

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

                return
            }
            else{
                map.isMyLocationEnabled = true
                // Obtener la ultimas ubicacion conocida
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    if(it != null) {
                        with(map) {
                            currentLocation = LatLng(it.latitude, it.longitude)
                            val cameraUpdate = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(currentLocation,
                                com.example.logint.CAMERA_ZOOM_LEVEL
                            )
                            map.animateCamera(cameraUpdate)
                        }
                    }
                }
            }

        }
    }

    private fun verificarPermisos() {
        val permisos = arrayListOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        // Segundo plano para Android Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        val permisosComoArray = permisos.toTypedArray()
        if (tienePermisos(permisosComoArray)) {
            haConcedidoPermisos = true
            onPermisosConcedidos()
            Log.d(LOG_TAG, "Los permisos ya fueron concedidos")
        } else {
            solicitarPermisos(permisosComoArray)
        }
    }


    private fun solicitarPermisos(permisos: Array<String>) {
        Log.d(LOG_TAG, "Solicitando permisos...")
        requestPermissions(
            permisos,
            2106
        )
    }

    private fun tienePermisos(permisos: Array<String>): Boolean {
        return permisos.all {
            return ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun onPermisosConcedidos() {
        // Hasta aquí sabemos que los permisos ya están concedidos
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    print("Ubicación ${it}")
                } else {
                    Log.d(LOG_TAG, "No se pudo obtener la ubicación")
                }
            }
            //////
            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 20000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    Log.d(LOG_TAG, "Se recibió una actualización")
                    for (location in locationResult.locations) {
                        if(location != null) {
                            with(map) {
                                currentLocation = LatLng(location.latitude, location.longitude)
                                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, CAMERA_ZOOM_LEVEL)
                                map.animateCamera(cameraUpdate)
                            }
                        }
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.d(LOG_TAG, "Tal vez no solicitaste permiso antes")
        }

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
}