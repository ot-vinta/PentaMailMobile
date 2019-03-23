package ru.otvinta.pentamail

import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ReceivedMessagesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ReceivedMessagesFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ReceivedMessagesFragment : Fragment(), onAsyncTaskListener {

    private var messages = ArrayList<Message>()
    private var email: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_received_messages, container, false)

        if (activity != null) {
            val args = activity!!.intent.extras
            if (args != null) {
                email = args.getString("email")
            }
        }

        val messagesTask = MessagesTask(this)
        messagesTask.execute("GetMessages", email, "Новые")

        return v
    }

    override fun onAsyncTaskFinished(v: ArrayList<String>) {
        for (mail in v) {
            val mailContent = mail.split("/$/")
            val sender = mailContent[0]
            val date = mailContent[1]
            val title = mailContent[2]
            val content = mailContent[3]
            messages.add(Message(sender, "Отправлено: " + date, title, content))
        }


        val recyclerView = view!!.findViewById<RecyclerView>(R.id.MessageList)
        recyclerView.layoutManager = LinearLayoutManager(view!!.context)
        val dataAdapter = DataAdapter(view!!.context, messages)
        recyclerView.adapter = dataAdapter
    }


    class MessagesTask(val fragment: ReceivedMessagesFragment) : AsyncTask<String, Int, ArrayList<String>>() {

        private lateinit var connection : HttpURLConnection
        private lateinit var result : ArrayList<String>

        override fun doInBackground(vararg params: String?): ArrayList<String> {

            try {
                val url = URL("http://pentamail/server/ServerAndroid.php")

                connection = url.openConnection() as HttpURLConnection
                connection.readTimeout = 15000
                connection.connectTimeout = 10000
                connection.requestMethod = "POST"

                connection.doOutput = true
                connection.doInput = true

                val builder = Uri.Builder().appendQueryParameter("method", params[0])
                    .appendQueryParameter("email", params[1])
                    .appendQueryParameter("folder", params[2])

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
