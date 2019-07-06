package ru.otvinta.pentamail

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


class FullMessageFragment : Fragment(), onAsyncTaskListener {

    private var id: String? = null
    private var sender: String? = null
    private var title: String? = null
    private var date: String? = null
    private var content: String? = null
    private var email: String? = null
    private var folders = ArrayList<Folder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_full_message, container, false)

        if (arguments != null) {
            val args = arguments
            sender = args!!.getString("sender")
            id = args.getString("id")
            title = args.getString("title")
            date = args.getString("date")
            content = args.getString("content")
            email = args.getString("email")
            folders = args.getSerializable("folders") as ArrayList<Folder>
        }

        val senderField = v.findViewById<TextView>(R.id.senderField)
        val titleField = v.findViewById<TextView>(R.id.titleField)
        val dateField = v.findViewById<TextView>(R.id.dateField)
        val contentField = v.findViewById<TextView>(R.id.contentField)

        senderField.text = sender
        titleField.text = title
        dateField.text = date
        contentField.text = content

        val deleteButton = v.findViewById<ImageButton>(R.id.deleteButton)
        val moveButton = v.findViewById<ImageButton>(R.id.moveButton)

        deleteButton.setOnClickListener {
            val messagesTask = DeleteTask(this)
            messagesTask.execute("DeleteMessage", id)
        }

        moveButton.setOnClickListener {
            
        }

        return v
    }

    override fun onAsyncTaskFinished(v: ArrayList<String>) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
    }


    class DeleteTask(val fragment: FullMessageFragment) : AsyncTask<String, Int, ArrayList<String>>() {

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
                    .appendQueryParameter("id", params[1])

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
            fragment.onAsyncTaskFinished(result)
        }
    }
}
