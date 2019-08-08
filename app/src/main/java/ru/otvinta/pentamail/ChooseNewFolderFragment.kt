package ru.otvinta.pentamail

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import androidx.recyclerview.widget.DividerItemDecoration
import android.widget.AdapterView.OnItemClickListener





class ChooseNewFolderFragment : DialogFragment(), OnAsyncTaskListener {

    private var folders = ArrayList<Folder>()
    private var email: String? = null
    private var messageId: String? = null
    private lateinit var recyclerView : RecyclerView
    private lateinit var dataAdapter : FolderDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.setTitle("Выберите папку")
        val v = inflater.inflate(R.layout.fragment_choose_new_folder, null)

        if (arguments != null) {
            val args = arguments
            email = args!!.getString("email")
            messageId = args.getString("messageId")
        }

        getFolders(email)

        return v
    }

    fun getFolders(email : String?){
        folders.clear()
        val getFoldersTask = GetFolderTask(this)
        getFoldersTask.execute("GetFolders", email)
    }

    override fun onAsyncTaskFinished(v: ArrayList<String>) {
        var foldersList = ArrayList<String>()
        try {
            foldersList = v[0].split("<%$%>") as ArrayList<String>
        }
        catch (e: IndexOutOfBoundsException){
            e.printStackTrace()
        }

        for (folder in foldersList)
            if ((folder != "/$/") && (folder != "")){
                val mailContent = folder.split("/$/")
                val id = mailContent[0].toInt()
                val title = mailContent[1]
                folders.add(Folder(id, title))
            }

        recyclerView = view!!.findViewById(R.id.FolderList)
        recyclerView.layoutManager = LinearLayoutManager(view!!.context)
        dataAdapter = FolderDataAdapter(view!!.context, folders)
        dataAdapter.folders = folders
        recyclerView.adapter = dataAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL))
        recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(context!!, recyclerView, object : RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    val mover = MessageMover(messageId!!.toInt(),view.findViewById<TextView>(R.id.folderId).text.toString().toInt())
                    mover.move()
                    val account = email
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.putExtra("email", account)
                    startActivity(intent)
                }

                override fun onLongItemClick(view: View, position: Int) {
                    // do whatever
                }
            })
        )
    }

    class GetFolderTask(val fragment: ChooseNewFolderFragment) : AsyncTask<String, Int, ArrayList<String>>() {

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
                    .appendQueryParameter("email", params[1])

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
