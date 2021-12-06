package com.example.logint

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class GeofenceReceiver: BroadcastReceiver() { // GeofenceReceiver extiende BroadcastReceiver
    lateinit var key: String
    lateinit var message: String

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        val geofencingTransition = geofencingEvent.geofenceTransition

        if(geofencingTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofencingTransition == Geofence.GEOFENCE_TRANSITION_DWELL){
            if(intent != null){
                key = intent.getStringExtra("key")!!
                message = intent.getStringExtra("message")!!
            }

            //Recibir de Firebase
            val firebase = Firebase.database
            val reference = firebase.getReference("reminders")
            val reminderLister = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reminder = snapshot.getValue<Reminder>()
                    if(reminder != null) {
                        if (context != null) {
                            MapsActivity.showNotification(
                                context.applicationContext,
                                message
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {    // Imprimir error
                    println("Reminder:onCancelled: ${error.details}")
                }

            }

            val child = reference.child(key)
            child.addValueEventListener(reminderLister)
        }
    }
}