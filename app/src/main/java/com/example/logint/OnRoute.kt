package com.example.logint

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import com.google.android.gms.maps.model.LatLng

class OnRoute : Service() {


    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    var estado=true
    lateinit var hilo : OnRoute.HiloRoute
    var banderaStop=1
    private var operation=0
    private var nameRoute=""
    var  radioTolerancia=50
    var  tiempoTolerancia =5


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

        }
        println("El valor del intent es: *************************************** "+nameRoute)
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

    class HiloRoute(puntero : OnRoute):Thread(){
        private lateinit var locationCallback: LocationCallback
        var pun = puntero
        var contador = 0
        override fun run() {
            super.run()
            outSideDetection()
        }


        fun outSideDetection(){
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
                    pun.mFusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                        val location = task.result
                        if (location != null) {
                            println("Latitudddd = ${location.latitude} Longitudddd = ${location.longitude} ")

                            val position = LatLng(
                                location.latitude,
                                location.longitude
                            )

                            if(PolyUtil.isLocationOnPath(position, GlobalClass.polyLine.toList(), true, 100.0)){
                                Log.d("SS" , "Siiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiu")
                            }else{
                                Log.d("SS" , " Noooooooooooooooooooooooooooooooooooooooui")
                            }
                        }

                    }

                }
            }
        }
    }


}