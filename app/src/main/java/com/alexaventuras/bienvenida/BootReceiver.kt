package com.alexaventuras.bienvenida

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Al prender el aparato (o actualizar la app) arranca el vigilante. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val arranque = intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        WakeService.iniciar(context, arranque)
    }
}
