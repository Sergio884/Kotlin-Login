package com.example.logint

import android.Manifest
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
import com.google.android.gms.location.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.lang.NullPointerException

class SecurityZone : Service() {

    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    var estado=true
    lateinit var hilo : SecurityZone.Hilo
    var banderaStop=1
    private var operation=0
    private var nameRoute=""
    var locationZone = Location("Zone")
    var radio = 50

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
            .setContentTitle("Zona Segura")
            .setContentText("Registrando tus coordenadas")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(1,notificationBuilder.build())

        if (intent != null) {

            locationZone.latitude = intent.getDoubleExtra("latitud",19.47991613867424)
            locationZone.longitude = intent.getDoubleExtra("longitud",-99.1377547739467)
            radio = intent.getIntExtra("radio",50)
        }
        Log.d("siiiiii",locationZone.toString())
        hilo= Hilo(this)
        hilo.start()
        return START_STICKY

    }

    override fun onDestroy() {
        super.onDestroy()
        banderaStop=0
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

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this,MainPanel::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    fun alertSOS(){

        val intentSOS = Intent(this,SendLocation::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intentSOS)
        }else{
            startService(intentSOS)
        }

        val intentKill = Intent(this,SecurityZone::class.java)
        stopService(intentKill)


    }

    class Hilo(puntero:SecurityZone):Thread(){

        var pun = puntero
        var contador = 0
        override fun run() {
            super.run()
            checkZone()
        }

        fun checkZone(){
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

                while (pun.banderaStop==1) {
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
                    pun.mFusedLocationProviderClient.requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallBack,
                        Looper.getMainLooper()
                    )
                    pun.mFusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                        val location = task.result
                        if (location != null) {
                            Log.d("Location","Latitudddd = ${location.latitude} Longitudddd = ${location.longitude} ")
                            if(location.distanceTo(pun.locationZone)>pun.radio){
                                sendSMS()
                                Thread.sleep(3000)
                                pun.banderaStop=0
                                GlobalClass.radio = 50
                                pun.alertSOS()
                            }
                        }
                    }
                }
            }
        }

        private val mLocationCallBack = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
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

                }
            }

        }
    }

}