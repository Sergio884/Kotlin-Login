package com.example.logint

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main_panel.*
import kotlinx.android.synthetic.main.activity_travel_info.*
import kotlinx.android.synthetic.main.item_location.*

class TravelInfo : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var locationArrayList: ArrayList<UserLocation>
    private lateinit var myAdapter: LocationAdapter
    private lateinit var db: FirebaseFirestore

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_info)
        //println("**********************\n\n\n\n\n**********************\nPene")
        /*recyclerView = findViewById<RecyclerView>(R.id.recycleView)
        //recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        myAdapter.notifyDataSetChanged();
        recyclerView.adapter = myAdapter
        EventChangeListener()*/
        initRecyclerView()

        //recyclerView.setBackgroundResource(R.drawable.style_border_white)
       /* val etRadioTolerancia: EditText = findViewById(R.id.et_radioTolerancia)
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
        }*/

        //*****************************************Nav Bottom

    }

    private fun EventChangeListener(){
        var auth = Firebase.auth
        val user = auth.currentUser
        db = FirebaseFirestore.getInstance()
        db.collection("routes-"+"${user!!.uid}").get().addOnSuccessListener{ result ->
            result.forEach { document ->

                //locationArrayList.add(UserLocation(document.id, ""))//get().addOnSuccessListener{ result ->
                db.collection("users/"+"${user!!.uid}/"+"routes/"+document.id+"/"+document.id).orderBy("idNumber",
                    Query.Direction.DESCENDING)
                    .limit(1).addSnapshotListener(object: EventListener<QuerySnapshot> {
                        override fun onEvent(
                            value: QuerySnapshot?,
                            error: FirebaseFirestoreException?
                        ) {
                            if(error!=null) {
                                Log.e("Firestore error", error.message.toString())
                            }
                            for(dc: DocumentChange in value?.documentChanges!!){
                                if(dc.type == DocumentChange.Type.ADDED){

                                    var lat = dc.document.get("lat").toString().toDouble()
                                    var lng = dc.document.get("lng").toString().toDouble()
                                    //println("JajaHola ++"+dc.document.get("lat"))
                                    var result: String = null.toString()
                                    val geoCoder = Geocoder(this@TravelInfo)
                                    val addressList = geoCoder.getFromLocation(lat, lng, 1)
                                    if ((addressList != null && addressList.size > 0)) {
                                        val address = addressList.get(0)
                                        val sb = StringBuilder()
                                        for (i in 0 until address.maxAddressLineIndex) {
                                            sb.append(address.getAddressLine(i)).append(", ")
                                        }
                                        sb.append(address.locality).append(", ")
                                        sb.append(address.postalCode).append(", ")
                                        sb.append(address.countryName).append(".")
                                        result = sb.toString()
                                    }
                                    //println("JajaHolad xdxd"+document.id)
                                    //println("JajaHola --"+result)
                                    locationArrayList.add(UserLocation(result, document.id))

                                }
                            }
                        }
                    })

                //myAdapter.notifyDataSetChanged()
            }
        }

    }

    fun initRecyclerView(){


        recycleView.layoutManager = LinearLayoutManager(this)
        locationArrayList = ArrayList()
        val adapter = LocationAdapter(locationArrayList)
        recycleView.adapter = adapter
        EventChangeListener()
        Toast.makeText(this, "Muy bien selecciona una ruta guardada", Toast.LENGTH_SHORT).show()
        adapter.notifyDataSetChanged()


    }



    }







