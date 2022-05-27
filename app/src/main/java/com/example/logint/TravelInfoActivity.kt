package com.example.logint

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_main_panel.*
import kotlinx.android.synthetic.main.activity_stored_routes.*
import kotlin.collections.ArrayList as ArrayList

class TravelInfoActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    private lateinit var pathPolyLine: ArrayList<PathLocation>
    lateinit var myAdapter: LocationAdapter
    lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stored_routes)
        //pathPolyLine = intent.getParcelableArrayListExtra("coordinates")!!
        //pathPolyLine = intent.getSerializableExtra("PathLocation") as ArrayList<PathLocation>
        if( isMyServiceRunning(SendLocation::class.java) == true){
            val intents = Intent(this,MainPanel::class.java)
            startActivity(intents)

        }
        pathPolyLine = intent.getParcelableArrayListExtra("path")!!
        pathPolyLine.forEach {
            //Log.d("Coordinates","lat:"+it.lat+" long:"+it.long)
            Log.d("Coordinates",it.position.toString())

        }
        val latitud = intent.getDoubleExtra("latitud",19.47991613867424)
        val longitud = intent.getDoubleExtra("longitud",-99.1377547739467)
        //pathPolyLine = coordinates?.getParcelableArrayList<LatLng>("coordinates") as ArrayList<LatLng>
        val etRadioTolerancia: EditText = findViewById(R.id.et_radioTolerancia)
        val etTiempoTolerancia: EditText = findViewById(R.id.et_tiempoTolerancia)
        val tvIrARecorrido: TextView = findViewById(R.id.tv_irARecorrido)
        val etRadioLlegada: EditText = findViewById(R.id.et_radioLlegada)
        val radioTolerancia = intent.getStringExtra("name")
        val tiempoTolerancia = intent.getStringExtra("number")
        etRadioTolerancia.setText(radioTolerancia)
        etTiempoTolerancia.setText(tiempoTolerancia)

        tvIrARecorrido.setOnClickListener {

            if(etRadioTolerancia.text.toString() != "" && etTiempoTolerancia.text.toString() != "" && etRadioLlegada.text.toString() != ""){
                val intents = Intent(this, OnRouteActivity::class.java)
                intents.putExtra("radioTolerancia", etRadioTolerancia.text.toString().toInt())
                intents.putExtra("tiempoTolerancia",etTiempoTolerancia.text.toString().toInt())
                intents.putExtra("radioLlegada",etRadioLlegada.text.toString().toInt())
                intents.putExtra("latitud", latitud)
                intents.putExtra("longitud",longitud)
                intents.putParcelableArrayListExtra("path",pathPolyLine)
                //intent.putExtra("tiempoTolerancia",tiempoTolerancia)
                startActivity(intents)
            }
            else{
                Toast.makeText(this, "Debes de llenar todos los campos", Toast.LENGTH_LONG).show()
            }

        }

        /*etRadioTolerancia.setOnKeyListener ( View.OnKeyListener { v, keycode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_UP) {
                var tolerancia = etRadioTolerancia.text.toString()
                if(tolerancia.toInt() > 1000){
                    var tam = etRadioTolerancia.text.toString().length
                    var i =0
                    var cambio = ""
                    tolerancia.forEach {
                        if (i < tam)
                        cambio=cambio+it.toString()
                    }

                    etRadioTolerancia.setText(cambio)

                }
                return@OnKeyListener true
            }
            false

        } )*/



    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

}