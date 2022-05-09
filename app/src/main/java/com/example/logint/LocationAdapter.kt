package com.example.logint

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocationAdapter(private val locationList: ArrayList<UserLocation>): RecyclerView.Adapter<LocationAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocationAdapter.MyViewHolder {
        val itemView  = LayoutInflater.from(parent.context).inflate(R.layout.item_location,parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LocationAdapter.MyViewHolder, position: Int) {
        val ubi: UserLocation = locationList[position]
        holder.location.text = ubi.location
        holder.location_name.text = ubi.location_name
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val location: TextView = itemView.findViewById(R.id.location)
        val location_name: TextView = itemView.findViewById(R.id.location_name)

    }
}