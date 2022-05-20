package com.example.logint

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main_panel.*

class TravelInfoActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var locationArrayList: ArrayList<UserLocation>
    lateinit var myAdapter: LocationAdapter
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stored_routes)



        val etRadioTolerancia: EditText = findViewById(R.id.et_radioTolerancia)
        val etTiempoTolerancia: EditText = findViewById(R.id.et_tiempoTolerancia)
        val tvIrARecorrido: TextView = findViewById(R.id.tv_irARecorrido)

        val radioTolerancia = intent.getStringExtra("name")
        val tiempoTolerancia = intent.getStringExtra("number")
        etRadioTolerancia.setText(radioTolerancia)
        etTiempoTolerancia.setText(tiempoTolerancia)
        val stringcito = "caca"
        tvIrARecorrido.setOnClickListener {
            val intents = Intent(this, OnRouteActivity::class.java)
            intents.putExtra("radioTolerancia", stringcito)

            //intent.putExtra("tiempoTolerancia",tiempoTolerancia)
            startActivity(intents)
        }



    }

}