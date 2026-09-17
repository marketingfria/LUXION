package com.luxion.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

public class FloatingService extends Service {

    private static final String CHANNEL_ID = "LUXION_SERVICE";
    private static final int NOTIFICATION_ID = 1001;

    private WindowManager windowManager;
    private TextView floatingButton;
    private WindowManager.LayoutParams params;

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;

    private float toqueInicialX;
    private float toqueInicialY;

    private int posicionInicialX;
    private int posicionInicialY;

    private boolean seEstaMoviendo = false;
    private boolean escuchando = false;

    /*
     * =========================================================
     * ESTADO DE LUXION
     * =========================================================
     */

    private boolean esperandoLuxion = false;
    private boolean escuchandoComando = false;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    /*
     * =========================================================
     * CREAR SERVICIO
     * =========================================================
     */

    @Override
    public void onCreate() {
        super.onCreate();

        crearCanalNotificacion();
        iniciarServicioForeground();
        inicializarVoz();

        windowManager =
                (WindowManager) getSystemService(WINDOW_SERVICE);

        crearBotonFlotante();

        /*
         * Esperamos un momento antes de comenzar
         * la primera detección de "Luxion".
         */
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        iniciarEsperaLuxion();
                    }
                },
                1000
        );
    }

    /*
     * =========================================================
     * NOTIFICACION
     * =========================================================
     */

    private void crearCanalNotificacion() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "LUXION",
                            NotificationManager.IMPORTANCE_LOW
                    );

            channel.setDescription(
                    "Servicio flotante de LUXION"
            );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /*
     * =========================================================
     * SERVICIO FOREGROUND
     * =========================================================
     */

    private void iniciarServicioForeground() {

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder =
                    new Notification.Builder(
                            this,
                            CHANNEL_ID
                    );

        } else {

            builder =
                    new Notification.Builder(this);
        }

        builder
                .setContentTitle("LUXION activo")
                .setContentText(
                        "Di Luxion o toca la L para hablar"
                )
                .setSmallIcon(
                        android.R.drawable.ic_btn_speak_now
                )
                .setOngoing(true);

        startForeground(
                NOTIFICATION_ID,
                builder.build()
        );
    }

    /*
     * =========================================================
     * VOZ
     * =========================================================
     */

    private void inicializarVoz() {

        if (!SpeechRecognizer
                .isRecognitionAvailable(this)) {

            return;
        }

        textToSpeech =
                new TextToSpeech(
                        this,
                        status -> {

                            if (status ==
                                    TextToSpeech.SUCCESS) {

                                textToSpeech.setLanguage(
                                        new Locale(
                                                "es",
                                                "ES"
                                        )
                                );
                            }
                        }
                );

        speechRecognizer =
                SpeechRecognizer
                        .createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(
                            Bundle params) {

                        escuchando = true;

                        if (esperandoLuxion) {
                            actualizarBoton("L");
                        } else {
                            actualizarBoton("🎙");
                        }
                    }

                    @Override
                    public void onBeginningOfSpeech() {

                        if (esperandoLuxion) {
                            actualizarBoton("L");
                        } else {
                            actualizarBoton("🔴");
                        }
                    }

                    @Override
                    public void onRmsChanged(
                            float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(
                            byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {

                        escuchando = false;

                        if (!esperandoLuxion) {
                            actualizarBoton("L");
                        }
                    }

                    @Override
                    public void onError(
                            int error) {

                        escuchando = false;

                        actualizarBoton("L");

                        /*
                         * Si estamos esperando "Luxion",
                         * volvemos a intentarlo.
                         */
                        if (esperandoLuxion) {
                            programarEsperaLuxion(1000);
                        }
                    }

                    @Override
                    public void onResults(
                            Bundle results) {

                        escuchando = false;

                        ArrayList<String> resultados =
                                results.getStringArrayList(
                                        SpeechRecognizer
                                                .RESULTS_RECOGNITION
                                );

                        if (resultados == null ||
                                resultados.isEmpty()) {

                            if (esperandoLuxion) {
                                programarEsperaLuxion(700);
                            }

                            return;
                        }

                        String texto =
                                resultados.get(0);

                        /*
                         * =================================================
                         * MODO ESPERA DE "LUXION"
                         * =================================================
                         */

                        if (esperandoLuxion) {

                            String resto =
                                    detectarLuxion(texto);

                            /*
                             * No dijo Luxion.
                             */
                            if (resto == null) {

                                programarEsperaLuxion(700);
                                return;
                            }

                            /*
                             * LUXION FUE DETECTADO.
                             */
                            esperandoLuxion = false;
                            escuchandoComando = true;

                            actualizarBoton("⚡");

                            /*
                             * Caso:
                             *
                             * "Luxion abre YouTube"
                             *
                             * Se ejecuta directamente.
                             */
                            if (!resto.trim().isEmpty()) {

                                procesarComando(resto);

                                programarEsperaLuxion(1800);

                                return;
                            }

                            /*
                             * Caso:
                             *
                             * "Luxion"
                             *
                             * Ahora esperamos el comando.
                             */
                            hablar(
                                    "Te escucho."
                            );

                            programarComando(1200);

                            return;
                        }

                        /*
                         * =================================================
                         * MODO COMANDO
                         * =================================================
                         */

                        escuchandoComando = false;

                        actualizarBoton("L");

                        procesarComando(texto);

                        /*
                         * Después del comando volvemos a esperar
                         * la palabra "Luxion".
                         */
                        programarEsperaLuxion(1800);
                    }

                    @Override
                    public void onPartialResults(
                            Bundle partialResults) {
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            Bundle params) {
                    }
                }
        );
    }

    /*
     * =========================================================
     * ACTUALIZAR BOTON
     * =========================================================
     */

    private void actualizarBoton(
            String texto) {

        if (floatingButton == null) {
            return;
        }

        try {

            floatingButton.setText(texto);

        } catch (Exception ignored) {
        }
    }

    /*
     * =========================================================
     * CREAR BOTON FLOTANTE
     * =========================================================
     */

    private void crearBotonFlotante() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M) {

            if (!Settings.canDrawOverlays(this)) {

                stopSelf();
                return;
            }
        }

        floatingButton =
                new TextView(this);

        floatingButton.setText("L");

        floatingButton.setTextSize(22);

        floatingButton.setTextColor(
                Color.WHITE
        );

        floatingButton.setGravity(
                Gravity.CENTER
        );

        floatingButton.setBackgroundColor(
                Color.rgb(
                        30,
                        30,
                        35
                )
        );

        floatingButton.setElevation(10);

        int tipoVentana;

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            tipoVentana =
                    WindowManager.LayoutParams
                            .TYPE_APPLICATION_OVERLAY;

        } else {

            tipoVentana =
                    WindowManager.LayoutParams
                            .TYPE_PHONE;
        }

        params =
                new WindowManager.LayoutParams(
                        70,
                        70,
                        tipoVentana,
                        WindowManager.LayoutParams
                                .FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );

        params.gravity =
                Gravity.TOP | Gravity.END;

        params.x = 20;
        params.y = 300;

        try {

            windowManager.addView(
                    floatingButton,
                    params
            );

        } catch (Exception e) {

            stopSelf();
            return;
        }

        configurarMovimientoYToque();
    }

    /*
     * =========================================================
     * MOVIMIENTO Y TOQUE
     * =========================================================
     */

    private void configurarMovimientoYToque() {

        floatingButton.setOnTouchListener(
                new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(
                            View view,
                            MotionEvent event) {

                        switch (event.getAction()) {

                            case MotionEvent.ACTION_DOWN:

                                toqueInicialX =
                                        event.getRawX();

                                toqueInicialY =
                                        event.getRawY();

                                posicionInicialX =
                                        params.x;

                                posicionInicialY =
                                        params.y;

                                seEstaMoviendo = false;

                                return true;

                            case MotionEvent.ACTION_MOVE:

                                float diferenciaX =
                                        event.getRawX()
                                                - toqueInicialX;

                                float diferenciaY =
                                        event.getRawY()
                                                - toqueInicialY;

                                if (Math.abs(diferenciaX) > 10 ||
                                        Math.abs(diferenciaY) > 10) {

                                    seEstaMoviendo = true;
                                }

                                params.x =
                                        posicionInicialX
                                                - (int) diferenciaX;

                                params.y =
                                        posicionInicialY
                                                + (int) diferenciaY;

                                try {

                                    windowManager.updateViewLayout(
                                            floatingButton,
                                            params
                                    );

                                } catch (Exception ignored) {
                                }

                                return true;

                            case MotionEvent.ACTION_UP:

                                if (!seEstaMoviendo) {

                                    activarMicrofono();
                                }

                                return true;
                        }

                        return true;
                    }
                }
        );
    }

    /*
     * =========================================================
     * ACTIVACION MANUAL DEL MICROFONO
     * =========================================================
     */

    private void activarMicrofono() {

        if (speechRecognizer == null) {

            inicializarVoz();
        }

        if (speechRecognizer == null) {

            hablar(
                    "El reconocimiento de voz no está disponible."
            );

            return;
        }

        /*
         * El botón cancela el modo de espera de "Luxion"
         * y pasa directamente al modo comando.
         */
        esperandoLuxion = false;
        escuchandoComando = true;

        if (escuchando) {

            try {

                speechRecognizer.cancel();

            } catch (Exception ignored) {
            }

            escuchando = false;
        }

        actualizarBoton("🎙");

        programarComando(250);
    }

    /*
     * =========================================================
     * ESPERAR "LUXION"
     * =========================================================
     */

    private void iniciarEsperaLuxion() {

        if (speechRecognizer == null) {
            return;
        }

        if (escuchando) {
            return;
        }

        esperandoLuxion = true;
        escuchandoComando = false;

        iniciarReconocimiento();
    }

    private void programarEsperaLuxion(
            long milisegundos) {

        handler.postDelayed(
                new Runnable() {

                    @Override
                    public void run() {

                        iniciarEsperaLuxion();
                    }
                },
                milisegundos
        );
    }

    /*
     * =========================================================
     * PROGRAMAR COMANDO
     * =========================================================
     */

    private void programarComando(
            long milisegundos) {

        handler.postDelayed(
                new Runnable() {

                    @Override
                    public void run() {

                        iniciarReconocimiento();
                    }
                },
                milisegundos
        );
    }

    /*
     * =========================================================
     * INICIAR RECONOCIMIENTO
     * =========================================================
     */

    private void iniciarReconocimiento() {

        if (speechRecognizer == null) {
            return;
        }

        if (escuchando) {
            return;
        }

        Intent intent =
                new Intent(
                        RecognizerIntent
                                .ACTION_RECOGNIZE_SPEECH
                );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent
                        .LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "es-ES"
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        try {

            speechRecognizer.startListening(
                    intent
            );

        } catch (Exception ignored) {

            escuchando = false;

            if (esperandoLuxion) {

                programarEsperaLuxion(1200);
            }
        }
    }

    /*
     * =========================================================
     * DETECTAR PALABRA "LUXION"
     * =========================================================
     */

    private String detectarLuxion(
            String texto) {

        if (texto == null) {
            return null;
        }

        String original =
                texto.trim();

        if (original.isEmpty()) {
            return null;
        }

        /*
         * Eliminamos acentos para que:
         *
         * Luxion
         * Luxión
         *
         * sean tratados igual.
         */
        String normalizado =
                Normalizer.normalize(
                        original,
                        Normalizer.Form.NFD
                )
                        .replaceAll(
                                "\\p{M}",
                                ""
                        )
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .trim();

        String palabra =
                "luxion";

        int posicion =
                normalizado.indexOf(
                        palabra
                );

        if (posicion < 0) {

            return null;
        }

        /*
         * Comprobamos que "luxion" sea una palabra
         * independiente y no parte de otra palabra.
         */
        boolean limiteAnterior =
                posicion == 0 ||
                        !Character.isLetterOrDigit(
                                normalizado.charAt(
                                        posicion - 1
                                )
                        );

        int finalPalabra =
                posicion + palabra.length();

        boolean limitePosterior =
                finalPalabra >=
                        normalizado.length() ||
                        !Character.isLetterOrDigit(
                                normalizado.charAt(
                                        finalPalabra
                                )
                        );

        if (!limiteAnterior ||
                !limitePosterior) {

            return null;
        }

        /*
         * Extraemos todo lo que venga después
         * de "Luxion".
         */
        String resto =
                original.substring(
                        finalPalabra
                ).trim();

        resto =
                resto.replaceFirst(
                        "^[,;:.!?¡¿\\s]+",
                        ""
                ).trim();

        return resto;
    }

    /*
     * =========================================================
     * PROCESAMIENTO PRINCIPAL DE COMANDOS
     * =========================================================
     */

    private void procesarComando(
            String texto) {

        if (texto == null) {
            return;
        }

        String comandoOriginal =
                texto.trim();

        if (comandoOriginal.isEmpty()) {
            return;
        }

        String comando =
                comandoOriginal
                        .toLowerCase(Locale.ROOT)
                        .trim();

        /*
         * =====================================================
         * BUSQUEDA LIBRE EN YOUTUBE
         * =====================================================
         */

        if (esComandoDeBusqueda(comando)) {

            String busqueda =
                    extraerBusquedaLibre(
                            comandoOriginal
                    );

            if (!busqueda.isEmpty()) {

                hablar(
                        "Buscando " + busqueda
                );

                boolean encontrado =
                        YouTubeSearch.buscar(
                                this,
                                busqueda
                        );

                if (!encontrado) {

                    hablar(
                            "No pude realizar la búsqueda."
                    );
                }

                return;
            }

            hablar(
                    "Dime qué quieres buscar."
            );

            return;
        }

        /*
         * =====================================================
         * WHATSAPP
         * =====================================================
         */

        if (comando.contains("whatsapp") ||
                comando.contains("wasap") ||
                comando.contains("watsapp") ||
                comando.contains("guasap")) {

            hablar(
                    "Abriendo WhatsApp."
            );

            abrirAplicacion(
                    new String[]{
                            "com.whatsapp",
                            "com.whatsapp.w4b"
                    }
            );

            return;
        }

        /*
         * =====================================================
         * GMAIL
         * =====================================================
         */

        if (comando.contains("gmail") ||
                comando.contains("correo") ||
                comando.contains("email")) {

            hablar(
                    "Abriendo Gmail."
            );

            abrirAplicacion(
                    new String[]{
                            "com.google.android.gm"
                    }
            );

            return;
        }

        /*
         * =====================================================
         * YOUTUBE
         * =====================================================
         */

        if (comando.contains("youtube") ||
                comando.contains("you tube")) {

            hablar(
                    "Abriendo YouTube."
            );

            abrirAplicacion(
                    new String[]{
                            "com.google.android.youtube"
                    }
            );

            return;
        }

        /*
         * =====================================================
         * TIKTOK
         * =====================================================
         */

        if (comando.contains("tiktok") ||
                comando.contains("tik tok")) {

            hablar(
                    "Abriendo TikTok."
            );

            abrirAplicacion(
                    new String[]{
                            "com.zhiliaoapp.musically",
                            "com.ss.android.ugc.trill"
                    }
            );

            return;
        }

        /*
         * =====================================================
         * INSTAGRAM
         * =====================================================
         */

        if (comando.contains("instagram")) {

            hablar(
                    "Abriendo Instagram."
            );

            abrirAplicacion(
                    new String[]{
                            "com.instagram.android"
                    }
            );

            return;
        }

        /*
         * =====================================================
         * FACEBOOK
         * =====================================================
         */

        if (comando.contains("facebook")) {

            hablar(
                    "Abriendo Facebook."
            );

            abrirAplicacion(
                    new String[]{
                            "com.facebook.katana"
                    }
            );

            return;
        }

        /*
         * =====================================================
         * TELEGRAM
         * =====================================================
         */

        if (comando.contains("telegram")) {

            hablar(
                    "Abriendo Telegram."
            );

            abrirAplicacion(
                    new String[]{
                            "org.telegram.messenger"
                    }
            );

            return;
        }

        /*
         * =====================================================
         * SPOTIFY
         * =====================================================
         */

        if (comando.contains("spotify")) {

            hablar(
                    "Abriendo Spotify."
            );

            abrirAplicacion(
                    new String[]{
                            "com.spotify.music"
                    }
            );

            return;
        }

        /*
         * =====================================================
         * CHROME / NAVEGADOR
         * =====================================================
         */

        if (comando.contains("chrome") ||
                comando.contains("navegador")) {

            hablar(
                    "Abriendo Chrome."
            );

            abrirAplicacion(
                    new String[]{
                            "com.android.chrome"
                    }
            );

            return;
        }

        /*
         * =====================================================
         * GOOGLE MAPS
         * =====================================================
         */

        if (comando.contains("mapas") ||
                comando.contains("google maps")) {

            hablar(
                    "Abriendo Google Maps."
            );

            abrirAplicacion(
                    new String[]{
                            "com.google.android.apps.maps"
                    }
            );

            return;
        }

        /*
         * =====================================================
         * AJUSTES
         * =====================================================
         */

        if (comando.contains("ajustes") ||
                comando.contains("configuración") ||
                comando.contains("configuracion")) {

            hablar(
                    "Abriendo configuración."
            );

            abrirConfiguracion();

            return;
        }

        /*
         * =====================================================
         * CAMARA
         * =====================================================
         */

        if (comando.contains("cámara") ||
                comando.contains("camara")) {

            hablar(
                    "Abriendo cámara."
            );

            abrirCamara();

            return;
        }

        /*
         * =====================================================
         * CONTACTOS
         * =====================================================
         */

        if (comando.contains("contactos")) {

            hablar(
                    "Abriendo contactos."
            );

            abrirContactos();

            return;
        }

        /*
         * =====================================================
         * PLAY STORE
         * =====================================================
         */

        if (comando.contains("play store") ||
                comando.contains("tienda")) {

            hablar(
                    "Abriendo Play Store."
            );

            abrirAplicacion(
                    new String[]{
                            "com.android.vending"
                    }
            );

            return;
        }

        /*
         * =====================================================
         * COMANDO DESCONOCIDO
         * =====================================================
         */

        hablar(
                "Todavía no conozco ese comando."
        );
    }

    /*
     * =========================================================
     * DETECTAR BUSQUEDA
     * =========================================================
     */

    private boolean esComandoDeBusqueda(
            String comando) {

        if (comando == null) {
            return false;
        }

        String valor =
                comando
                        .toLowerCase(Locale.ROOT)
                        .trim();

        return valor.equals("busca")
                || valor.startsWith("busca ")
                || valor.equals("buscar")
                || valor.startsWith("buscar ")
                || valor.startsWith("búscame ")
                || valor.startsWith("buscame ")
                || valor.startsWith("quiero buscar ")
                || valor.startsWith("quiero que busques ")
                || valor.startsWith("puedes buscar ")
                || valor.startsWith("puedes buscarme ")
                || valor.startsWith("me puedes buscar ");
    }

    /*
     * =========================================================
     * EXTRAER BUSQUEDA
     * =========================================================
     */

    private String extraerBusquedaLibre(
            String texto) {

        if (texto == null) {
            return "";
        }

        String resultado =
                texto.trim();

        String minusculas =
                resultado
                        .toLowerCase(Locale.ROOT);

        String[] comandosBusqueda = {

                "quiero que busques ",

                "quiero buscar ",

                "me puedes buscar ",

                "puedes buscarme ",

                "puedes buscar ",

                "búscame ",

                "buscame ",

                "buscarme ",

                "busca ",

                "buscar "
        };

        for (String prefijo :
                comandosBusqueda) {

            if (minusculas.startsWith(
                    prefijo)) {

                resultado =
                        resultado.substring(
                                prefijo.length()
                        ).trim();

                break;
            }
        }

        if (resultado
                .equalsIgnoreCase("busca")) {

            return "";
        }

        if (resultado
                .equalsIgnoreCase("buscar")) {

            return "";
        }

        resultado =
                resultado
                        .replace("¿", "")
                        .replace("?", "")
                        .replace("¡", "")
                        .replace("!", "")
                        .trim();

        return resultado;
    }

    /*
     * =========================================================
     * ABRIR APLICACION
     * =========================================================
     */

    private void abrirAplicacion(
            String[] paquetes) {

        if (paquetes == null ||
                paquetes.length == 0) {

            return;
        }

        for (String paquete :
                paquetes) {

            if (paquete == null ||
                    paquete.trim().isEmpty()) {

                continue;
            }

            try {

                Intent intent =
                        getPackageManager()
                                .getLaunchIntentForPackage(
                                        paquete
                                );

                if (intent != null) {

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    );

                    startActivity(intent);

                    return;
                }

            } catch (Exception ignored) {
            }
        }

        hablar(
                "No encontré esa aplicación instalada."
        );
    }

    /*
     * =========================================================
     * ABRIR CONFIGURACION
     * =========================================================
     */

    private void abrirConfiguracion() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_SETTINGS
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);

        } catch (Exception e) {

            hablar(
                    "No pude abrir configuración."
            );
        }
    }

    /*
     * =========================================================
     * ABRIR CAMARA
     * =========================================================
     */

    private void abrirCamara() {

        try {

            Intent intent =
                    new Intent(
                            android.provider.MediaStore
                                    .ACTION_IMAGE_CAPTURE
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);

        } catch (Exception e) {

            hablar(
                    "No pude abrir la cámara."
            );
        }
    }

    /*
     * =========================================================
     * ABRIR CONTACTOS
     * =========================================================
     */

    private void abrirContactos() {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            android.provider.ContactsContract
                                    .Contacts.CONTENT_URI
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);

        } catch (Exception e) {

            hablar(
                    "No pude abrir los contactos."
            );
        }
    }

    /*
     * =========================================================
     * RESPUESTA DE VOZ
     * =========================================================
     */

    private void hablar(
            String mensaje) {

        if (mensaje == null ||
                mensaje.trim().isEmpty()) {

            return;
        }

        /*
         * Cancelamos cualquier escucha activa para evitar
         * que LUXION pueda escucharse a sí misma.
         */
        if (speechRecognizer != null &&
                escuchando) {

            try {

                speechRecognizer.cancel();

            } catch (Exception ignored) {
            }

            escuchando = false;
        }

        if (textToSpeech != null) {

            textToSpeech.speak(
                    mensaje,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "LUXION"
            );
        }
    }

    /*
     * =========================================================
     * DESTRUIR SERVICIO
     * =========================================================
     */

    @Override
    public void onDestroy() {

        handler.removeCallbacksAndMessages(null);

        if (speechRecognizer != null) {

            try {

                speechRecognizer.cancel();

            } catch (Exception ignored) {
            }

            speechRecognizer.destroy();

            speechRecognizer = null;
        }

        if (textToSpeech != null) {

            textToSpeech.stop();

            textToSpeech.shutdown();

            textToSpeech = null;
        }

        if (floatingButton != null &&
                windowManager != null) {

            try {

                windowManager.removeView(
                        floatingButton
                );

            } catch (Exception ignored) {
            }

            floatingButton = null;
        }

        super.onDestroy();
    }

    /*
     * =========================================================
     * BIND
     * =========================================================
     */

    @Override
    public IBinder onBind(
            Intent intent) {

        return null;
    }
}
