package com.example.logint

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main_panel.*
import kotlinx.android.synthetic.main.activity_travel_info.*
import kotlinx.android.synthetic.main.item_location.*

class StoredTraveledActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var locationArrayList: ArrayList<UserLocation>
    private lateinit var myAdapter: LocationAdapter
    private lateinit var db: FirebaseFirestore

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_info)
        locationArrayList = (intent.getSerializableExtra("locationList") as ArrayList<UserLocation>?)!!
        initRecyclerView()
        tv_irARecorrido.setOnClickListener{
            //val intent = Intent(this,StoredTraveledActivity::class.java)
            //intent.putExtra("locationList",locationArrayList)
            //           intent.putParcelableArrayListExtra("locationList",locationArrayList)
            //startActivity(intent)
            if(myAdapter.getSelected() == null){
                Toast.makeText(
                    this@StoredTraveledActivity,
                    "Seleccione un viaje guardado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                println("JajaHola" + myAdapter.getSelected())
            }
            //Toast.makeText(this, "Muy bien selecciona una ruta guardada", Toast.LENGTH_SHORT).show()
        }
    }

    fun initRecyclerView(){
        recycleView.layoutManager = LinearLayoutManager(this)
        val adapter = LocationAdapter(locationArrayList,this)
        recycleView.adapter = adapter
        //Toast.makeText(this, "Muy bien selecciona una ruta guardada", Toast.LENGTH_SHORT).show()
        adapter.notifyDataSetChanged()
        myAdapter = LocationAdapter(locationArrayList,this)
        recycleView.adapter = myAdapter
        myAdapter.notifyDataSetChanged()
    }

    fun showPopUp(v: View) {

        PopupMenu(this, v).apply {

            inflate(R.menu.menu_locations)
            setOnMenuItemClickListener {
                when (it!!.itemId) {
                    R.id.eliminar_location -> {
                        Toast.makeText(
                            this@StoredTraveledActivity,
                            "Eliminando Contacto",
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }


    }







