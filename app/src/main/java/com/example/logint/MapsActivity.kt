package com.example.logint

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.random.Random

const val LOCATION_REQUEST_CODE = 123
const val GEOFENCE_LOCATION_REQUEST_CODE = 12345
const val CAMERA_ZOOM_LEVEL = 13f
const val GEOFENCE_RADIUS = 500
const val GEOFENCE_ID = "REMINDER_GEOFENCE_ID"
const val GEOFENCE_EXPIRATION = 10* 24 * 60 * 60 // Duracion del Geofence (10 Dias)
const val GEOFENCE_DWELL_DELAY = 10 * 1000 // 10 Segundos+

private val TAG = MapsActivity::class.java.simpleName //Por si necesitamos logear usamos este tag

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var currentLocation: LatLng
    private lateinit var destinyLocation: LatLng


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)


            println(this.getIntent().getStringExtra("radioTolerancia")+"_________")
            println(intent.getStringExtra("radioTolerancia")+"!!!!!!!!!!!!!!!")

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

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
                        moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, CAMERA_ZOOM_LEVEL))
                        /*readData(destino)
                        val URL = getDirectionURL(currentLocation,destinyLocation)
                        GetDirection(URL).execute()*/
                    }
                }
                else { // Si no tenemos la ultima ubicacion conocida
                    with(map){
                        moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(25.05, 25.05),
                                CAMERA_ZOOM_LEVEL
                            )
                        )
                    }
                }
            }
        }

        SetLongClick(map)
    }

    fun graficaRuta(coordenadas: ArrayList<LatLng>){
        val lineoption = PolylineOptions()
        lineoption.addAll(coordenadas)
        lineoption.width(10f)
        lineoption.color(Color.BLUE)
        lineoption.geodesic(true)
        map.addPolyline(lineoption)
    }


    private fun readData(destino: String) {
        /*fAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        var userId: String? = this.fAuth.currentUser?.uid
        var documentReference = db.collection("users/").document(userId.toString()+"/routes/"+destino+"/"+opcion+"/"+opcion)
        destinyLocation = LatLng(documentReference.get("lat").value as Double, documentReference.get("lng").value as Double) */

        var auth = Firebase.auth
        val db = FirebaseFirestore.getInstance()
        val user = auth.currentUser
        val coordenadas = ArrayList<LatLng>()
        var routeReference = db.collection("users").
        document(user!!.uid.toString()).
        collection("routes").
        document(destino).
        collection(destino).get().addOnSuccessListener {
                result ->
            for(documento in result){
                coordenadas.add(LatLng(documento.data.get("lat") as Double, documento.data.get("lng") as Double))
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error getting documents: ", exception)
        }

        graficaRuta(coordenadas)

    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun SetLongClick(map: GoogleMap){
        map.setOnMapClickListener { latlng ->
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
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, CAMERA_ZOOM_LEVEL))
            destinyLocation = LatLng(latlng.latitude, latlng.longitude)
            val URL = getDirectionURL(currentLocation,destinyLocation)
            GetDirection(URL).execute()
            val database = Firebase.database
            val reference = database.getReference("reminders")
            val key = reference.push().key
            if(key != null){
                val reminder = Reminder(key, latlng.latitude, latlng.longitude) //Objeto de la base de datos
                reference.child(key).setValue(reminder)
            }


            createGeofence(latlng, key!!, geofencingClient)
        }
    }

    fun getDirectionURL(origin:LatLng,dest:LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyAGKhhjhbxvZft5yaeMKC3v0UbAkUPxoKM"
    }

    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()
            Log.d("GoogleMap" , " data : $data")
            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)

                val path =  ArrayList<LatLng>()

                for (i in 0 until respObj.routes[0].legs[0].steps.size){
//                    val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
//                    path.add(startLatLng)
//                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            map.addPolyline(lineoption)
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
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
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

    private fun createGeofence(location: LatLng, key:String, geofencingClient: GeofencingClient){
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(location.latitude, location.longitude, GEOFENCE_RADIUS.toFloat())
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or GEOFENCE_TRANSITION_DWELL
            ).setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, GeofenceReceiver::class.java)
            .putExtra("key", key)
            .putExtra("message", "Geofence detectada :D")

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if(ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    GEOFENCE_LOCATION_REQUEST_CODE
                )
            } else {
                geofencingClient.addGeofences(geofenceRequest, pendingIntent)
            }
        } else {    // Si no es android 10 o mayor
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GEOFENCE_LOCATION_REQUEST_CODE){
            if(permissions.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(
                    this,
                    "Esta aplicacion necesita la ubicacion de fondo para ser activada",
                    Toast.LENGTH_LONG
                ).show()
                // Volver a pedir permisos aqui
            }
        }

        if(requestCode == LOCATION_REQUEST_CODE){
            if(grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        || grantResults[1] == PackageManager.PERMISSION_GRANTED
                        )
            ){
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
                map.isMyLocationEnabled = true
                onMapReady(map)
            } else {
                Toast.makeText(
                    this,
                    "Esta aplicacion necesita la ubicacion d efondo para ser activada",
                    Toast.LENGTH_LONG
                ).show()
                // Volver a pedir permisos aqui
            }
        }
    }

    companion object {
        fun showNotification(context: Context, message: String) {
            val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
            var notificationId = 1554
            notificationId += Random(notificationId).nextInt(1, 30)

            val notificationBuilder =
                NotificationCompat.Builder(context.applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_alarm_24)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(message)
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(message)
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = context.getString(R.string.app_name) }

                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }

}
