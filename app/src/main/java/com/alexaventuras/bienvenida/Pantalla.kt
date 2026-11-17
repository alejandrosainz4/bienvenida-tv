package com.alexaventuras.bienvenida

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * WebView a pantalla completa que muestra la bienvenida.
 * Si se cae el internet, muestra la última versión guardada y reintenta cada minuto.
 */
@SuppressLint("SetJavaScriptEnabled")
class Pantalla(context: Context) : WebView(context) {
    private val handler = Handler(Looper.getMainLooper())
    private var url: String = Ajustes.DEMO_URL
    private var sinConexion = false

    private val reintento = Runnable { cargar(url) }
    private val refresco = object : Runnable {
        override fun run() {
            // Recarga cada 10 min para que tome cambios (nuevo huésped, avisos).
            if (!sinConexion) cargar(url)
            handler.postDelayed(this, 10 * 60 * 1000L)
        }
    }

    init {
        setBackgroundColor(Color.BLACK)
        isFocusable = false
        isFocusableInTouchMode = false
        overScrollMode = View.OVER_SCROLL_NEVER
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView, req: WebResourceRequest, err: WebResourceError) {
                if (!req.isForMainFrame) return
                if (!sinConexion) {
                    // Primer fallo: mostrar lo guardado en caché.
                    sinConexion = true
                    settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                    view.loadUrl(url)
                } else if (!url.startsWith("file:")) {
                    // Sin caché tampoco: bienvenida genérica para no dejar un error en la tele.
                    view.loadUrl(Ajustes.GENERICA_URL)
                }
                handler.removeCallbacks(reintento)
                handler.postDelayed(reintento, 60_000L)
            }
        }
    }

    fun cargar(nueva: String) {
        url = nueva
        sinConexion = !hayInternet()
        settings.cacheMode = if (sinConexion) WebSettings.LOAD_CACHE_ELSE_NETWORK else WebSettings.LOAD_DEFAULT
        loadUrl(url)
        if (sinConexion) {
            handler.removeCallbacks(reintento)
            handler.postDelayed(reintento, 60_000L)
        }
    }

    fun iniciarRefresco() {
        handler.removeCallbacks(refresco)
        handler.postDelayed(refresco, 10 * 60 * 1000L)
    }

    fun detener() {
        handler.removeCallbacksAndMessages(null)
    }

    @Suppress("DEPRECATION")
    private fun hayInternet(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }
}
