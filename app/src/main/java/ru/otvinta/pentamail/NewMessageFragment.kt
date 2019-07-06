package ru.otvinta.pentamail

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class NewMessageFragment : Fragment(), onAsyncTaskListener, View.OnClickListener {

    var sender: String? = ""

    lateinit var receiver: EditText
    lateinit var title: EditText
    lateinit var content: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_new_message, container, false)

        if (arguments != null) {
            val args = arguments
            sender = args!!.getString("email")
        }

        receiver = v.findViewById(R.id.emailField)
        title = v.findViewById(R.id.titleField)
        content = v.findViewById(R.id.contentField)
        val submit = v.findViewById<ImageButton>(R.id.sendMessageButton)

        submit.setOnClickListener (this)

        return v
    }

    override fun onClick(v: View?) {
        val sendTask = NewMessageFragment.SendTask(this)
        sendTask.execute("SendMessage",
            title.text.toString(),
            content.text.toString(),
            sender,
            receiver.text.toString())
    }

    override fun onAsyncTaskFinished(v: ArrayList<String>) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("email", sender)
        startActivity(intent)
    }


    class SendTask(val fragment: NewMessageFragment) : AsyncTask<String, Int, ArrayList<String>>() {

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
                    .appendQueryParameter("title", params[1])
                    .appendQueryParameter("content", params[2])
                    .appendQueryParameter("sender", params[3])
                    .appendQueryParameter("receiver", params[4])

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
