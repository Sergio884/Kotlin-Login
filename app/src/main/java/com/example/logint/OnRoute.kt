package com.example.logint

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ktx.database
import java.time.Instant
import java.time.ZoneId


class OnRoute : Service() {


    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    var estado=true
    lateinit var hilo : OnRoute.HiloRoute
    var banderaStop=1
    private var operation=0
    private var nameRoute=""
    var  radioTolerancia=50
    var  tiempoTolerancia =5
    lateinit var intentSOS : Intent
    var latitud = 19.47991613867424
    var longitud = -99.1377547739467
    var radioLlegada = 50


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChanel(notificationManager)
        }
        val notificationBuilder = NotificationCompat.Builder(this,"Tracking")
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle("Recorrido en curso")
            .setContentText("Seguimiento de tus cordenadas")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(1,notificationBuilder.build())

        if (intent != null) {
             radioTolerancia  = intent.getIntExtra("radioTolerancia",50)
             tiempoTolerancia  = intent.getIntExtra("tiempoTolerancia",5)
             radioLlegada = intent.getIntExtra("radioLlegada",50)
             latitud = intent.getDoubleExtra("latitud",19.47991613867424)
             longitud = intent.getDoubleExtra("longitud",-99.1377547739467)


        }


        //println("El valor del intent es: *************************************** "+nameRoute)
        hilo= OnRoute.HiloRoute(this)
        hilo.start()
        return START_STICKY

    }

    override fun onDestroy() {
        super.onDestroy()
        banderaStop=0
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this,MainPanel::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    fun alertSOS(){

        val intentSOS = Intent(this,SendLocation::class.java)
        val intentRedirection = Intent(this,MainPanel::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intentSOS)

        }else{
            startService(intentSOS)
        }
        startActivity(intentRedirection)
        val intentKill = Intent(this,OnRoute::class.java)
        stopService(intentKill)


    }
    fun goalSOS(){
        val intentKill = Intent(this,OnRoute::class.java)
        stopService(intentKill)
        /*val intentRedirection = Intent(this,MainPanel::class.java)
        intentRedirection.putExtra("ruteGooal",true)
        startActivity(intentRedirection)*/

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel(notificationManager: NotificationManager){
        val chanel = NotificationChannel(
            "Tracking",
            "Tracking Route",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(chanel)
    }

    /*@SuppressLint("MissingPermission")
    private fun createGeofence(location: LatLng, geofencingClient: GeofencingClient){
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(location.latitude, location.longitude, GEOFENCE_RADIUS.toFloat())
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL
            ).setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, GeofenceReceiver::class.java)
            .putExtra("message", "Geofence detectada")

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

                geofencingClient.addGeofences(geofenceRequest, pendingIntent)

        } else {    // Si no es android 10 o mayor
                geofencingClient.addGeofences(geofenceRequest, pendingIntent)
        }

    }*/

    class HiloRoute(puntero : OnRoute):Thread(){
        private lateinit var locationCallback: LocationCallback
        var pun = puntero
        var contador = 0
        var tiempoTolerancia = pun.tiempoTolerancia*60
        var toleranciaAux = 0
        var alerta = false
        var distance = Location("lastLocation")
        val database = Firebase.database//("http://10.0.2.2:9002?ns=tttt-d4047")
        val auth = Firebase.auth
        val user = auth.currentUser
        val reference = database.getReference("users")

        override fun run() {
            super.run()
            outSideDetection()
        }
        fun outSideDetection(){
            if (ActivityCompat.checkSelfPermission(
                    pun,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    pun,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    pun,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            } else{

                while (pun.banderaStop == 1) {
                    sleep(5000)
                    contador+=5
                    pun.mFusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(pun)
                    val mLocationRequest = LocationRequest()
                    mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    mLocationRequest.interval = 0
                    mLocationRequest.fastestInterval = 0
                    mLocationRequest.numUpdates = 1
                    pun.mFusedLocationProviderClient.flushLocations()
                    /*pun.mFusedLocationProviderClient.requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallBack,
                        Looper.getMainLooper()
                    )*/
                    if(GlobalClass.playPause == 0){
                        pun.mFusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                            val location = task.result
                            if (location != null) {
                                println("Latitudddd = ${location.latitude} Longitudddd = ${location.longitude} ")

                                if(contador ==5){
                                    distance.latitude = location.latitude
                                    distance.longitude = location.longitude
                                }

                                if(distance.distanceTo(location)>20){
                                    sendCordenates(location)
                                    distance.latitude = location.latitude
                                    distance.longitude = location.longitude
                                }

                                val position = LatLng(
                                    location.latitude,
                                    location.longitude
                                )

                                val finalLocation = Location("FinalPoint")
                                finalLocation.latitude  =pun.latitud
                                finalLocation.longitude = pun.longitud
                                //Log.d("SS" , location.distanceTo(finalLocation).toString())

                                if(location.distanceTo(finalLocation)<pun.radioLlegada){
                                    Log.d("Destino" , "Llegamos al destino")
                                    GlobalClass.polyLine.clear()
                                    pun.goalSOS()

                                }
                                if(PolyUtil.isLocationOnPath(position, GlobalClass.polyLine.toList(), true, pun.radioTolerancia.toDouble())){
                                    toleranciaAux = 0
                                    Log.d("Recorrido" , "Recorrido en marcha")
                                }else{
                                    toleranciaAux += 5
                                    if(toleranciaAux >= tiempoTolerancia){
                                        if(alerta == false){
                                            sendSMS()
                                            pun.alertSOS()
                                            Log.d("Enviar Alerta" , "Enviando Alerta")
                                            alerta = true
                                        }


                                    }
                                    Log.d("Salir" , "No se encuentra dentro del recorrido")
                                }
                            }

                        }
                    }


                }
            }
        }

        private fun sendCordenates(location: Location){
            //val key = reference.push().key
            if (user != null) {
                val reminder =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Reminder("1", location.latitude, location.longitude,
                            Instant.now().atZone(ZoneId.of("Mexico/General")).toString())
                    } else {
                        Reminder("1", location.latitude, location.longitude,"00-00-00T00:00:00")
                    }
                reference.child(user.uid).setValue(reminder)
            }
        }

        private fun sendSMS(){
            val auth = Firebase.auth
            val db = FirebaseFirestore.getInstance()
            val user = auth.currentUser
            val docs = db.collection("contacts-${user!!.uid}")
            val uid :String = user!!.uid
            val informacion = "¡ALERTA DE EMERGENCIA!\n "+user.displayName.toString()+" se encuentra en peligro te compartimos un link con el cual podras acceder a su ubicacion".replace("ñ","n").replace("á","a").replace("é","e").replace("í","i").replace("ó","o")
            val url = "safesos.online/mapa.php?u=${uid}&n="+user.displayName.toString()
            print(url)
            docs.get().addOnSuccessListener { documents ->
                for(document in documents){
                    //Log.d("contacto: ", "${document.id} => ${document.data}")
                    println("${document.id.toString()} ${document.data.toString()}")
                    val sms = SmsManager.getDefault()
                    sms.sendTextMessage(document.id.toString(),null,informacion,null,null)
                    sms.sendTextMessage(document.id.toString(),null,url,null,null)
                    /*val sendWhats = Intent(Intent.ACTION_SEND)
                    sendWhats.type = "text/plain"
                    sendWhats.setPackage("com.whatsapp")
                    sendWhats.putExtra("jid", "525587355557" + "@s.whatsapp.net");
                    sendWhats.putExtra(Intent.EXTRA_TEXT,"Prueba Api");
                    startActivity(sendWhats);*/


                }
            }

        }




    }


}