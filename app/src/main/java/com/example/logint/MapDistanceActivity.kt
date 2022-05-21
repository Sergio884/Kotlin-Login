package com.example.logint

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.res.Resources
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.logint.io.response.DistanceResponse
import com.example.logint.model.Step
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.maps.android.PolyUtil.isLocationOnPath
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.android.synthetic.main.activity_map_distance.*

class MapDistanceActivity : AppCompatActivity(), OnMapReadyCallback {
    private val FROM_REQUEST_CODE = 1
    private val TO_REQUEST_CODE = 2
    private val TAG = "MapsDistance"
    private var travelMode = "driving"
    private lateinit var mMap: GoogleMap
    private var markerFrom : Marker? = null
    private var markerTo : Marker? = null
    private var fromLatLng: LatLng? = null
    private var toLatLng: LatLng? = null
    private var destinyLocation: LatLng? = null
    private lateinit var currentLocation: LatLng
    lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_distance)
        val btnDestiny = findViewById<Button>(R.id.btnDestiny)

        val driving = findViewById<ImageView>(R.id.driving)
        val transit = findViewById<ImageView>(R.id.transit)
        val walking = findViewById<ImageView>(R.id.walking)
        val bicycling = findViewById<ImageView>(R.id.bicycling)

        driving.isSelected = true

        driving.setOnClickListener {
            travelMode = "driving"
            driving.isSelected = !driving.isSelected
            //driving.setImageResource(R.drawable.bg_border_active)
            transit.isSelected = false
            walking.isSelected = false
            bicycling.isSelected = false
            changeTravelMode()
        }


        transit.setOnClickListener {
            travelMode = "transit"
            transit.isSelected = !transit.isSelected
            driving.isSelected = false
            walking.isSelected = false
            bicycling.isSelected = false
            changeTravelMode()
        }

        walking.setOnClickListener {
            travelMode = "walking"
            walking.isSelected = !walking.isSelected
            driving.isSelected = false
            transit.isSelected = false
            bicycling.isSelected = false
            changeTravelMode()
        }

        bicycling.setOnClickListener {
            travelMode = "bicycling"
            bicycling.isSelected = !bicycling.isSelected
            driving.isSelected = false
            transit.isSelected = false
            walking.isSelected = false
            changeTravelMode()
        }

        btn_start_route.setOnClickListener{
            val intents = Intent(this,StoredTraveledActivity::class.java)
            //intent.putExtra("tiempoTolerancia",tiempoTolerancia)
            startActivity(intents)
        }

        setupMap()

        setupPlace()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private fun fetchLocation(){
        val task = fusedLocationClient.lastLocation

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //    this.fusedLocationClient.flushLocations()
            ActivityCompat.requestPermissions(this,arrayOf( android.Manifest.permission.ACCESS_FINE_LOCATION),101)
            return
        }
        task.addOnSuccessListener {
            if(it!=null){
                Toast.makeText(this,"${it.latitude},${it.longitude}",Toast.LENGTH_SHORT).show()
                val toPosition = LatLng(it.latitude, it.longitude)
                fromLatLng = toPosition
                setMarkerFrom(toPosition)

            }
        }
    }

    private fun setupMap(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupPlace(){
        Places.initialize(applicationContext, getString(R.string.api_key ))

        btnDestiny.setOnClickListener {
            autocompleStart(TO_REQUEST_CODE)
        }
    }

    private fun autocompleStart(resultCode: Int){
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)
        startActivityForResult(intent, resultCode)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == TO_REQUEST_CODE)
            mMap.clear()
        autocompleteProccess(resultCode,data){ place ->
                //tvDestiny.text = "Destino : ${place.address}"
                place.latLng?.let{
                    toLatLng = it
                    setMarkerTo(it)
                    val URL = getDirectionURL()
                    Log.d("GoogleMap", "URL : $URL")
                    GetDirection(URL).execute()
                }
            }
        return

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun changeTravelMode(){
        mMap.clear()
        val URL = getDirectionURL()
        Log.d("GoogleMap", "URL : $URL")
        GetDirection(URL).execute()
    }

    private fun autocompleteProccess(resultCode: Int,data: Intent?, callback: (Place)->Unit){
        when (resultCode) {
            Activity.RESULT_OK -> {
                data?.let {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    Log.i(TAG, "Place: ${place.name}, ${place.id}")
                    btnDestiny.text = place.name
                    callback(place)
                }
            }
            AutocompleteActivity.RESULT_ERROR -> {
                // TODO: Handle the error.
                data?.let {
                    val status = Autocomplete.getStatusFromIntent(data)
                    status.statusMessage?.let{
                            message -> Log.i(TAG, message)
                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                // The user canceled the operation.
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMinZoomPreference(10f)
        mMap.setMaxZoomPreference(17f)
        // Add a marker in Sydney and move the camera

        mMap.uiSettings.isZoomControlsEnabled = true


        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style));

        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
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
            mMap.isMyLocationEnabled = true
            // Obtener la ultimas ubicacion conocida
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if(it != null) {
                    with(map) {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        fromLatLng = currentLocation
                        setMarkerFrom(currentLocation)
                    }
                }
            }
        }
        SetLongClick(mMap)
    }

    private fun SetLongClick(map: GoogleMap){
        map.setOnMapClickListener { latlng ->
            map.clear()
            map.addMarker(  //Marcador de la posicion
                MarkerOptions().position(latlng).title("Posicion Actual")
            ).showInfoWindow()
            map.addCircle(  // Circulo
                CircleOptions()
                    .center(latlng)
                    .strokeColor(Color.argb(50,70,70,70))
                    .fillColor(Color.argb(70,150,150,150))
                    .radius(GEOFENCE_RADIUS.toDouble())
            )
            //map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, CAMERA_ZOOM_LEVEL))
            toLatLng = LatLng(latlng.latitude, latlng.longitude)
            fromLatLng = currentLocation
            val URL = getDirectionURL()
            GetDirection(URL).execute()
            val database = Firebase.database
            val reference = database.getReference("reminders")
            val key = reference.push().key
            if(key != null){
                val reminder = Reminder(key, latlng.latitude, latlng.longitude) //Objeto de la base de datos
                reference.child(key).setValue(reminder)
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

    private fun addMarkerFrom(latLng : LatLng , title : String): Marker{
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(title)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_white_30))

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        return mMap.addMarker(markerOptions)
    }

    private fun addMarkerTo(latLng : LatLng , title : String): Marker{
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(title)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag_white_30))

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        return mMap.addMarker(markerOptions)
    }

    private fun setMarkerFrom(latLng: LatLng){
        markerFrom = addMarkerFrom(latLng,"Origen")
    }

    private fun setMarkerTo(latLng: LatLng){
        markerTo = addMarkerTo(latLng,"Destino")
    }


    private fun displayTravelInfo(distanceResponse: DistanceResponse){
        drawRoute(distanceResponse.steps)
    }

    private var lasPolyline:Polyline? = null
    private fun drawRoute(steps : List<Step>){
        Log.d(TAG,"drawRoute")
        lasPolyline?.remove()
        if(steps==null || steps.isEmpty()){
            Log.d(TAG,"No se puede dibujar la ruta ya que no hay pasos")
        }

        val options = PolylineOptions().clickable(true)

        options.add(steps[0].start_location.toLatLng())

        steps.forEach{
            options.add(it.end_location.toLatLng())
        }

       lasPolyline = mMap.addPolyline(options)

    }

    private fun latLngToString(latLng: LatLng)= "${latLng.latitude},${latLng.longitude}"

    fun getDirectionURL(origin:LatLng,dest:LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=${travelMode}&key=AIzaSyAGKhhjhbxvZft5yaeMKC3v0UbAkUPxoKM"
    }

    fun getDirectionURL() : String{
        val from = fromLatLng?.let { latLngToString(it) }
        val to = toLatLng?.let { latLngToString(it) }
        toLatLng?.let { setMarkerTo(it) }
        fromLatLng?.let { setMarkerFrom(it) }
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${from}&destination=${to}&sensor=false&mode=${travelMode}&key=AIzaSyAGKhhjhbxvZft5yaeMKC3v0UbAkUPxoKM"
    }

    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            Log.d("GoogleMap" , " data : $data")

            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data,GoogleMapDTO::class.java)

                val path =  ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size-1)){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
                //19.47991613867424, -99.1377547739467 outside
                //19.3615, -99.1514 inside eje central
                val outside = LatLng(19.47991613867424, -99.1377547739467)
                val inside = LatLng(19.3615, -99.1514)

                Log.d("SS" , "***************************************************************************************")
                if(isLocationOnPath(outside, path.toList(),true,300.0)){
                    Log.d("SS" , "Siiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiu")
                }else{
                    Log.d("SS" , " Noooooooooooooooooooooooooooooooooooooooui")
                }
            }catch (e:Exception){
                e.printStackTrace()
            }

            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(6f)
                lineoption.color(Color.rgb(0, 255, 185 ))
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }
    }

    public fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }

}