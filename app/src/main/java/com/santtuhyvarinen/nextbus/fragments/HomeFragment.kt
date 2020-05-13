package com.santtuhyvarinen.nextbus.fragments

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.santtuhyvarinen.nextbus.ApiHandler
import com.santtuhyvarinen.nextbus.BusStopAdapter
import com.santtuhyvarinen.nextbus.LocationHandler
import com.santtuhyvarinen.nextbus.R
import com.santtuhyvarinen.nextbus.models.BusStopModel


//Fragment for showing the list of close by bus stops
class HomeFragment : Fragment() {

    lateinit var locationHandler : LocationHandler

    //Used to update the stop times every minute
    lateinit var tickReceiver: BroadcastReceiver
    lateinit var tickFilter : IntentFilter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val busStopModels = ArrayList<BusStopModel>()

        //Test data
        /*for(x in 0 until 4) {
            busStopModels.add(BusStopModel("543", "Fakekatu","Helsinki", BusStopModel.ROUTE_TYPE_BUS,1))
            busStopModels.add(BusStopModel( "434", "Kuninkaantie","Vantaa", BusStopModel.ROUTE_TYPE_BUS,1))
            busStopModels.add(BusStopModel( "431", "Vesikuja","Kivistö", BusStopModel.ROUTE_TYPE_RAIL,1))
        }*/

        //Initialize the RecyclerView
        val linearLayoutManager = LinearLayoutManager(context)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        val busStopAdapter = BusStopAdapter(context!!, busStopModels)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = busStopAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val locationOffIcon = root.findViewById<View>(R.id.locationOffIcon)
        val locationOffText = root.findViewById<View>(R.id.locationOffText)

        val progress = root.findViewById<ProgressBar>(R.id.progress)

        val apiHandler = ApiHandler(context!!, object : ApiHandler.ApiHandlerListener {
            override fun dataReady(busModels: List<BusStopModel>) {
                busStopAdapter.busStopModels = busModels
                busStopAdapter.notifyDataSetChanged()
            }
        }, progress)


        locationHandler = LocationHandler(context!!, object : LocationHandler.LocationHandlerListener {

            //Show the recyclerView
            override fun locationUpdated() {
                recyclerView.visibility = View.VISIBLE
                locationOffIcon.visibility = View.GONE
                locationOffText.visibility = View.GONE
                val location = locationHandler.location

                //Get the query radius from preferences
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val radius = sharedPreferences.getInt("search_radius_key", 1000)
                if(location != null) {
                    //Start fetching data from HSL api
                    apiHandler.fetch(location.x, location.y, radius)
                }
            }

            //Hide recyclerview and instead show Location Off
            override fun locationOff() {
                recyclerView.visibility = View.INVISIBLE
                locationOffIcon.visibility = View.VISIBLE
                locationOffText.visibility = View.VISIBLE
            }
        })

        tickReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                busStopAdapter.notifyDataSetChanged()
            }
        }

        tickFilter = IntentFilter()
        tickFilter.addAction("android.intent.action.TIME_TICK")
        context!!.registerReceiver(tickReceiver, tickFilter)

        return root
    }

    override fun onPause() {
        super.onPause()
        context!!.unregisterReceiver(tickReceiver)
    }

    override fun onResume() {
        super.onResume()
        context!!.registerReceiver(tickReceiver, tickFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
