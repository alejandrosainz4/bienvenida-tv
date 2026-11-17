package com.alexaventuras.bienvenida

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

/**
 * Servicio que queda corriendo en segundo plano y abre la bienvenida:
 *  1) cuando el aparato termina de arrancar, y
 *  2) cada vez que la tele sale de reposo (pantalla encendida).
 * Android solo permite esto si la app tiene el permiso "Mostrar sobre otras apps".
 */
class WakeService : Service() {
    private val handler = Handler(Looper.getMainLooper())

    private val alEncender = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            if (i.action == Intent.ACTION_SCREEN_ON) abrirConRetraso(1500L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        iniciarEnPrimerPlano()
        registerReceiver(alEncender, IntentFilter(Intent.ACTION_SCREEN_ON))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra(EXTRA_ARRANQUE, false) == true) {
            // Tras el arranque, el menú de la tele tarda en cargar: abrimos dos veces por si nos tapa.
            abrirConRetraso(4000L)
            abrirConRetraso(12000L)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        try { unregisterReceiver(alEncender) } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun abrirConRetraso(ms: Long) {
        handler.postDelayed({
            if (!Ajustes.puedeAbrirseSola(this)) return@postDelayed
            val i = Intent(this, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
            try { startActivity(i) } catch (_: Exception) {}
        }, ms)
    }

    private fun iniciarEnPrimerPlano() {
        val canal = "bienvenida"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(canal, "Bienvenida al encender", NotificationManager.IMPORTANCE_MIN)
            )
        }
        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, canal)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        val n: Notification = builder.setContentTitle("Bienvenida activa")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(ID_NOTIF, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(ID_NOTIF, n)
        }
    }

    companion object {
        private const val ID_NOTIF = 7
        const val EXTRA_ARRANQUE = "arranque"

        fun iniciar(c: Context, arranque: Boolean = false) {
            val i = Intent(c, WakeService::class.java).putExtra(EXTRA_ARRANQUE, arranque)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) c.startForegroundService(i)
                else c.startService(i)
            } catch (_: Exception) {
            }
        }
    }
}
