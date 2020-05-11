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
class BusStopAdapter(private val context : Context, private val busStopModels : List<BusStopModel>) : RecyclerView.Adapter<BusStopAdapter.ViewHolder>(){
    class ViewHolder(var root : View) : RecyclerView.ViewHolder(root) {
        val routeTextView : TextView
        val leavesTextView : TextView
        val destinationTextView : TextView
        val busStopNameTextView : TextView
        val busIcon : ImageView

        init {
            busIcon = root.findViewById(R.id.transportIcon)
            busStopNameTextView = root.findViewById(R.id.stopText)
            routeTextView = root.findViewById(R.id.routeText)
            leavesTextView = root.findViewById(R.id.leavesText)
            destinationTextView = root.findViewById(R.id.destinationText)
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
        holder.busStopNameTextView.text = busStopModel.stopName
        holder.routeTextView.text = busStopModel.route
        holder.destinationTextView.text = busStopModel.destinationName
        holder.busIcon.setImageDrawable(busStopModel.getDrawable(context))
    }
}