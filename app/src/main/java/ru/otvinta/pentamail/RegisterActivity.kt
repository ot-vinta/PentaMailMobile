package ru.otvinta.pentamail

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import java.net.HttpURLConnection
import java.net.URL
import java.io.*
import java.lang.Exception


class RegisterActivity : AppCompatActivity() {

    var result = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val penta = findViewById<TextView>(R.id.PentaMailName)
        val email = findViewById<TextInputEditText>(R.id.EmailField)
        val password = findViewById<TextInputEditText>(R.id.PasswordField)
        val backupMail = findViewById<TextInputEditText>(R.id.BackupMailField)
        val phone = findViewById<TextInputEditText>(R.id.PhoneField)
        val registerButton = findViewById<Button>(R.id.RegisterButton)

        registerButton.setOnClickListener {
            val register = RegisterTask()
            register.execute("Register", email.text.toString()+"@penta.ru", password.text.toString(), backupMail.text.toString(), phone.text.toString())
            result = register.get()
            if (result == "true") {
                val intent = Intent(this, EnterActivity::class.java)
                startActivity(intent)
            }
            else {
                penta.text = result
            }
        }
    }

    class RegisterTask : AsyncTask<String, Void, String>() {

        private lateinit var connection : HttpURLConnection

        override fun doInBackground(vararg params: String?): String {

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
                    .appendQueryParameter("password", params[2])
                    .appendQueryParameter("backupMail", params[3])
                    .appendQueryParameter("phone", params[4])

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
