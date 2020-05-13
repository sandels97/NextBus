package com.santtuhyvarinen.nextbus.fragments

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.santtuhyvarinen.nextbus.handlers.ApiHandler
import com.santtuhyvarinen.nextbus.BusStopAdapter
import com.santtuhyvarinen.nextbus.handlers.LocationHandler
import com.santtuhyvarinen.nextbus.R
import com.santtuhyvarinen.nextbus.database.FavoritesDatabase
import com.santtuhyvarinen.nextbus.database.FavoritesDatabase.Companion.DATABASE_TAG
import com.santtuhyvarinen.nextbus.models.BusStopModel
import com.santtuhyvarinen.nextbus.models.FavoriteModel
import kotlinx.coroutines.*
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


//Fragment for showing the list of close by bus stops
class HomeFragment : Fragment() {

    lateinit var locationHandler : LocationHandler

    //Used to update the stop times every minute
    lateinit var tickReceiver: BroadcastReceiver
    lateinit var tickFilter : IntentFilter

    lateinit var busStopAdapter: BusStopAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        //Initialize the RecyclerView
        val linearLayoutManager = LinearLayoutManager(context)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        busStopAdapter = BusStopAdapter(context!!, ArrayList())

        //Insert favorite or delete favorite from database when stop long clicked
        busStopAdapter.busStopAdapterListener = object : BusStopAdapter.BusStopAdapterListener {
            override fun longPressedRoute(busStopModel: BusStopModel) {
                updateFavorite(busStopModel)
            }
        }

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = busStopAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val locationOffIcon = root.findViewById<View>(R.id.locationOffIcon)
        val locationOffText = root.findViewById<View>(R.id.locationOffText)

        val progress = root.findViewById<ProgressBar>(R.id.progress)

        val apiHandler = ApiHandler(
            context!!,
            object :
                ApiHandler.ApiHandlerListener {
                override fun dataReady(busModels: List<BusStopModel>) {
                    updateBusModels(busModels)
                }
            },
            progress
        )

        locationHandler = LocationHandler(
            context!!,
            object :
                LocationHandler.LocationHandlerListener {

                //Show the recyclerView and fetch stop times
                override fun locationUpdated() {
                    recyclerView.visibility = View.VISIBLE
                    locationOffIcon.visibility = View.GONE
                    locationOffText.visibility = View.GONE
                    val location = locationHandler.location

                    //Get the query radius from preferences
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    val radius = sharedPreferences.getInt("search_radius_key", 1000)
                    if (location != null) {
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

    //Populate
    fun updateBusModels(busStopModels : List<BusStopModel>) = CoroutineScope(Dispatchers.Main).launch {

        val task = async (Dispatchers.IO) {
            matchBusStopModelsWithFavorites(busStopModels)
        }
        task.await()
        busStopAdapter.busStopModels = busStopModels
        busStopAdapter.notifyDataSetChanged()
    }

    fun updateFavorite(busStopModel: BusStopModel) = CoroutineScope(Dispatchers.Main).launch {
        val database = FavoritesDatabase.getInstance(context!!)
        if(database != null) {
            val task = async(Dispatchers.IO) {
                val favorites = database.favoritesDao().getAll()
                if (busStopModel.isFavorite) {
                    //Find the matching favorite model
                    var favoriteModel: FavoriteModel? = null
                    for (favorite in favorites) {
                        if (favorite.route.equals(busStopModel.route)) {
                            favoriteModel = favorite
                            break
                        }
                    }
                    if (favoriteModel != null) {
                        database.favoritesDao().delete(favoriteModel)
                        Log.d(DATABASE_TAG, "Deleted favorite: " + busStopModel.route)
                    }

                } else {
                    val favoriteModel = FavoriteModel(busStopModel.route)
                    database.favoritesDao().insert(favoriteModel)
                    Log.d(DATABASE_TAG, "Inserted new favorite: " + busStopModel.route)
                }

                matchBusStopModelsWithFavorites(busStopAdapter.busStopModels)
            }

            task.await()
            busStopAdapter.notifyDataSetChanged()
        }
    }

    private fun matchBusStopModelsWithFavorites(busModels: List<BusStopModel>) {
        val database = FavoritesDatabase.getInstance(context!!)
        if(database != null) {
            val favorites = database.favoritesDao().getAll()
            //Match the favorite with
            for (busModel in busModels) {
                busModel.isFavorite = false
                for (favorite in favorites) {
                    if (busModel.route == favorite.route) {
                        busModel.isFavorite = true
                        break
                    }
                }
            }

            //Sort BusStopModels first based on if they are favorite and then based on the distance
            Collections.sort(busModels, object : Comparator<BusStopModel> {
                override fun compare(stop1: BusStopModel, stop2: BusStopModel): Int {

                    val value = stop2.isFavorite.compareTo(stop1.isFavorite)
                    if(value != 0) return value
                    return stop1.distance.compareTo(stop2.distance)
                }
            })

        }
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
