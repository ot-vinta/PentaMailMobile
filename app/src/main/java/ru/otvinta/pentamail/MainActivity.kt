package ru.otvinta.pentamail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private var email: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        email = intent.extras!!.getString("email")
        val args = Bundle()
        args.putString("folder", "Новые")
        args.putString("email", email)

        val fragment = ReceivedMessagesFragment()
        fragment.arguments = args
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment, "RMF")
        transaction.commit()

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener {menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            drawerLayout.closeDrawers()
            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here
            when(menuItem.itemId) {
                R.id.exit -> {
                    val intent = Intent(this, EnterActivity::class.java)
                    startActivity(intent)
                }
                R.id.newMessages -> {
                    val receivedMessagesFragment = ReceivedMessagesFragment()
                    setNewFragment(receivedMessagesFragment, "Новые")
                }
                R.id.spam -> {
                    val receivedMessagesFragment = ReceivedMessagesFragment()
                    setNewFragment(receivedMessagesFragment, "Спам")
                }
                R.id.watched -> {
                    val receivedMessagesFragment = ReceivedMessagesFragment()
                    setNewFragment(receivedMessagesFragment, "Прочитанные")
                }
                R.id.sent -> {
                    val receivedMessagesFragment = ReceivedMessagesFragment()
                    setNewFragment(receivedMessagesFragment, "Отправленные")
                }
                R.id.newFolder -> {

                }
                R.id.writeMessage -> {
                    val newMessageFragment = NewMessageFragment()
                    setNewFragment(newMessageFragment, "")
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

    private fun setNewFragment(fragment: Fragment, folder: String) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.addToBackStack(null)
        transaction.commit()
        if (folder != "") {
            title = folder + " сообщения"
            val args = Bundle()
            args.putString("folder", folder)
            args.putString("email", email)
            fragment.arguments = args
            transaction.replace(R.id.container, fragment, "RMF")
        }
        else {
            title = "Написать"
            val args = Bundle()
            args.putString("email", email)
            fragment.arguments = args
            transaction.replace(R.id.container, fragment, "NMF")
        }
    }
}
