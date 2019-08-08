package ru.otvinta.pentamail

import android.net.Uri
import android.os.AsyncTask
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class MessageMover (val messageId : Int, val folderId : Int) : OnAsyncTaskListener{
    override fun onAsyncTaskFinished(v: ArrayList<String>) {

    }
    fun move(){
        val getFoldersTask = MoveMessageTask(this)
        getFoldersTask.execute("MoveMessage", messageId.toString(), folderId.toString())
    }
}

class MoveMessageTask(val mover: MessageMover) : AsyncTask<String, Int, ArrayList<String>>() {

    private lateinit var connection : HttpURLConnection
    private lateinit var result : ArrayList<String>

    override fun doInBackground(vararg params: String?): ArrayList<String> {

        try {
            val urlName = UrlName()
            val url = URL(urlName.url)

            connection = url.openConnection() as HttpURLConnection
            connection.readTimeout = 15000
            connection.connectTimeout = 10000
            connection.requestMethod = "POST"

            connection.doOutput = true
            connection.doInput = true

            val builder = Uri.Builder().appendQueryParameter("method", params[0])
                .appendQueryParameter("messageId", params[1])
                .appendQueryParameter("folderId", params[2])

            val query = builder.build().encodedQuery

            val os = connection.outputStream
            val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
            writer.write(query)
            writer.flush()
            writer.close()
            os.close()
            connection.connect()

            val response_code = connection.responseCode

            if (response_code == HttpURLConnection.HTTP_OK) {
                val input = connection.inputStream
                val reader = BufferedReader(InputStreamReader(input))
                result = ArrayList()

                var s = reader.readLine()
                while (s != null) {
                    result.add(s)
                    s = reader.readLine()
                }
            }
        }
        catch (e : Exception){
            e.printStackTrace()
        }
        finally {
            connection.disconnect()
        }

        return result
    }

    override fun onPostExecute(result: ArrayList<String>) {
        super.onPostExecute(result)
        mover.onAsyncTaskFinished(result)
    }
}