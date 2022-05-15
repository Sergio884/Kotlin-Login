package com.example.logint

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ShutdownReceiver: BroadcastReceiver() { // GeofenceReceiver extiende BroadcastReceiver


    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("GoogleMap", "La combi versace")
        println("Siuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu")
    }
}