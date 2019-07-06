package ru.otvinta.pentamail

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import org.w3c.dom.Text
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

class EnterActivity : AppCompatActivity() {

    var result = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)

        try {
            val error = intent.extras!!.getBoolean("error")
            if (error) {
                val errorText = findViewById<TextView>(R.id.error_text)
                errorText.text = getString(R.string.enter_error)
            }
        }
        catch (e : NullPointerException){
            val errorText = findViewById<TextView>(R.id.error_text)
            errorText.text = ""
        }

        val email = findViewById<TextInputEditText>(R.id.EmailField)
        val password = findViewById<TextInputEditText>(R.id.PasswordField)

        val registerButton = findViewById<Button>(R.id.RegisterButton)
        registerButton.setOnClickListener {
            val intent =  Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val enterButton = findViewById<Button>(R.id.EnterButton)
        enterButton.setOnClickListener {
            val enter = EnterTask()
            enter.execute("Enter", email.text.toString(), password.text.toString())
            result = enter.get()
            if (result == "true") {
                val account = email.text.toString()
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("email", account)
                startActivity(intent)
            }
            else {
                val intent = Intent(this, EnterActivity::class.java)
                intent.putExtra("error", true)
                startActivity(intent)
            }
        }
    }

    class EnterTask() : AsyncTask<String, String, String>() {

        private lateinit var connection : HttpURLConnection

        override fun doInBackground(vararg params: String?): String {

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
                    .appendQueryParameter("password", params[2])

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

                    return reader.readLine()
                }
                else {
                    return "0"
                }
            }
            catch (e : Exception){
                e.printStackTrace()
                return "exception"
            }
            finally {
                connection.disconnect()
            }
        }
    }
}
