package com.alexaventuras.bienvenida

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Pantalla de configuración (se abre la primera vez, o con ARRIBA x5 en la bienvenida). */
class SetupActivity : Activity() {
    private lateinit var estadoPermiso: TextView
    private lateinit var campoUrl: EditText
    private lateinit var campoMinutos: EditText
    private lateinit var campoCasa: EditText
    private lateinit var campoRed: EditText
    private lateinit var campoClave: EditText
    private lateinit var campoCodigo: EditText
    private var ingles = false
    private var destino = ""

    /** Destinos: "" = Cancún (Alex Aventuras), "tupungato" = Tupungato (Volcano). */
    private fun textoDestino() = if (destino == "tupungato") "Destino: Tupungato – Volcano (cambiar)" else "Destino: Cancún – Alex Aventuras (cambiar)"

    private fun textoIdioma() = if (ingles) "Idioma de la pantalla: English (cambiar)" else "Idioma de la pantalla: Español (cambiar)"

    private fun campo(pista: String, valor: String) = EditText(this).apply {
        setText(valor)
        hint = pista
        inputType = InputType.TYPE_CLASS_TEXT
        setTextColor(Color.WHITE); setHintTextColor(Color.parseColor("#9FC3CC"))
        textSize = 20f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = dp(40)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            setBackgroundColor(Color.parseColor("#1F4E5F"))
        }

        col.addView(texto("Configuración – Bienvenida Alex Aventuras", 30f, true))
        col.addView(texto("1. Permiso para abrirse sola al encender la tele", 22f, true, arriba = 24))
        estadoPermiso = texto("", 18f, false)
        col.addView(estadoPermiso)
        col.addView(boton("Dar permiso “Mostrar sobre otras apps”") { pedirPermiso() })

        col.addView(texto("2. Datos de esta casa", 22f, true, arriba = 24))
        col.addView(texto("Tip: con la app Google TV del celular puedes escribir con el teclado del teléfono.", 16f, false))
        campoCasa = campo("Nombre de la casa (ej. Shark Villa)", Ajustes.texto(this, "casa"))
        campoRed = campo("Nombre de la red Wi-Fi", Ajustes.texto(this, "red"))
        campoClave = campo("Contraseña del Wi-Fi", Ajustes.texto(this, "clave"))
        col.addView(campoCasa, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        col.addView(campoRed, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        col.addView(campoClave, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        col.addView(texto("Código de la casa (opcional): está en el panel de alexaventuras.com → Teles de bienvenida. Con él, la tele saluda al huésped por su nombre.", 16f, false, arriba = 12))
        campoCodigo = campo("Código de 8 letras (ej. ABCD-2345)", Ajustes.texto(this, "codigo")).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        }
        col.addView(campoCodigo, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        ingles = Ajustes.texto(this, "idioma") == "en"
        val botonIdioma = boton(textoIdioma()) {}
        botonIdioma.setOnClickListener { ingles = !ingles; botonIdioma.text = textoIdioma() }
        col.addView(botonIdioma)
        destino = Ajustes.texto(this, "destino")
        val botonDestino = boton(textoDestino()) {}
        botonDestino.setOnClickListener { destino = if (destino == "tupungato") "" else "tupungato"; botonDestino.text = textoDestino() }
        col.addView(botonDestino)

        col.addView(texto("Avanzado: dirección de una página en internet (déjala vacía para usar los datos de arriba)", 16f, false, arriba = 16))
        campoUrl = campo("https://…", Ajustes.urlGuardada(this)).apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI }
        col.addView(campoUrl, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        col.addView(texto("3. Minutos que la bienvenida mantiene la tele encendida", 22f, true, arriba = 24))
        campoMinutos = EditText(this).apply {
            setText(Ajustes.minutosVisible(this@SetupActivity).toString())
            inputType = InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.WHITE); textSize = 20f
        }
        col.addView(campoMinutos, LinearLayout.LayoutParams(dp(200), WRAP_CONTENT))

        col.addView(texto("4. (Opcional) Usarla también como protector de pantalla", 22f, true, arriba = 24))
        col.addView(boton("Abrir ajustes del protector de pantalla") { abrirProtector() })

        col.addView(boton("Guardar y mostrar la bienvenida", principal = true) { guardar() })

        setContentView(ScrollView(this).apply { addView(col) })
    }

    override fun onResume() {
        super.onResume()
        if (Ajustes.puedeAbrirseSola(this)) {
            estadoPermiso.text = "✓ Listo: la bienvenida se abrirá sola al encender."
            estadoPermiso.setTextColor(Color.parseColor("#9BE7A0"))
        } else {
            estadoPermiso.text = "✗ Falta el permiso. Pulsa el botón y activa “Bienvenida Alex Aventuras”.\n" +
                "Si tu tele no muestra esa opción, se activa una vez desde una compu con:\n" +
                "adb shell appops set $packageName SYSTEM_ALERT_WINDOW allow"
            estadoPermiso.setTextColor(Color.parseColor("#FFD27A"))
        }
    }

    private fun pedirPermiso() {
        val intentos = listOf(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")),
        )
        for (i in intentos) {
            try { startActivity(i); return } catch (_: ActivityNotFoundException) {} catch (_: SecurityException) {}
        }
    }

    private fun abrirProtector() {
        val intentos = listOf(Intent(Settings.ACTION_DREAM_SETTINGS), Intent(Settings.ACTION_DISPLAY_SETTINGS))
        for (i in intentos) {
            try { startActivity(i); return } catch (_: ActivityNotFoundException) {}
        }
    }

    private fun guardar() {
        var url = campoUrl.text.toString().trim()
        if (url.isNotEmpty() && !url.startsWith("http")) url = "https://$url"
        Ajustes.guardarUrl(this, url)
        Ajustes.guardarTexto(this, "casa", campoCasa.text.toString())
        Ajustes.guardarTexto(this, "red", campoRed.text.toString())
        Ajustes.guardarTexto(this, "clave", campoClave.text.toString())
        Ajustes.guardarTexto(this, "codigo", campoCodigo.text.toString().uppercase().replace(Regex("[^A-Z0-9]"), ""))
        Ajustes.guardarTexto(this, "idioma", if (ingles) "en" else "es")
        Ajustes.guardarTexto(this, "destino", destino)
        Ajustes.guardarMinutos(this, campoMinutos.text.toString().toIntOrNull() ?: 30)
        Ajustes.marcarConfigurado(this)
        WakeService.iniciar(this)
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
        finish()
    }

    private fun texto(t: String, size: Float, negrita: Boolean, arriba: Int = 0) = TextView(this).apply {
        text = t
        textSize = size
        setTextColor(Color.WHITE)
        if (negrita) setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(arriba), 0, dp(6))
    }

    private fun boton(t: String, principal: Boolean = false, accion: () -> Unit) = Button(this).apply {
        text = t
        textSize = 18f
        isAllCaps = false
        gravity = Gravity.CENTER
        if (principal) setTextColor(Color.parseColor("#1F4E5F"))
        setOnClickListener { accion() }
        layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            topMargin = dp(if (principal) 32 else 8)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
