package com.example.logint

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_contacto.view.*
import kotlinx.android.synthetic.main.item_location.view.*
import kotlinx.android.synthetic.main.item_location.view.location
import kotlinx.android.synthetic.main.item_location.view.location_name

class LocationAdapter(val locationList: ArrayList<UserLocation>,val act: Activity): RecyclerView.Adapter<LocationAdapter.MyViewHolder>() {
    private var checkedPosition = -1
    private lateinit var previousItemView: View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView  = LayoutInflater.from(parent.context)
        return MyViewHolder(itemView.inflate(R.layout.item_location,parent, false),act)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.render(locationList[position],position)
//        val ubi: UserLocation = locationList[position]
//        holder.location.text = ubi.location
//        holder.location_name.text = ubi.location_name
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    fun getSelected(): String? {
        return if (checkedPosition !== -1) {
            locationList[checkedPosition].location_name
        } else null
    }

    public inner class MyViewHolder(private val itemView: View, private val act: Activity): RecyclerView.ViewHolder(itemView){
        //var location: TextView = itemView.findViewById(R.id.location)
        //var location_name: TextView = itemView.findViewById(R.id.location_name)

        fun render(location: UserLocation, selectedItem: Int){
            itemView.location.text = location.location
            itemView.location_name.text = location.location_name

            if(checkedPosition == -1){
                checkedPosition = 0
                itemView.setBackgroundResource(R.drawable.style_border_green)
                previousItemView = itemView
            }

            itemView.setOnClickListener{
                if(checkedPosition != selectedItem){
                    checkedPosition = selectedItem
                    previousItemView.setBackgroundResource(0)
                    itemView.setBackgroundResource(R.drawable.style_border_green)
                }
                previousItemView = itemView
            }

            itemView.points_menu_location.setOnClickListener {
                PopupMenu(act, itemView).apply {
                    inflate(R.menu.menu_locations)
                    setOnMenuItemClickListener {
                        when (it!!.itemId) {
                            R.id.eliminar_location -> {
                                val auth = Firebase.auth
                                val db = FirebaseFirestore.getInstance()
                                val user = auth.currentUser
                                Toast.makeText(
                                    act,
                                    "Eliminando ruta ${itemView.location_name.text}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                db.collection("routes-${user!!.uid}").document("${itemView.location_name.text}").delete()
                                db.collection("users/"+"${user!!.uid}/"+"routes/"+"${itemView.location_name.text}"+"/"+"${itemView.location_name.text}")
                                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                                        if (firebaseFirestoreException != null) {
                                            Log.e("FIRESTORE", "Cards listener error.", firebaseFirestoreException)
                                            return@addSnapshotListener
                                        }
                                        
                                        querySnapshot!!.documents.forEach { it ->
                                            it.reference.delete()
                                        }
                                    }
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }


        }
    }
}