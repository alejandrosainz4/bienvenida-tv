package com.alexaventuras.bienvenida

import android.content.Context
import android.os.Build
import android.provider.Settings

/** Configuración guardada en el aparato. */
object Ajustes {
    const val DEMO_URL = "file:///android_asset/bienvenida.html?demo=1"
    /** Bienvenida genérica (sin nombre) que se muestra si no hay internet ni copia guardada. */
    const val GENERICA_URL = "file:///android_asset/bienvenida.html"

    private fun prefs(c: Context) = c.getSharedPreferences("ajustes", Context.MODE_PRIVATE)

    /**
     * Dirección que se muestra:
     *  - si hay una dirección avanzada guardada (página en internet), esa;
     *  - si no, la pantalla incluida con los datos de la casa (nombre, Wi-Fi);
     *  - si no hay nada configurado, la demostración.
     */
    fun url(c: Context): String {
        val avanzada = urlGuardada(c)
        if (avanzada.isNotBlank()) return avanzada
        val casa = texto(c, "casa"); val red = texto(c, "red"); val clave = texto(c, "clave"); val codigo = texto(c, "codigo")
        if (casa.isBlank() && red.isBlank() && codigo.isBlank() && texto(c, "destino").isBlank()) return DEMO_URL
        val b = android.net.Uri.parse(GENERICA_URL).buildUpon()
        if (casa.isNotBlank()) b.appendQueryParameter("casa", casa)
        if (red.isNotBlank()) b.appendQueryParameter("red", red)
        if (clave.isNotBlank()) b.appendQueryParameter("clave", clave)
        if (codigo.isNotBlank()) b.appendQueryParameter("codigo", codigo)
        val destino = texto(c, "destino")
        if (destino.isNotBlank()) b.appendQueryParameter("destino", destino)
        b.appendQueryParameter("idioma", texto(c, "idioma").ifBlank { "es" })
        return b.build().toString()
    }

    fun texto(c: Context, clave: String): String = prefs(c).getString(clave, "")!!
    fun guardarTexto(c: Context, clave: String, valor: String) = prefs(c).edit().putString(clave, valor.trim()).apply()
    fun urlGuardada(c: Context): String = prefs(c).getString("url", "")!!
    fun guardarUrl(c: Context, url: String) = prefs(c).edit().putString("url", url.trim()).apply()

    /** Minutos que la bienvenida mantiene la pantalla encendida antes de dejar actuar al reposo de la TV. */
    fun minutosVisible(c: Context): Int = prefs(c).getInt("minutos", 30)
    fun guardarMinutos(c: Context, m: Int) = prefs(c).edit().putInt("minutos", m.coerceIn(1, 240)).apply()

    /** Si es false, la primera vez se abre la configuración. */
    fun configurado(c: Context): Boolean = prefs(c).getBoolean("configurado", false)
    fun marcarConfigurado(c: Context) = prefs(c).edit().putBoolean("configurado", true).apply()

    /** Sin este permiso Android no deja que la app se abra sola. */
    fun puedeAbrirseSola(c: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(c)
}
