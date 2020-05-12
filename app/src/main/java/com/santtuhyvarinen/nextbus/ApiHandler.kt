package com.santtuhyvarinen.nextbus

import android.util.Log
import com.santtuhyvarinen.nextbus.models.BusStopModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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


class ApiHandler(val apiHandlerListener: ApiHandlerListener)  {

    interface ApiHandlerListener {
        fun dataReady(busModels : List<BusStopModel>)
    }
    companion object {
        const val API_URL = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql"
        const val API_TAG = "api_tag"

        fun secondsToTime(seconds : Int) {
            val minutes = Math.floor((seconds % 3600)/60.0)
            val hours = Math.floor(seconds/3600.0)

            Log.d(API_TAG, "$hours : $minutes")
        }

        //Create BusStopModels from the fetched JSONObject - JSON object has to match GraphQL query in fetchStopTimeData
        fun parseJSONResponse(jsonObject: JSONObject) : List<BusStopModel> {
            val list : ArrayList<BusStopModel> = ArrayList()
            try {

                val edges = jsonObject.getJSONObject("data").getJSONObject("stopsByRadius").getJSONArray("edges")

                for(x in 0 until edges.length()) {
                    val node = edges.getJSONObject(x).getJSONObject("node")
                    val stop = node.getJSONObject("stop")
                    val busStopName = stop.getString("name")
                    val id = stop.getString("id")
                    val vehicleType = stop.getInt("vehicleType")

                    val distance = node.getInt("distance")

                    val patterns = stop.getJSONArray("stoptimesForPatterns")
                    for(y in 0 until patterns.length()) {
                        val pattern = patterns.getJSONObject(y).getJSONObject("pattern")
                        val routeName = pattern.getJSONObject("route").getString("shortName")

                        val tripHeadSign = pattern.getString("headsign")

                        val stopTimesArray = patterns.getJSONObject(y).getJSONArray("stoptimes")
                        val stopTimes = IntArray(stopTimesArray.length())
                        for(z in 0 until stopTimesArray.length()) {
                            stopTimes[z] = stopTimesArray.getJSONObject(z).getInt("scheduledDeparture")
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
        GlobalScope.launch(Dispatchers.Main) {
            val date = Date()
            val result = fetchStopTimeData(date, latitude, longitude, radius)
            if (result != null) {
                Log.d(API_TAG, result.toString())

                val list = parseJSONResponse(result)
                apiHandlerListener.dataReady(list)
            } else {
                Log.d(API_TAG, "Result was null")
            }
        }
    }

    private suspend fun fetchStopTimeData(date : Date, latitude : Float, longitude : Float, radius : Int) : JSONObject? = withContext(Dispatchers.IO) {
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

            val dateFormat: DateFormat = SimpleDateFormat("yyyyMMdd")
            val dateValue = dateFormat.format(date)

            //GraphQL query
            //val data = "{ stopsByRadius(lat: $latitude, lon: $longitude, radius: $radius, first: 20) {edges{node{stop{id name stoptimesForServiceDate(date : \"$dateValue\"){stoptimes{trip{routeShortName tripHeadsign}}}vehicleType}distance}}}}"
            val data = "{ stopsByRadius(lat: $latitude, lon: $longitude, radius: $radius, first: 20) {edges{node{stop{id name vehicleType stoptimesForPatterns{pattern{ headsign route{shortName}}stoptimes{scheduledDeparture}}}distance}}}}"

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