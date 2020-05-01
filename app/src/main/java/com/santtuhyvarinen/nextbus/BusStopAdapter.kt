package com.santtuhyvarinen.nextbus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.santtuhyvarinen.nextbus.models.BusStopModel

//RecyclerView adapter that manages bus stop item views
class BusStopAdapter(val context : Context, val busStopModels : List<BusStopModel>) : RecyclerView.Adapter<BusStopAdapter.ViewHolder>(){
    class ViewHolder(var root : View) : RecyclerView.ViewHolder(root) {
        val busNumberTextView : TextView
        val arrivalTimeTextView : TextView
        val busStopNameTextView : TextView
        val busIcon : ImageView

        init {
            busIcon = root.findViewById(R.id.transportIcon)
            busStopNameTextView = root.findViewById(R.id.busStopNameText)
            busNumberTextView = root.findViewById(R.id.busNumberText)
            arrivalTimeTextView = root.findViewById(R.id.arrivalTimeText)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_busstop, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return busStopModels.size
    }

    //Set the bus stop info
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val busStopModel = busStopModels[position]
        holder.busStopNameTextView.text = busStopModel.name
        holder.busNumberTextView.text = busStopModel.busNumber
    }
}