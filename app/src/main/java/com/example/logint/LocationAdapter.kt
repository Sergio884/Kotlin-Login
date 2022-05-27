package com.example.logint

import android.annotation.SuppressLint
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
import com.google.android.gms.maps.model.LatLng
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
    var pathPolyLine : ArrayList<PathLocation> = ArrayList()
    lateinit var toLatLng: LatLng

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
        if (checkedPosition !== -1) {
            return locationList[checkedPosition].location_name
        } else return null
    }

    fun getSelectedPath(): ArrayList<PathLocation>? {
        return if (checkedPosition !== -1) {
            return pathPolyLine
        } else null
    }

    fun getSelectedPathLastPoint(): LatLng? {
        return if (checkedPosition !== -1) {
            return toLatLng
        } else null
    }

    private fun getFireStorePoints(route: String?){

        var auth = Firebase.auth
        val user = auth.currentUser
        var db = FirebaseFirestore.getInstance()

        //locationArrayList.add(UserLocation(document.id, ""))//get().addOnSuccessListener{ result ->
        db.collection("users/"+"${user!!.uid}/"+"routes/"+route+"/"+route).orderBy("idNumber",
            Query.Direction.ASCENDING)
            .addSnapshotListener(object: EventListener<QuerySnapshot> {
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if(error!=null) {
                        Log.e("Firestore error", error.message.toString())
                    }
                    var tam = value?.size()
                    var contador = 0
                    println("JajaHola "+route)
                    for(dc: DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){
                            val lat = dc.document.get("lat").toString().toDouble()
                            val lng = dc.document.get("lng").toString().toDouble()
                            pathPolyLine.add(PathLocation(
                                LatLng(dc.document.get("lat").toString().toDouble(),
                                    dc.document.get("lng").toString().toDouble())
                            ))
                            if (tam != null) {
                                if(contador == tam-1){
                                    toLatLng = LatLng(lat,lng)
                                    println("JajaHola el LatLng es "+toLatLng)
                                }
                            }
                            contador++
                        }
                    }
                }
            })

        //myAdapter.notifyDataSetChanged()

    }

    public inner class MyViewHolder(private val itemView: View, private val act: Activity): RecyclerView.ViewHolder(itemView){
        //var location: TextView = itemView.findViewById(R.id.location)
        //var location_name: TextView = itemView.findViewById(R.id.location_name)

        @SuppressLint("ResourceAsColor")
        fun render(location: UserLocation, selectedItem: Int){
            itemView.location.text = location.location
            itemView.location_name.text = location.location_name
            itemView.setBackgroundResource(R.drawable.style_border_white)
            if(checkedPosition == -1){
                //checkedPosition = 0
                //itemView.setBackgroundResource(R.drawable.style_border_green)
                previousItemView = itemView
            }

            itemView.setOnClickListener{
                if(checkedPosition != selectedItem){
                    pathPolyLine = ArrayList()
                    toLatLng = LatLng(0.0, 0.0)
                    checkedPosition = selectedItem
                    previousItemView.setBackgroundResource(R.drawable.style_border_white)
                    previousItemView.location_name.setTextColor(ContextCompat.getColor(it.context,R.color.white))
                    previousItemView.location.setTextColor(ContextCompat.getColor(it.context,R.color.white))
                    previousItemView.icon_locations.setImageResource(R.drawable.ic_location)

                    itemView.icon_locations.setImageResource(R.drawable.ic_location_green)
                    itemView.setBackgroundResource(R.drawable.style_border_green)
                    itemView.location_name.setTextColor(ContextCompat.getColor(it.context,R.color.Green))
                    itemView.location.setTextColor(ContextCompat.getColor(it.context,R.color.Green))
                    getFireStorePoints(location.location_name)
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
                                itemView.visibility = View.GONE
                                val params: ViewGroup.LayoutParams =
                                    itemView.getLayoutParams()
                                params.height = 0
                                params.width = 0
                                itemView.setLayoutParams(params)
                                if(checkedPosition == selectedItem){
                                    previousItemView.setBackgroundResource(0)
                                    checkedPosition = -1
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