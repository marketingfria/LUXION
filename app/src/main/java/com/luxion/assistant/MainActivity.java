package com.luxion.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int AUDIO_PERMISSION = 100;

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;

    private TextView estado;
    private TextView resultado;
    private Button boton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        crearInterfaz();
        inicializarVoz();

        if (checkSelfPermission(
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    AUDIO_PERMISSION
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Cada vez que LUXION vuelve a primer plano,
         * comprobamos si el permiso de ventana flotante
         * ya fue concedido.
         */
        iniciarBotonFlotante();
    }

    private void crearInterfaz() {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setGravity(Gravity.CENTER);

        layout.setPadding(
                40,
                40,
                40,
                40
        );

        layout.setBackgroundColor(
                0xFF101014
        );

        TextView titulo =
                new TextView(this);

        titulo.setText("LUXION");
        titulo.setTextSize(36);
        titulo.setTextColor(0xFFFFFFFF);
        titulo.setGravity(Gravity.CENTER);

        TextView subtitulo =
                new TextView(this);

        subtitulo.setText(
                "AI Android Assistant"
        );

        subtitulo.setTextSize(16);
        subtitulo.setTextColor(0xFFAAAAAA);
        subtitulo.setGravity(Gravity.CENTER);

        estado =
                new TextView(this);

        estado.setText(
                "LUXION listo"
        );

        estado.setTextSize(18);
        estado.setTextColor(0xFFFFFFFF);
        estado.setGravity(Gravity.CENTER);

        estado.setPadding(
                0,
                50,
                0,
                20
        );

        resultado =
                new TextView(this);

        resultado.setText(
                "Pulsa el botón y habla"
        );

        resultado.setTextSize(17);
        resultado.setTextColor(0xFFCCCCCC);
        resultado.setGravity(Gravity.CENTER);

        resultado.setPadding(
                0,
                20,
                0,
                30
        );

        boton =
                new Button(this);

        boton.setText(
                "🎙 ACTIVAR LUXION"
        );

        boton.setTextSize(17);

        boton.setOnClickListener(
                v -> escuchar()
        );

        layout.addView(titulo);
        layout.addView(subtitulo);
        layout.addView(estado);
        layout.addView(resultado);
        layout.addView(boton);

        setContentView(layout);
    }

    private void inicializarVoz() {

        textToSpeech =
                new TextToSpeech(
                        this,
                        status -> {

                            if (status ==
                                    TextToSpeech.SUCCESS) {

                                int idioma =
                                        textToSpeech.setLanguage(
                                                new Locale(
                                                        "es",
                                                        "ES"
                                                )
                                        );

                                if (idioma ==
                                        TextToSpeech.LANG_MISSING_DATA
                                        ||
                                        idioma ==
                                        TextToSpeech
                                                .LANG_NOT_SUPPORTED) {

                                    textToSpeech.setLanguage(
                                            Locale.getDefault()
                                    );
                                }
                            }
                        }
                );

        if (!SpeechRecognizer
                .isRecognitionAvailable(this)) {

            estado.setText(
                    "Reconocimiento de voz no disponible"
            );

            return;
        }

        speechRecognizer =
                SpeechRecognizer
                        .createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(
                            Bundle params) {

                        estado.setText(
                                "🎙 Escuchando..."
                        );

                        boton.setText(
                                "🔴 ESCUCHANDO"
                        );
                    }

                    @Override
                    public void onBeginningOfSpeech() {

                        estado.setText(
                                "🎙 Te escucho..."
                        );
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

                        estado.setText(
                                "Procesando..."
                        );

                        boton.setText(
                                "🎙 ACTIVAR LUXION"
                        );
                    }

                    @Override
                    public void onError(
                            int error) {

                        estado.setText(
                                "No entendí. Intenta nuevamente."
                        );

                        boton.setText(
                                "🎙 ACTIVAR LUXION"
                        );
                    }

                    @Override
                    public void onResults(
                            Bundle results) {

                        ArrayList<String>
                                resultados =
                                results.getStringArrayList(
                                        SpeechRecognizer
                                                .RESULTS_RECOGNITION
                                );

                        if (resultados != null &&
                                !resultados.isEmpty()) {

                            String texto =
                                    resultados.get(0);

                            resultado.setText(
                                    "Tú dijiste:\n" +
                                    texto
                            );

                            procesarComando(texto);
                        }

                        boton.setText(
                                "🎙 ACTIVAR LUXION"
                        );
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

    private void escuchar() {

        if (checkSelfPermission(
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    AUDIO_PERMISSION
            );

            return;
        }

        if (speechRecognizer == null) {

            inicializarVoz();
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

        speechRecognizer.startListening(intent);
    }

    private void procesarComando(
            String texto) {

        String comando =
                texto
                        .toLowerCase(Locale.ROOT)
                        .trim();

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

        if (comando.contains("ajustes") ||
                comando.contains("configuración") ||
                comando.contains("configuracion")) {

            hablar(
                    "Abriendo configuración."
            );

            abrirConfiguracion();

            return;
        }

        if (comando.contains("cámara") ||
                comando.contains("camara")) {

            hablar(
                    "Abriendo cámara."
            );

            abrirCamara();

            return;
        }

        if (comando.contains("contactos")) {

            hablar(
                    "Abriendo contactos."
            );

            abrirContactos();

            return;
        }

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

        hablar(
                "Todavía no conozco ese comando."
        );

        estado.setText(
                "Comando no reconocido"
        );
    }

    private void abrirAplicacion(
            String[] paquetes) {

        for (String paquete : paquetes) {

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

                    estado.setText(
                            "Aplicación abierta"
                    );

                    return;
                }

            } catch (Exception ignored) {
            }
        }

        hablar(
                "No encontré esa aplicación instalada."
        );

        estado.setText(
                "Aplicación no encontrada"
        );
    }

    private void abrirConfiguracion() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_SETTINGS
                    );

            startActivity(intent);

        } catch (Exception e) {

            hablar(
                    "No pude abrir configuración."
            );
        }
    }

    private void abrirCamara() {

        try {

            Intent intent =
                    new Intent(
                            android.provider.MediaStore
                                    .ACTION_IMAGE_CAPTURE
                    );

            startActivity(intent);

        } catch (Exception e) {

            hablar(
                    "No pude abrir la cámara."
            );
        }
    }

    private void abrirContactos() {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            android.provider.ContactsContract
                                    .Contacts.CONTENT_URI
                    );

            startActivity(intent);

        } catch (Exception e) {

            hablar(
                    "No pude abrir los contactos."
            );
        }
    }

    private void hablar(
            String mensaje) {

        if (textToSpeech != null) {

            textToSpeech.speak(
                    mensaje,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "LUXION"
            );
        }
    }

    private void iniciarBotonFlotante() {

        /*
         * Android 6+ necesita permiso para
         * dibujar sobre otras aplicaciones.
         */
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M) {

            if (!Settings.canDrawOverlays(this)) {

                estado.setText(
                        "Activa el permiso del botón flotante"
                );

                try {

                    Intent intent =
                            new Intent(
                                    Settings
                                            .ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse(
                                            "package:" +
                                            getPackageName()
                                    )
                            );

                    startActivity(intent);

                } catch (Exception e) {

                    try {

                        Intent intent =
                                new Intent(
                                        Settings
                                                .ACTION_MANAGE_OVERLAY_PERMISSION
                                );

                        startActivity(intent);

                    } catch (Exception ignored) {
                    }
                }

                return;
            }
        }

        /*
         * El permiso ya está concedido.
         * Ahora iniciamos el servicio.
         */
        Intent servicio =
                new Intent(
                        this,
                        FloatingService.class
                );

        try {

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.O) {

                startForegroundService(
                        servicio
                );

            } else {

                startService(
                        servicio
                );
            }

            estado.setText(
                    "LUXION flotante activo"
            );

        } catch (Exception e) {

            estado.setText(
                    "No se pudo iniciar el botón flotante"
            );
        }
    }

    @Override
    protected void onDestroy() {

        if (speechRecognizer != null) {

            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        if (textToSpeech != null) {

            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }

        super.onDestroy();
    }
}
