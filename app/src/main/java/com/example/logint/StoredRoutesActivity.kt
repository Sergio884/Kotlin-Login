package com.example.logint

import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase

class StoredRoutesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var locationArrayList: ArrayList<UserLocation>
    private lateinit var myAdapter: LocationAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stored_routes)

        recyclerView = findViewById<RecyclerView>(R.id.recycleView)
        //recyclerView.setHasFixedSize(true)
        locationArrayList = arrayListOf()
        myAdapter = LocationAdapter(locationArrayList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = myAdapter
        EventChangeListener()

    }

    private fun EventChangeListener() {
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
                                    println("JajaHola ++"+dc.document.get("lat"))
                                    var result: String = null.toString()
                                    val geoCoder = Geocoder(this@StoredRoutesActivity)
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
                                    println("JajaHolad xdxd"+document.id)
                                    println("JajaHola --"+result)
                                    locationArrayList.add(UserLocation(result, document.id))
                                }
                            }
                        }
                    })


            }
        }
    }
}