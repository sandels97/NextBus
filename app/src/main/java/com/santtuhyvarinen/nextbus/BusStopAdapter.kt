package com.santtuhyvarinen.nextbus

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.santtuhyvarinen.nextbus.models.BusStopModel
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

//RecyclerView adapter that manages bus stop item views
class BusStopAdapter(private val context : Context, var busStopModels : List<BusStopModel>) : RecyclerView.Adapter<BusStopAdapter.ViewHolder>(){
    class ViewHolder(var root : View) : RecyclerView.ViewHolder(root) {
        val routeTextView : TextView
        val stopTimeTextView : TextView
        val nextStopTimeTextView : TextView
        val destinationTextView : TextView
        val busStopNameTextView : TextView
        val busIcon : ImageView
        val distanceText : TextView

        init {
            busIcon = root.findViewById(R.id.transportIcon)
            busStopNameTextView = root.findViewById(R.id.stopText)
            routeTextView = root.findViewById(R.id.routeText)
            stopTimeTextView = root.findViewById(R.id.stopTimeText)
            nextStopTimeTextView = root.findViewById(R.id.nextstopTimeText)
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

        //Highlight distance text if near enough
        val near = distance < 250
        holder.distanceText.typeface = if(near) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        if(near) {
            holder.distanceText.setTextColor(Color.RED)
        } else {
            holder.distanceText.setTextColor(Color.DKGRAY)
        }

        //Fill the stop time and the next stop time textViews
        if(busStopModel.stopTimes.size > 0) {
            var nextStopTime = false
            for(stopTime in busStopModel.stopTimes) {
                //Skip the stop times that have already passed
                if(stopTime.isAfterNow) {
                    if(!nextStopTime) {
                        val withinTenMinutes = isWithinTenMinutes(stopTime)
                        holder.stopTimeTextView.typeface = if(withinTenMinutes) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

                        if(withinTenMinutes) {
                            holder.stopTimeTextView.setTextColor(Color.RED)
                        } else {
                            holder.stopTimeTextView.setTextColor(Color.BLACK)
                        }

                        holder.stopTimeTextView.text = getLeaveTime(stopTime)
                        nextStopTime = true
                    } else {
                        holder.nextStopTimeTextView.text = getLeaveTime(stopTime)
                        break
                    }
                }
            }
        }
    }

    private fun isWithinTenMinutes(stopTime : DateTime) : Boolean {
        val tenMinutes = DateTime.now().plusMinutes(10)
        return !(stopTime.isAfter(tenMinutes))
    }
    //Get the stop time - if the stop time is within 10min, show only minutes to stop time
    private fun getLeaveTime(stopTime : DateTime) : String {
        if(isWithinTenMinutes(stopTime)) {
            val minutes = stopTime.minuteOfDay - DateTime().minuteOfDay
            return "$minutes m"
        } else {
            val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
            return dateTimeFormatter.print(stopTime)
        }
    }
}