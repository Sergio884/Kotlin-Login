package com.example.logint

import android.Manifest
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.android.synthetic.main.activity_record_route.*
import kotlinx.android.synthetic.main.activity_security_zone.*

class SecurityZoneActivity : AppCompatActivity(),OnMapReadyCallback {
    //lateinit var seekBar: SeekBar
    private lateinit var map: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: LatLng
    private lateinit var locationCallback: LocationCallback
    private val CODIGO_PERMISOS_UBICACION_SEGUNDO_PLANO = 2106
    private val LOG_TAG = "EnviarUbicacion"
    private var haConcedidoPermisos = false
    lateinit var circle: Circle;
    var zom_level = 17f
    var radio = GlobalClass.radio
    val hilo : Hilo = SecurityZoneActivity.Hilo(this)
    var banderaStop = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_zone)
        var buttonBool = false


        if(verificarSendLocation()){
            llevarMainPanel()
        }
        hilo.start()
        verificarPermisos()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_zone) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Thread.sleep(500)

        if(isMyServiceRunning(SecurityZone::class.java)==true){
            cambiarButton(buttonBool)
            Log.d("radioo: ",radio.toString())
            tv_titulo_zone.visibility = View.GONE
            seekBar.visibility = View.GONE
            metros_texto.visibility = View.GONE
            if(radio < 50){
                radio = 50
            }
            //metros_texto.text = radio.toString()+" metros"
            //circle.radius = radio.toDouble();


            if(radio > 200 && radio < 400 ){
                zom_level = 16f
            }
            else if(radio >= 400){
                zom_level = 15.5f
            }
            else{
                zom_level = 17f
            }

        }

        if(isMyServiceRunning(SecurityZone::class.java)==false){
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

                    radio = (p1*5)
                    GlobalClass.radio=radio
                    if(radio < 50){
                        //radio = 50
                    }
                    metros_texto.text = radio.toString()+" metros"
                    circle.radius = radio.toDouble();


                    if(radio > 200 && radio < 400 ){
                        zom_level = 16f
                    }
                    else if(radio >= 400){
                        zom_level = 15.5f
                    }
                    else{
                        zom_level = 17f
                    }


                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })
            GlobalClass.radio=50
        }




        record_Zone.setOnClickListener {

            if(isMyServiceRunning(SecurityZone::class.java)==false){
                cambiarButton(buttonBool)
                tv_titulo_zone.visibility = View.GONE
                seekBar.visibility = View.GONE
                metros_texto.visibility = View.GONE
                var intentZone = Intent(this,SecurityZone::class.java)

                intentZone.putExtra("latitud",currentLocation.latitude)
                intentZone.putExtra("longitud",currentLocation.longitude)
                intentZone.putExtra("radio",radio)
                locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                locationPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                startService(intentZone)

            }
            else{
                cambiarButton(buttonBool)
                var intentZone = Intent(this,SecurityZone::class.java)
                stopService(intentZone)
                Toast.makeText(this, "Finalizo su seguimiento", Toast.LENGTH_SHORT).show()
                var mainPanelIntent = Intent(this,MainPanel::class.java)
                startActivity(mainPanelIntent)
            }

        }

    }

    fun cambiarButton(buttonBool: Boolean){
        if(!buttonBool){
            record_Zone.setImageResource(R.drawable.ic_stop)
            record_Zone.setBackgroundResource(R.drawable.style_cancel_btn)
        }else{
            record_Zone.setImageResource(R.drawable.ic_play)
            record_Zone.setBackgroundResource(R.drawable.style_button_background)
        }
    }

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
                return
            }
            else{
                map.isMyLocationEnabled = true
                // Obtener la ultimas ubicacion conocida
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    if(it != null) {
                        with(map) {
                            currentLocation = LatLng(it.latitude, it.longitude)
                            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, CAMERA_ZOOM_LEVEL)
                            map.animateCamera(cameraUpdate)
                            circle = map.addCircle(  // Circulo
                                CircleOptions()
                                    .center(LatLng(it.latitude, it.longitude))
                                    .strokeColor(Color.argb(150,0,169,79))
                                    .fillColor(Color.argb(70,71,242,145))
                                    .radius(GlobalClass.radio.toDouble())
                            )
                        }
                    }
                }
            }

        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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
        Log.d("2", "Solicitando permisos...")
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
                fastestInterval = 1000
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
                                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, zom_level)
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

    fun verificarSendLocation():Boolean{
        if(isMyServiceRunning(SendLocation::class.java) == true){

            if(isMyServiceRunning(SecurityZone::class.java)==true){
                try{
                    val intentKill = Intent(this,SecurityZone::class.java)
                    stopService(intentKill)
                }catch (e:NullPointerException){
                    e.printStackTrace()
                }

            }


            return true
        }
        return false
    }

    fun llevarMainPanel(){
        banderaStop = 0
        val intents = Intent(this,MainPanel::class.java)
        startActivity(intents)
    }

    class Hilo(pun: SecurityZoneActivity):Thread(){
        val pun = pun
        override fun run(){
            super.run()

            while(pun.banderaStop==1){
                Thread.sleep(1000)
                if(pun.verificarSendLocation()){
                    pun.llevarMainPanel()
                }

            }

        }

    }



}