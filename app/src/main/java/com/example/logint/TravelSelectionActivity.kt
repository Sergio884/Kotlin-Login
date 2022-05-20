package com.example.logint

import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main_panel.*
import kotlinx.android.synthetic.main.activity_main_panel.bottomNavigationView
import kotlinx.android.synthetic.main.activity_travel_selection.*

class TravelSelectionActivity : AppCompatActivity() {

    var locationArrayList : ArrayList<UserLocation> = arrayListOf()
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_selection)

        EventChangeListener()
        Thread.sleep(500)
        //**************************************Pertenece al Grabado de rutas
        container_route.setOnClickListener{
            val intent = Intent(this,StoredTraveledActivity::class.java)
            intent.putExtra("locationList",locationArrayList)
 //           intent.putParcelableArrayListExtra("locationList",locationArrayList)
            startActivity(intent)

        }

        //**************************************Pertenece al edit de las rutas
        container_record.setOnClickListener {
            val intent = Intent(this,MapDistanceActivity::class.java)
            startActivity(intent)

        }


        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.navigation_contactos->{
                    val intentContactos= Intent(this,ContactActivity::class.java)
                    startActivity(intentContactos)
                    true
                }
                R.id.navigation_home->{
                    val intentHome= Intent(this,MainPanel::class.java)
                    startActivity(intentHome)
                    true
                }
                R.id.navigation_ajustes ->{
                    val intentAjustes = Intent(this,MainActivity::class.java)
                    startActivity(intentAjustes)
                    true
                }
                R.id.navigation_informacion ->{
                    val intentInfo = Intent(this,InfoActivity::class.java)
                    startActivity(intentInfo)
                    true
                }


                else -> false
            }
        }


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
                                    val geoCoder = Geocoder(this@TravelSelectionActivity)
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


}