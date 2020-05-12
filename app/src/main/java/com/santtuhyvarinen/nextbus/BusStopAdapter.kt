package com.santtuhyvarinen.nextbus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.santtuhyvarinen.nextbus.models.BusStopModel
import org.joda.time.DateTime
import java.util.*

//RecyclerView adapter that manages bus stop item views
class BusStopAdapter(private val context : Context, var busStopModels : List<BusStopModel>) : RecyclerView.Adapter<BusStopAdapter.ViewHolder>(){
    class ViewHolder(var root : View) : RecyclerView.ViewHolder(root) {
        val routeTextView : TextView
        val leavesTextView : TextView
        val destinationTextView : TextView
        val busStopNameTextView : TextView
        val busIcon : ImageView
        val distanceText : TextView

        init {
            busIcon = root.findViewById(R.id.transportIcon)
            busStopNameTextView = root.findViewById(R.id.stopText)
            routeTextView = root.findViewById(R.id.routeText)
            leavesTextView = root.findViewById(R.id.leavesText)
            destinationTextView = root.findViewById(R.id.destinationText)
            distanceText = root.findViewById(R.id.stopDistanceText)
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

        val distance = busStopModel.distance
        holder.distanceText.text = "$distance m"

        if(busStopModel.stopTimes.size > 0) {
            holder.leavesTextView.text = getLeaveTime(busStopModel.stopTimes[0])
        }

    }

    fun getLeaveTime(seconds : Int) : String {
        val now = DateTime.now()

        val leaveMinutes = Math.floor((seconds % 3600)/60.0).toInt()
        val leaveHours = Math.floor(seconds/3600.0).toInt()

        //If departure is within 10 min of the current moment
        if(seconds - now.secondOfDay < 10 * 60) {
            val value = Math.floor((seconds - now.secondOfDay) / 60.0).toInt()
            return "$value m"
        }
        val hourString = String.format("%02d", leaveHours)
        val minuteString = String.format("%02d", leaveMinutes)
        return "$hourString:$minuteString"
    }
}