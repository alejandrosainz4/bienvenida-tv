# Bienvenida TV – Alex Aventuras

App para Google TV / Android TV. Al encender la tele muestra la bienvenida a pantalla completa:

- saludo con el nombre de la casa (y del huésped, cuando se conecte con Hospitable),
- Wi-Fi con QR para conectarse,
- carrusel de servicios (yate, cuatrimotos, chef, parques, spa…) con un QR a cada página,
- QR a alexaventuras.com,
- el día de salida, los pasos de check-out.

El huésped sale con **Atrás** o **Inicio**, como de cualquier app.

## Cómo se obtiene el APK

Este proyecto se compila solo en GitHub (gratis):

1. Sube esta carpeta a un repositorio **público** de GitHub (por ejemplo `bienvenida-tv`).
2. GitHub Actions corre "Compilar APK" y publica el archivo en *Releases*.
3. La dirección fija para descargarlo es:
   `https://github.com/<usuario>/bienvenida-tv/releases/latest/download/bienvenida-alexaventuras.apk`

También se puede abrir en Android Studio y usar *Build → Build APK*.

## Instalar en cada tele (≈10 min, una sola vez)

1. **Activar instalación de apps externas:** Ajustes → Sistema → Acerca de → presiona 7 veces "Compilación del SO de Android TV" (activa modo desarrollador).
2. Instala la app **Downloader** desde Google Play, y en Ajustes → Apps → Seguridad permite que Downloader instale apps desconocidas.
3. En Downloader escribe el código **6188566** (o la dirección `aftv.news/6188566`), descarga e instala. El código siempre baja la versión más reciente.
4. Abre **Bienvenida Alex Aventuras**. La primera vez aparece la configuración:
   1. **Permiso "Mostrar sobre otras apps":** pulsa el botón y actívalo. Sin esto Android no deja que se abra sola.
   2. **Datos de la casa:** nombre de la casa, red Wi-Fi, contraseña e idioma.
   3. **Minutos visibles:** cuánto tiempo mantiene la tele encendida (30 por defecto). Después, la tele hace su reposo normal.
   4. (Opcional) Elegirla también como **protector de pantalla**.
5. Apaga y prende la tele para probar.

Para volver a la configuración: con la bienvenida en pantalla presiona **ARRIBA 5 veces seguidas**.

### Si la tele no muestra el permiso "Mostrar sobre otras apps"

Algunos Google TV lo esconden. Se activa una sola vez desde una computadora en la misma red:

```
adb connect <IP-de-la-tele>
adb shell appops set com.alexaventuras.bienvenida SYSTEM_ALERT_WINDOW allow
```

En teles TCL / iFFALCON, además, permite el "inicio automático" de la app en su app de seguridad (Safety Guard).

## Qué hace por dentro

| Pieza | Para qué |
|---|---|
| `BootReceiver` | Al prender el aparato, arranca el vigilante. |
| `WakeService` | Queda en segundo plano; cada vez que la tele sale de reposo abre la bienvenida. |
| `MainActivity` | La pantalla completa (un navegador interno sin barras). Recarga cada 10 min. |
| `BienvenidaDream` | Opción de protector de pantalla. |
| `SetupActivity` | Configuración. |
| `assets/bienvenida.html` | El diseño de la pantalla. Funciona sin internet, salvo las fotos. |

## Cambiar los servicios del carrusel

En `app/src/main/assets/bienvenida.html`, lista `SERVICIOS`. Cada uno tiene textos en español e inglés, su página en alexaventuras.com (a donde lleva el QR), una foto (en `assets/fotos/`, tomada del sitio) y, si existe, un video del sitio que se reproduce sin sonido cuando hay internet. Sin internet se ve la foto.

## Siguiente paso: nombre del huésped automático

En "Avanzado" de la configuración se puede poner una página en internet (por ejemplo `alexaventuras.com/tv/shark-villa`) que tome de Hospitable el nombre y las fechas de quien esté hospedado. Esa página puede usar el mismo diseño con `?datos=<json>`.
