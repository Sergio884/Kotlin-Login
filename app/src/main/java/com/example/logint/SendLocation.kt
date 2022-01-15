package com.example.logint

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.example.logint.databinding.ActivityAccountRecoveryBinding
import com.example.logint.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.HandlerThread
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
//import com.Login.ejgps.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.logging.Handler

class SendLocation : Service() {
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    var estado=true
    lateinit var location: Location

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
      /*  if(checkPermissions()){
            if(isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }else{

        }*/
        val hilo = Hilo(this)
        hilo.start()


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class Hilo(p:SendLocation,):Thread(){

        var pun = p
        var i = 0

        override fun run(){
            super.run()
            startLocationUpdates()
            while(i<10){
                sleep(5000)

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
                else{
                    pun.mFusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                        val location= task.result
                        if(location!=null){
                           // requestNewLocationData()
                           println("Latitudd = ${location.latitude} Longitud = ${location.longitude}")
                            val database = Firebase.database
                            val reference = database.getReference("reminders")
                            val key = reference.push().key
                                if(key != null){
                                    val reminder = Reminder(key, location.latitude,location.longitude) //Objeto de la base de datos
                                    reference.child(key).setValue(reminder)
                                }
                            i++
                            }
                        else{

                            i++
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
                val database = Firebase.database
                val reference = database.getReference("reminders")
                val key = reference.push().key
                if(key != null){
                    val reminder = Reminder(key, location.latitude,location.longitude) //Objeto de la base de datos
                    reference.child(key).setValue(reminder)
                }

            }
        }
    }
}