package com.example.logint

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
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
        locationArrayList = intent.getSerializableExtra("locationList") as ArrayList<UserLocation>
        initRecyclerView()
    }

    fun initRecyclerView(){
        recycleView.layoutManager = LinearLayoutManager(this)
        val adapter = LocationAdapter(locationArrayList)
        recycleView.adapter = adapter
        //Toast.makeText(this, "Muy bien selecciona una ruta guardada", Toast.LENGTH_SHORT).show()
        adapter.notifyDataSetChanged()
    }



    }







