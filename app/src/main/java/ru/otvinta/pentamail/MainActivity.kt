package ru.otvinta.pentamail

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity(), onAsyncTaskListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var email: String? = ""
    private var folders = ArrayList<Folder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        email = intent.extras!!.getString("email")
        val args = Bundle()
        args.putString("folder", "Новые")
        args.putString("email", email)

        //Default fragment
        val fragment = ReceivedMessagesFragment()
        fragment.arguments = args
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment, "RMF")
        transaction.commit()

        val findFoldersTask = FindFoldersTask(this)
        findFoldersTask.execute("GetFolders", email)

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener {menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            drawerLayout.closeDrawers()
            when(menuItem.itemId) {
                R.id.exit -> {
                    val intent = Intent(this, EnterActivity::class.java)
                    startActivity(intent)
                }
                R.id.newMessages -> {
                    val receivedMessagesFragment = ReceivedMessagesFragment()
                    setNewFragment(receivedMessagesFragment, "Новые", "Новые сообщения")
                }
                R.id.spam -> {
                    val receivedMessagesFragment = ReceivedMessagesFragment()
                    setNewFragment(receivedMessagesFragment, "Спам", "Спам")
                }
                R.id.watched -> {
                    val receivedMessagesFragment = ReceivedMessagesFragment()
                    setNewFragment(receivedMessagesFragment, "Прочитанные", "Прочитанные сообщения")
                }
                R.id.sent -> {
                    val receivedMessagesFragment = ReceivedMessagesFragment()
                    setNewFragment(receivedMessagesFragment, "Отправленные", "Отправленные сообщения")
                }
                R.id.newFolder -> {
                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.addToBackStack(null)
                    transaction.commit()
                    val newFolderFragment = NewFolderFragment()
                    toolbar.title = "Добавить папку"
                    val args = Bundle()
                    args.putString("email", email)
                    newFolderFragment.arguments = args
                    transaction.replace(R.id.container, newFolderFragment, "NFF")
                }
                R.id.writeMessage -> {
                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.addToBackStack(null)
                    transaction.commit()
                    val newMessageFragment = NewMessageFragment()
                    toolbar.title = "Написать"
                    val args = Bundle()
                    args.putString("email", email)
                    newMessageFragment.arguments = args
                    transaction.replace(R.id.container, newMessageFragment, "NMF")
                }
                else -> {
                    val receivedMessagesFragment = ReceivedMessagesFragment()
                    setNewFragment(receivedMessagesFragment, menuItem.title.toString(), "Папка" + menuItem.title.toString())
                }
            }

            true
        }

        //!!!!!!!!!!!!!
        //-------------
        //TRY TO USE IT
        drawerLayout.addDrawerListener(
            object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    // Respond when the drawer's position changes
                }

                override fun onDrawerOpened(drawerView: View) {
                    // Respond when the drawer is opened
                }

                override fun onDrawerClosed(drawerView: View) {
                    // Respond when the drawer is closed
                }

                override fun onDrawerStateChanged(newState: Int) {
                    // Respond when the drawer motion state changes
                }
            }
        )

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //-------------------
    //Makes Folder's list
    override fun onAsyncTaskFinished(v: ArrayList<String>) {
        var foldersList = ArrayList<String>()
        try {
            foldersList = v[0].split("<%$%>") as ArrayList<String>
        }
        catch (e: IndexOutOfBoundsException){
            e.printStackTrace()
        }

        var order = 0

        for (folder in foldersList)
            if ((folder != "/$/") && (folder != "")){
                val folderContent = folder.split("/$/")
                val id = folderContent[0].toInt()
                val title = folderContent[1]
                if ((title != "Новые") && (title != "Спам") && (title != "Отправленные") && (title != "Прочитанные")) {
                    folders.add(Folder(id, title))
                    navigationView.menu.add(R.id.folders, Menu.NONE, 1, title)
                    order++
                }
            }
    }

    //------------------------------------------------
    //Connects to a server and makes a list of folder's names
    class FindFoldersTask(val activity: MainActivity) : AsyncTask<String, Int, ArrayList<String>>() {

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
            activity.onAsyncTaskFinished(result)
        }
    }

    //------------------------------------------
    //Sets a new fragment. It's only for folders
    private fun setNewFragment(fragment: Fragment, folder: String, title: String) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.addToBackStack(null)
        transaction.commit()
        if (folder != "") {
            toolbar.title = title
            val args = Bundle()
            args.putString("folder", folder)
            args.putString("email", email)
            fragment.arguments = args
            transaction.replace(R.id.container, fragment, "RMF")
        }
    }

}
