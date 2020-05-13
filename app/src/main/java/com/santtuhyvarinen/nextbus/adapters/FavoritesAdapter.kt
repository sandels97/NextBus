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
import com.santtuhyvarinen.nextbus.models.FavoriteModel
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

//RecyclerView adapter that manages bus stop item views
class FavoritesAdapter(private val context : Context, var favoriteModels : List<FavoriteModel>) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>(){

    var favoritesAdapterListener: FavoritesAdapterListener? = null

    class ViewHolder(val root : View) : RecyclerView.ViewHolder(root) {

        val routeTextView : TextView
        val deleteButton : ImageView
        init {
            deleteButton = root.findViewById(R.id.deleteButton)
            routeTextView = root.findViewById(R.id.routeText)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(
            view
        )
    }

    override fun getItemCount(): Int {
        return favoriteModels.size
    }

    //Set the bus stop info
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favoriteModel = favoriteModels[position]
        holder.routeTextView.text = favoriteModel.route

        holder.deleteButton.setOnClickListener {
            if(favoritesAdapterListener != null) favoritesAdapterListener!!.delete(favoriteModel)
        }
    }


    interface FavoritesAdapterListener {
        fun delete(favoriteModel: FavoriteModel)
    }
}