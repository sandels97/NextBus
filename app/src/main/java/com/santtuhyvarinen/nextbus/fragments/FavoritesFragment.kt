package com.santtuhyvarinen.nextbus.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.santtuhyvarinen.nextbus.R
import com.santtuhyvarinen.nextbus.adapters.FavoritesAdapter
import com.santtuhyvarinen.nextbus.database.FavoritesDatabase
import com.santtuhyvarinen.nextbus.models.FavoriteModel
import kotlinx.coroutines.*
import java.lang.Exception

class FavoritesFragment : Fragment() {

    lateinit var favoritesAdapter :FavoritesAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_favorites, container, false)

        val linearLayoutManager = LinearLayoutManager(context)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)

        favoritesAdapter = FavoritesAdapter(context!!, ArrayList())
        favoritesAdapter.favoritesAdapterListener = object : FavoritesAdapter.FavoritesAdapterListener {
            override fun delete(favoriteModel: FavoriteModel) {
                deleteFavorite(favoriteModel)
            }
        }

        //Add new favorite
        val addEditText : EditText = root.findViewById(R.id.addFavoriteEditText)
        val addButton : ImageView = root.findViewById(R.id.addFavoriteButton)
        addButton.setOnClickListener {
            if(!addEditText.text.isNullOrEmpty()) {
                addFavorite(addEditText.text.toString())
                addEditText.setText("")
            }
        }

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = favoritesAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        update()

        return root
    }

    fun addFavorite(string : String) = CoroutineScope(Dispatchers.Main).launch {
        val favoriteModel = FavoriteModel(string)
        val task = async(Dispatchers.IO){
            val database = FavoritesDatabase.getInstance(context!!)
            database?.favoritesDao()?.insert(favoriteModel)
        }
        task.await()
        Toast.makeText(context!!, getString(R.string.added_to_favorites, favoriteModel.route), Toast.LENGTH_SHORT).show()
        update()
    }

    fun deleteFavorite(favoriteModel: FavoriteModel) = CoroutineScope(Dispatchers.Main).launch {
        val task = async(Dispatchers.IO){
            val database = FavoritesDatabase.getInstance(context!!)
            database?.favoritesDao()?.delete(favoriteModel)
        }
        task.await()
        Toast.makeText(context!!, getString(R.string.remove_from_favorites, favoriteModel.route), Toast.LENGTH_SHORT).show()
        update()
    }

    fun update() = CoroutineScope(Dispatchers.Main).launch {
        val task = async(Dispatchers.IO){
            val database = FavoritesDatabase.getInstance(context!!)
            if(database != null) {
                val favorites = database.favoritesDao().getAll()

                favoritesAdapter.favoriteModels = favorites
            }
        }

        task.await()
        favoritesAdapter.notifyDataSetChanged()
    }
}
