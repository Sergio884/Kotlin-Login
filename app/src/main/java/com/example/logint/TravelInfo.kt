package com.example.logint

import android.content.Intent
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

class TravelInfo : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var locationArrayList: ArrayList<UserLocation>
    private lateinit var myAdapter: LocationAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_info)

        val etRadioTolerancia: EditText = findViewById(R.id.et_radioTolerancia)
        val etTiempoTolerancia: EditText = findViewById(R.id.et_tiempoTolerancia)
        val tvIrARecorrido: TextView = findViewById(R.id.tv_irARecorrido)

        recyclerView = findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        locationArrayList = arrayListOf()
        myAdapter = LocationAdapter(locationArrayList)
        recyclerView.adapter = myAdapter
        EventChangeListener()

        val radioTolerancia = intent.getStringExtra("name")
        val tiempoTolerancia = intent.getStringExtra("number")

        etRadioTolerancia.setText(radioTolerancia)
        etTiempoTolerancia.setText(tiempoTolerancia)
        val stringcito = "caca"
        tvIrARecorrido.setOnClickListener {
            val intent = Intent(this,TravelInfo::class.java)
            intent.putExtra("radioTolerancia",stringcito)
            //intent.putExtra("tiempoTolerancia",tiempoTolerancia)
            startActivity(intent)
        }
    }

    private fun EventChangeListener(){
        db = FirebaseFirestore.getInstance()
        db.collection("users").
                addSnapshotListener(object: EventListener<QuerySnapshot>{
                    override fun onEvent(
                        value: QuerySnapshot?,
                        error: FirebaseFirestoreException?
                    ){
                        if(error != null){
                            Log.e("Error de Firestore", error.message.toString())
                        }

                        for(dc: DocumentChange in value?.documentChanges!!){
                            if(dc.type == DocumentChange.Type.ADDED){
                                locationArrayList.add(dc.document.toObject(UserLocation::class.java))
                            }
                        }

                        myAdapter.notifyDataSetChanged()

                    }
                })

    }
}
