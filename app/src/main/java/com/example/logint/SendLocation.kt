package com.example.logint

import android.app.Service
import android.content.Intent
import androidx.core.app.ActivityCompat
import com.example.logint.databinding.ActivityAccountRecoveryBinding
import com.example.logint.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.os.SystemClock.sleep
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
//import com.Login.ejgps.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import java.lang.Exception
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.logging.Handler

class SendLocation : Service() {
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    var estado=true
    lateinit var hilo :Hilo
    var banderaStop=1
    private var operation=0
    private var nameRoute=""
    //lateinit var location: Location

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
                .setContentTitle("Transmitiendo tu trayecto")
                .setContentText("Enviando tus coordenadas")
                .setContentIntent(getMainActivityPendingIntent())

            startForeground(1,notificationBuilder.build())




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
            IMPORTANCE_HIGH
        )
            notificationManager.createNotificationChannel(chanel)
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this,MainPanel::class.java),
        FLAG_UPDATE_CURRENT
    )


    class Hilo(p:SendLocation):Thread(){
        private lateinit var locationCallback: LocationCallback
        var pun = p
        override fun run() {
            super.run()
                sendSOSLocation()
        }

        private fun sendSOSLocation(){
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
                    pun.mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(pun)
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
                                // requestNewLocationData()
                                println("Latitudd = ${location.latitude} Longitud = ${location.longitude}")
                                val database =
                                    Firebase.database//("http://10.0.2.2:9002?ns=tttt-d4047")
                                val auth = Firebase.auth
                                val user = auth.currentUser
                                val reference = database.getReference("users")
                                //val key = reference.push().key
                                if (user != null) {
                                    val reminder =
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            Reminder("1", location.latitude, location.longitude,Instant.now().atZone(ZoneId.of("Mexico/General")).toString())
                                        } else {
                                            Reminder("1", location.latitude, location.longitude,"00-00-00T00:00:00")
                                        }
                                    reference.child(user.uid).setValue(reminder)
                                }


                        }


                    }
                }
            }
        }


        private fun startLocationUpdates() {
            if (ActivityCompat.checkSelfPermission(
                    pun,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    pun,
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
            pun.mFusedLocationProviderClient.requestLocationUpdates(LocationRequest(),
                LocationCallback(),
                Looper.getMainLooper())
        }

        @SuppressLint("MissingPermission")
        private fun requestNewLocationData(){
            val mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequest.interval = 0
            mLocationRequest.fastestInterval = 0
            mLocationRequest.numUpdates = 1
            pun.mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(pun)
            pun.mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper())
        }

        private val mLocationCallBack = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                val location : Location = locationResult.lastLocation
                println("Latituds = ${location.latitude} Longitud = ${location.longitude}")
                val database = Firebase.database //("http://10.0.2.2:9002?ns=tttt-d4047")

                val auth = Firebase.auth
                val user = auth.currentUser
                val reference = database.getReference("users")
                //val key = reference.push().key
                if(user != null){
                    val reminder = Reminder(user.uid, location.latitude,location.longitude)
                    reference.child(user.uid).setValue(reminder)
                }
//                val database = Firebase.database
//                val reference = database.getReference("reminders")
//                val key = reference.push().key
//                if(key != null){
//                    val reminder = Reminder(key, location.latitude,location.longitude)
//                    reference.child(key).setValue(reminder)
//                }

            }
        }

        private fun sendSMS(){

            val auth = Firebase.auth
            val db = FirebaseFirestore.getInstance()
            val user = auth.currentUser
            val docs = db.collection("contacts-${user!!.uid}")
            val info = "Alerta de Emergencia\n ${user.displayName} se encuentra en peligro te compartimos un link con el cual podras acceder a su ubicacion \n url: http://tt2021/location?uid=${user.uid}"
            docs.get().addOnSuccessListener { documents ->
                for(document in documents){
                    //Log.d("contacto: ", "${document.id} => ${document.data}")
                    println("${document.id.toString()} ${document.data.toString()}")
                    try{
                        val sms :SmsManager= SmsManager.getDefault()
                        sms.sendTextMessage(document.id.toString(),null,info,null,null)
                    }catch (e :Exception){
                        e.printStackTrace()
                    }


                }
            }


        }
    }


}