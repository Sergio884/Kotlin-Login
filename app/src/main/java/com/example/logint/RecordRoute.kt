package com.example.logint

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
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
import java.lang.Exception

class RecordRoute : Service() {

    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    var estado=true
    lateinit var hilo : HiloRecord
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
            .setContentTitle("Grabando Recorrido")
            .setContentText("Enviando tus cordenadas")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(1,notificationBuilder.build())

        if (intent != null) {
            nameRoute = intent.getStringExtra("nameRoute")!!
        }
        println("El valor del intent es: *************************************** "+nameRoute)
            hilo= HiloRecord(this)
            hilo.start()
            return START_STICKY

    }

    override fun onDestroy() {
        super.onDestroy()
        banderaStop=0
    }

    public fun getNameRoute():String{
        return nameRoute
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this,MainPanel::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    public fun setNameRoute(nameRoute :String){
        this.nameRoute=nameRoute
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



    class HiloRecord(puntero:RecordRoute):Thread(){
        private lateinit var locationCallback: LocationCallback
        var pun = puntero
        var contador = 0
        override fun run() {
            super.run()
            sendSOSLocation()
        }

        private fun sendSOSLocation(){
            var idLatLngRoute=0;
            var auth = Firebase.auth;
            val db = FirebaseFirestore.getInstance();
            val user = auth.currentUser;
            db.collection("routes-"+user!!.uid.toString()).document(pun.getNameRoute()).set(
                hashMapOf("name" to pun.getNameRoute())
            )

            //sendSMS()
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
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
                    pun.mFusedLocationProviderClient.requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallBack,
                        Looper.getMainLooper()
                    )
                    pun.mFusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                        val location = task.result
                        if (location != null) {
                                println("Latitudddd = ${location.latitude} Longitudddd = ${location.longitude} y nombre:${pun.getNameRoute()}")
                                var auth = Firebase.auth
                                val db = FirebaseFirestore.getInstance()
                                val user = auth.currentUser
                                db.collection("users").
                                document(user!!.uid.toString()).
                                collection("routes").
                                document(pun.getNameRoute()).
                                collection(pun.getNameRoute()).
                                document(""+idLatLngRoute).
                                set(hashMapOf("lat" to "${location.latitude}","lng" to "${location.longitude}","segundos" to contador,"idNumber" to idLatLngRoute))
                                idLatLngRoute = idLatLngRoute +1



                               /* collection(""+idLatLngRoute).
                                document(""+idLatLngRoute).set(hashMapOf("lat" to "${location.latitude}","lng" to "${location.longitude}"))
                                idLatLngRoute = idLatLngRoute +1*/

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
            pun.mFusedLocationProviderClient.requestLocationUpdates(
                LocationRequest(),
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
                        val sms : SmsManager = SmsManager.getDefault()
                        sms.sendTextMessage(document.id.toString(),null,info,null,null)
                    }catch (e : Exception){
                        e.printStackTrace()
                    }


                }
            }


        }
    }
}