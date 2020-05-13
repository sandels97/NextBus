package com.santtuhyvarinen.nextbus

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.preference.PreferenceManager
import com.santtuhyvarinen.nextbus.models.BusStopModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ApiHandler(val context : Context, val apiHandlerListener: ApiHandlerListener, val progress : ProgressBar)  {

    interface ApiHandlerListener {
        fun dataReady(busModels : List<BusStopModel>)
    }
    companion object {
        const val API_URL = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql"
        const val API_TAG = "api_tag"

        //Create BusStopModels from the fetched JSONObject - JSON object has to match GraphQL query in fetchStopTimeData
        fun parseJSONResponse(jsonObject: JSONObject) : List<BusStopModel> {
            val list : ArrayList<BusStopModel> = ArrayList()
            try {

                val edges = jsonObject.getJSONObject("data").getJSONObject("stopsByRadius").getJSONArray("edges")

                for(x in 0 until edges.length()) {
                    val node = edges.getJSONObject(x).getJSONObject("node")
                    val stop = node.getJSONObject("stop")
                    val busStopName = stop.getString("name")
                    val vehicleType = stop.getInt("vehicleType")

                    val distance = node.getInt("distance")

                    val patterns = stop.getJSONArray("stoptimesForPatterns")
                    for(y in 0 until patterns.length()) {
                        val pattern = patterns.getJSONObject(y).getJSONObject("pattern")
                        val routeName = pattern.getJSONObject("route").getString("shortName")

                        val tripHeadSign = pattern.getString("headsign")

                        val stopTimesArray = patterns.getJSONObject(y).getJSONArray("stoptimes")
                        val stopTimes = ArrayList<DateTime>()
                        for(z in 0 until stopTimesArray.length()) {

                            val stoptime = stopTimesArray.getJSONObject(z)
                            val seconds = stoptime.getInt("scheduledDeparture") //Stop time is SecondsFromMidnight
                            val date = stoptime.getLong("serviceDay") //Date is in UNIX format

                            var dateTime = DateTime(date * 1000)
                            dateTime = dateTime.plusSeconds(seconds)

                            stopTimes.add(dateTime)
                        }

                        val busStopModel =
                            BusStopModel(
                                routeName,
                                busStopName,
                                tripHeadSign,
                                vehicleType,
                                distance,
                                stopTimes
                            )

                        list.add(busStopModel)
                    }
                }

            } catch (e : JSONException) {
                e.printStackTrace()
            } catch (e : Exception) {
                e.printStackTrace()
            }

            return list
        }
    }

    fun fetch(latitude : Float, longitude : Float, radius : Int){

        progress.visibility = View.VISIBLE
        GlobalScope.launch(Dispatchers.Main) {

            //How many stops will be shown from the result
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val searchAmount = sharedPreferences.getInt("search_amount_key", 10)

            val result = fetchStopTimeData(latitude, longitude, radius, searchAmount)
            if (result != null) {
                Log.d(API_TAG, result.toString())

                val list = parseJSONResponse(result)
                apiHandlerListener.dataReady(list)
            } else {
                Log.d(API_TAG, "Result was null")
            }
            progress.visibility = View.GONE
        }
    }

    private suspend fun fetchStopTimeData(latitude : Float, longitude : Float, radius : Int, searchAmount : Int) : JSONObject? = withContext(Dispatchers.IO) {

        val inputStream : InputStream

        var result : JSONObject? = null
        val url = URL(API_URL)

        try {

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/graphql");
            conn.connectTimeout = 8000;
            conn.readTimeout = 8000;
            conn.doOutput = true
            conn.doInput = true

            conn.connect()

            //GraphQL query
            //val data = "{ stopsByRadius(lat: $latitude, lon: $longitude, radius: $radius, first: 20) {edges{node{stop{id name stoptimesForServiceDate(date : \"$dateValue\"){stoptimes{trip{routeShortName tripHeadsign}}}vehicleType}distance}}}}"
            val data = "{ stopsByRadius(lat: $latitude, lon: $longitude, radius: $radius, first: $searchAmount) {edges{node{stop{id name vehicleType stoptimesForPatterns{pattern{ headsign route{shortName}}stoptimes{scheduledDeparture serviceDay}}}distance}}}}"

            Log.d(API_TAG, data)

            val wr = OutputStreamWriter(conn.outputStream)
            wr.write(data)
            wr.flush()

            inputStream = conn.inputStream

            val streamReader =
                BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val responseStrBuilder = StringBuilder()

            while (true) {
                val line = streamReader.readLine()
                if(line == null) break
                responseStrBuilder.append(line)
            }

            inputStream.close()
            //returns the json object
            result = JSONObject(responseStrBuilder.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        result
    }

}