package com.example.logint

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_location.view.*

class LocationAdapter(val locationList: ArrayList<UserLocation>): RecyclerView.Adapter<LocationAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView  = LayoutInflater.from(parent.context).inflate(R.layout.item_location,parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.render(locationList[position])

//        val ubi: UserLocation = locationList[position]
//        holder.location.text = ubi.location
//        holder.location_name.text = ubi.location_name
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    public class MyViewHolder(val itemView: View): RecyclerView.ViewHolder(itemView){

        //var location: TextView = itemView.findViewById(R.id.location)
        //var location_name: TextView = itemView.findViewById(R.id.location_name)

        fun render(location: UserLocation){
            itemView.location.text = location.location
            itemView.location_name.text = location.location_name
        }
    }
}