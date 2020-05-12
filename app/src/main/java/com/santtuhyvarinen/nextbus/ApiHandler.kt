package com.santtuhyvarinen.nextbus

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class ApiHandler  {
    companion object {
        const val API_URL = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql"
        const val API_TAG = "api_tag"
    }

    fun fetch(){
        GlobalScope.launch(Dispatchers.Main) {
            val result = fetchStopTimeData()
            if (result != null) {
                Log.d(API_TAG, result.toString())
            } else {
                Log.d(API_TAG, "Result was null")
            }
        }
    }

    private suspend fun fetchStopTimeData() : JSONObject? = withContext(Dispatchers.IO) {
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

            val data = "{\n" +
                    "  stopsByRadius(lat: 60.19924, lon: 24.94112, radius: 1000, first: 10) {\n" +
                    "    edges {\n" +
                    "      node {\n" +
                    "        stop {\n" +
                    "          name\n" +
                    "          lat\n" +
                    "          lon\n" +
                    "        }\n" +
                    "        distance\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}"

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