package ru.otvinta.pentamail

import android.content.Context
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

class NewFolderFragment : Fragment(), View.OnClickListener {

    var email : String? = ""

    lateinit var title: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_new_folder, container, false)

        if (arguments != null) {
            email = arguments!!.getString("email")
        }

        title = v.findViewById(R.id.folderNameField)
        val submit = v.findViewById<ImageButton>(R.id.addFolderButton)

        submit.setOnClickListener(this)

        return v
    }

    override fun onClick(v: View?) {
        val task = AddNewFolderTask(this)
        task.execute("AddFolder", title.text.toString(), email)
    }


    fun onAsyncTaskFinished(v: ArrayList<String>) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
    }


    class AddNewFolderTask(val fragment: NewFolderFragment) : AsyncTask<String, Int, ArrayList<String>>() {

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
                    .appendQueryParameter("title", params[1])
                    .appendQueryParameter("email", params[2])

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
