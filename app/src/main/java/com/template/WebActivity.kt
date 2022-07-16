package com.template

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf


class WebActivity : AppCompatActivity() {

    lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        webView = findViewById<WebView>(R.id.webView)
        var urlText = intent.getStringExtra(LoadingActivity.URL_KEY)


        if(savedInstanceState != null){
            webView.restoreState(savedInstanceState);
        }else {
            webView.loadUrl(urlText!!);
        }


        webView.apply {
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true


            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    CookieSyncManager.getInstance().sync()
                    super.onPageFinished(view, url)
                }
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return true
                }
            }
        }


        CookieSyncManager.createInstance(this)
        CookieSyncManager.getInstance().startSync()
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
    }

    override fun onBackPressed() {
        if(this.webView.canGoBack()){
            webView.goBack();
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    companion object{

        fun intentWebActivity(
            activity: Activity,
            url: String) = Intent(activity, WebActivity::class.java).apply {
                putExtra(LoadingActivity.URL_KEY, url)
            }
    }


}