package com.santtuhyvarinen.nextbus.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.santtuhyvarinen.nextbus.R
import com.santtuhyvarinen.nextbus.models.BusStopModel
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

//RecyclerView adapter that manages bus stop item views
class BusStopAdapter(private val context : Context, var busStopModels : List<BusStopModel>) : RecyclerView.Adapter<BusStopAdapter.ViewHolder>(){

    var busStopAdapterListener: BusStopAdapterListener? = null

    private var highlightStopTime = false

    private val distanceConsideredNear : Int

    init {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        highlightStopTime = sharedPreferences.getBoolean("highlight_stoptime_key", true)
        distanceConsideredNear = sharedPreferences.getInt("stop_near_key", 500)
    }
    class ViewHolder(val root : View) : RecyclerView.ViewHolder(root) {
        val routeTextView : TextView
        val stopTimeTextView : TextView
        val nextStopTimeTextView : TextView
        val destinationTextView : TextView
        val busStopNameTextView : TextView
        val busIcon : ImageView
        val distanceText : TextView
        val highlightBackground : View
        val favoriteIcon : ImageView

        init {
            busIcon = root.findViewById(R.id.transportIcon)
            busStopNameTextView = root.findViewById(R.id.stopText)
            routeTextView = root.findViewById(R.id.routeText)
            stopTimeTextView = root.findViewById(R.id.stopTimeText)
            nextStopTimeTextView = root.findViewById(R.id.nextstopTimeText)
            destinationTextView = root.findViewById(R.id.destinationText)
            distanceText = root.findViewById(R.id.stopDistanceText)
            highlightBackground = root.findViewById(R.id.stopTimeHighlight)
            favoriteIcon = root.findViewById(R.id.favoriteIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_busstop, parent, false)
        return ViewHolder(
            view
        )
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
        val near = distance < distanceConsideredNear
        holder.distanceText.typeface = if(near) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        if(near) {
            holder.distanceText.setTextColor(Color.RED)
        } else {
            holder.distanceText.setTextColor(Color.DKGRAY)
        }

        holder.favoriteIcon.visibility = if(busStopModel.isFavorite)  View.VISIBLE else View.GONE

        if(busStopAdapterListener != null)
        holder.root.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(p0: View?): Boolean {
                busStopAdapterListener!!.longPressedRoute(busStopModel)
                return true
            }
        })

        //Fill the stop time and the next stop time textViews
        if(busStopModel.stopTimes.size > 0) {
            var nextStopTime = false

            var highlightBackground = false

            for(stopTime in busStopModel.stopTimes) {
                //Skip the stop times that have already passed
                if(stopTime.isAfterNow) {
                    if(!nextStopTime) {
                        val withinTenMinutes = isWithinTenMinutes(stopTime)
                        holder.stopTimeTextView.typeface = if(withinTenMinutes) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

                        if(withinTenMinutes) {
                            highlightBackground = near //Only highlight background if also the stop is near
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
            val background = holder.highlightBackground
            //Highlight background, if the stop is nearby and the next stop time is within 10 minutes
            if (highlightBackground && highlightStopTime) {
                background.visibility = View.VISIBLE
                val alphaAnimation = AlphaAnimation(0.2f, 0.8f)
                alphaAnimation.duration = 1000
                alphaAnimation.repeatCount = Animation.INFINITE
                alphaAnimation.repeatMode = Animation.REVERSE

                background.startAnimation(alphaAnimation)
            } else {
                background.visibility = View.GONE
                background.clearAnimation()
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

    interface BusStopAdapterListener {
        fun longPressedRoute(busStopModel: BusStopModel)
    }
}