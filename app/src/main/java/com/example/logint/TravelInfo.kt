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

        recyclerView = findViewById<RecyclerView>(R.id.recycleView)
        //recyclerView.setHasFixedSize(true)

        locationArrayList = arrayListOf()
        myAdapter = LocationAdapter(locationArrayList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = myAdapter
        EventChangeListener()

        val radioTolerancia = intent.getStringExtra("name")
        val tiempoTolerancia = intent.getStringExtra("number")

        etRadioTolerancia.setText(radioTolerancia)
        etTiempoTolerancia.setText(tiempoTolerancia)
        val stringcito = "caca"
        tvIrARecorrido.setOnClickListener {
            val intents = Intent(this,MapDistanceActivity::class.java)
            intents.putExtra("radioTolerancia",stringcito)
            //intent.putExtra("tiempoTolerancia",tiempoTolerancia)
            startActivity(intents)
        }
    }

    private fun EventChangeListener(){
        var auth = Firebase.auth
        val user = auth.currentUser
        db = FirebaseFirestore.getInstance()
        //db.collection("users").
        db.collection("users-${user!!.uid}-routes").
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
                                //locationArrayList.add(dc.document.toObject(UserLocation::class.java))
                                //dc.document.getData().orderBy("name", Query.Direction.ASCENDING).limit(1)
                                val userLocation = UserLocation(dc.document.id,"")
                                //dc.document.getDocumentReference()
                                Log.e("Pene", dc.document.id)
                                locationArrayList.add(userLocation)
                            }
                        }

                        myAdapter.notifyDataSetChanged()

                    }
                })

    }
}
