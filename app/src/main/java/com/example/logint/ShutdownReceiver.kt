package com.example.logint

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.time.ZoneId

class ShutdownReceiver: BroadcastReceiver() { // GeofenceReceiver extiende BroadcastReceiver

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var hilo : ShutdownReceiver.Hilo

    override fun onReceive(context: Context?, intent: Intent?) {

        print("siiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiuuuuu")
            if (GlobalClass.course < 1){
                print("No enviaremos la Ubicación")
            }
            else{

                hilo= Hilo()
                hilo.start()
                print("Enviaremos la Ubicación")
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)


                if (ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                fusedLocationClient.lastLocation.addOnSuccessListener { location : Location ->

                    //println("Latitudddd = ${location.latitude} Longitudddd = ${location.longitude}  curso = ${GlobalClass.course}")
                    val database =
                        Firebase.database//("http://10.0.2.2:9002?ns=tttt-d4047")
                    val auth = Firebase.auth
                    val user = auth.currentUser
                    val reference = database.getReference("users")
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

            }

    }

    class Hilo():Thread(){

        override fun run() {
            super.run()
            //println("Si ****************************************************************************")
            sendSMS()
        }
        private fun sendSMS(){
            val auth = Firebase.auth
            val db = FirebaseFirestore.getInstance()
            val user = auth.currentUser
            val docs = db.collection("contacts-${user!!.uid}")
            val uid :String = user!!.uid
            //val informacion = "¡ALERTA DE EMERGENCIA!\n "+user.displayName.toString()+" se encuentra en peligro te compartimos un link con el cual podras acceder a su ubicacion".replace("ñ","n").replace("á","a").replace("é","e").replace("í","i").replace("ó","o")
            val url = user.displayName.toString()+" se encuentra en peligro ".replace("ñ","n").replace("á","a").replace("é","e").replace("í","i").replace("ó","o")+"safesos.online/mapa.php?u=${uid}&n="+user.displayName.toString()
            docs.get().addOnSuccessListener { documents ->
                for(document in documents){
                    //Log.d("contacto: ", "${document.id} => ${document.data}")
                    //println("${document.id.toString()} ${document.data.toString()}")
                    val sms = SmsManager.getDefault()
                    //sms.sendTextMessage(document.id.toString(),null,informacion,null,null)
                    sms.sendTextMessage(document.id.toString(),null,url,null,null)
                }
            }

        }

    }



}