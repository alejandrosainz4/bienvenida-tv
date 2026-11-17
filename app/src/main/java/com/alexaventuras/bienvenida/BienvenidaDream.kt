package com.alexaventuras.bienvenida

import android.service.dreams.DreamService

/** Permite elegir la bienvenida como protector de pantalla de la tele. */
class BienvenidaDream : DreamService() {
    private var pantalla: Pantalla? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = true
        val p = Pantalla(this)
        pantalla = p
        setContentView(p)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        pantalla?.cargar(Ajustes.url(this))
        pantalla?.iniciarRefresco()
    }

    override fun onDreamingStopped() {
        pantalla?.detener()
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        pantalla?.destroy()
        pantalla = null
        super.onDetachedFromWindow()
    }
}
