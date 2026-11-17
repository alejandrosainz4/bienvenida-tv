package com.alexaventuras.bienvenida

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager

/**
 * Pantalla de bienvenida a pantalla completa.
 * - Se abre sola al encender la tele (ver WakeService).
 * - Mantiene la pantalla encendida N minutos; luego deja que la tele use su reposo normal.
 * - El huésped sale con Atrás o Inicio como de cualquier app.
 * - Para configurar: presionar ARRIBA 5 veces seguidas en el control.
 */
class MainActivity : Activity() {
    private lateinit var pantalla: Pantalla
    private val handler = Handler(Looper.getMainLooper())
    private val toquesArriba = ArrayDeque<Long>()

    private val soltarPantalla = Runnable {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pantalla = Pantalla(this)
        setContentView(pantalla)
        WakeService.iniciar(this)

        if (!Ajustes.configurado(this)) {
            startActivity(Intent(this, SetupActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        modoInmersivo()
        pantalla.cargar(Ajustes.url(this))
        pantalla.iniciarRefresco()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        handler.removeCallbacks(soltarPantalla)
        handler.postDelayed(soltarPantalla, Ajustes.minutosVisible(this) * 60_000L)
    }

    override fun onPause() {
        super.onPause()
        pantalla.detener()
        handler.removeCallbacks(soltarPantalla)
    }

    override fun onDestroy() {
        pantalla.destroy()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) modoInmersivo()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            val ahora = SystemClock.elapsedRealtime()
            toquesArriba.addLast(ahora)
            while (toquesArriba.isNotEmpty() && ahora - toquesArriba.first() > 3000) toquesArriba.removeFirst()
            if (toquesArriba.size >= 5) {
                toquesArriba.clear()
                startActivity(Intent(this, SetupActivity::class.java))
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @Suppress("DEPRECATION")
    private fun modoInmersivo() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }
}
