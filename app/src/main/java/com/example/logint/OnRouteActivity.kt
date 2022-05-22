package com.example.logint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class OnRouteActivity : AppCompatActivity() {

    private lateinit var pathPolyLine: ArrayList<PathLocation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_route)

        pathPolyLine = intent.getParcelableArrayListExtra("path")!!
        pathPolyLine.forEach {
            //Log.d("Coordinates","lat:"+it.lat+" long:"+it.long)
            Log.d("Coordinates",it.position.toString())

        }
        var radioTolerancia  = intent.getIntExtra("radioTolerancia",50)
        var tiempoTolerancia  = intent.getIntExtra("tiempoTolerancia",5)

        Log.d("radioTolerancia: ",""+radioTolerancia.toString())
        Log.d("tiempoTolerancia: ",tiempoTolerancia.toString())


    }
}