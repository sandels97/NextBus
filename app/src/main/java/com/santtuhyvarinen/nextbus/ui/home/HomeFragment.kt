package com.santtuhyvarinen.nextbus.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.santtuhyvarinen.nextbus.BusStopAdapter
import com.santtuhyvarinen.nextbus.R
import com.santtuhyvarinen.nextbus.models.BusStopModel

//Fragment for showing the list of close by bus stops
class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        //Test data
        val busStopModels = ArrayList<BusStopModel>()
        for(x in 0 until 4) {
            busStopModels.add(BusStopModel(12, "543", "Helsinki", 1.5f, 1.45f))
            busStopModels.add(BusStopModel(11, "434", "Vantaa", 1.5f, 1.45f))
            busStopModels.add(BusStopModel(10, "431", "Kivistö", 1.5f, 1.45f))
        }

        //Initialize the RecyclerView
        val linearLayoutManager = LinearLayoutManager(context)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        val busStopAdapter = BusStopAdapter(context!!, busStopModels)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = busStopAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        return root
    }
}
