package com.luxion.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQUEST_RECORD_AUDIO = 1001;

    private TextView statusText;
    private TextView resultText;

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;

    private boolean listening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        crearInterfaz();
        prepararReconocimiento();

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            statusText.setText("LUXION está listo");

        } else {

            statusText.setText("Pulsa el botón para activar LUXION");
        }
    }

    private void crearInterfaz() {

        LinearLayout mainLayout = new LinearLayout(this);

        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setPadding(40, 40, 40, 40);
        mainLayout.setBackgroundColor(Color.rgb(10, 10, 18));

        TextView title = new TextView(this);

        title.setText("LUXION");
        title.setTextSize(42);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);

        subtitle.setText("AI Android Assistant");
        subtitle.setTextSize(18);
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER);

        statusText = new TextView(this);

        statusText.setText("Preparando LUXION...");
        statusText.setTextSize(17);
        statusText.setTextColor(Color.LTGRAY);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 50, 0, 25);

        resultText = new TextView(this);

        resultText.setText("Di un comando");
        resultText.setTextSize(20);
        resultText.setTextColor(Color.WHITE);
        resultText.setGravity(Gravity.CENTER);
        resultText.setPadding(20, 20, 20, 30);

        Button voiceButton = new Button(this);

        voiceButton.setText("🎙  ACTIVAR LUXION");
        voiceButton.setTextSize(16);

        voiceButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                activarMicrofono();
            }
        });

        mainLayout.addView(title);
        mainLayout.addView(subtitle);
        mainLayout.addView(statusText);
        mainLayout.addView(resultText);
        mainLayout.addView(voiceButton);

        setContentView(mainLayout);
    }

    private void prepararReconocimiento() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            statusText.setText(
                    "El reconocimiento de voz no está disponible"
            );

            return;
        }

        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(Bundle params) {

                        listening = true;

                        statusText.setText(
                                "🎙 LUXION está escuchando..."
                        );
                    }

                    @Override
                    public void onBeginningOfSpeech() {

                        statusText.setText(
                                "🎙 Te estoy escuchando..."
                        );
                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {

                        listening = false;

                        statusText.setText(
                                "Procesando comando..."
                        );
                    }

                    @Override
                    public void onError(int error) {

                        listening = false;

                        String mensaje;

                        switch (error) {

                            case SpeechRecognizer.ERROR_AUDIO:
                                mensaje = "Error de audio";
                                break;

                            case SpeechRecognizer.ERROR_NETWORK:
                                mensaje = "Error de red";
                                break;

                            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                                mensaje = "Tiempo de espera agotado";
                                break;

                            case SpeechRecognizer.ERROR_NO_MATCH:
                                mensaje = "No entendí el comando";
                                break;

                            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                                mensaje = "El reconocimiento está ocupado";
                                break;

                            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                                mensaje = "No detecté tu voz";
                                break;

                            default:
                                mensaje = "No pude reconocer el comando";
                                break;
                        }

                        statusText.setText(mensaje);
                    }

                    @Override
                    public void onResults(Bundle results) {

                        listening = false;

                        ArrayList<String> resultados =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (resultados == null ||
                                resultados.isEmpty()) {

                            statusText.setText(
                                    "No entendí el comando"
                            );

                            return;
                        }

                        String texto = resultados.get(0);

                        resultText.setText(
                                "Tú dijiste:\n" + texto
                        );

                        ejecutarComando(texto);
                    }

                    @Override
                    public void onPartialResults(
                            Bundle partialResults) {

                        ArrayList<String> resultados =
                                partialResults.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (resultados != null &&
                                !resultados.isEmpty()) {

                            resultText.setText(
                                    resultados.get(0)
                            );
                        }
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            Bundle params) {
                    }
                }
        );

        speechIntent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );
    }

    private void activarMicrofono() {

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    REQUEST_RECORD_AUDIO
            );

            return;
        }

        iniciarEscucha();
    }

    private void iniciarEscucha() {

        if (speechRecognizer == null) {

            prepararReconocimiento();
        }

        if (speechRecognizer == null) {

            statusText.setText(
                    "Reconocimiento no disponible"
            );

            return;
        }

        if (listening) {

            speechRecognizer.stopListening();

            listening = false;

            statusText.setText(
                    "LUXION dejó de escuchar"
            );

            return;
        }

        resultText.setText(
                "Habla ahora..."
        );

        statusText.setText(
                "Preparando micrófono..."
        );

        speechRecognizer.startListening(
                speechIntent
        );
    }

    private void ejecutarComando(String comando) {

        String texto = comando
                .toLowerCase(Locale.ROOT)
                .trim();

        // WHATSAPP
        if (contiene(texto, "whatsapp", "watsapp", "guasap", "wasap")) {

            abrirAplicacion(
                    new String[]{
                            "com.whatsapp",
                            "com.whatsapp.w4b"
                    },
                    "https://www.whatsapp.com"
            );

            return;
        }

        // YOUTUBE
        if (contiene(texto, "youtube", "you tube")) {

            abrirAplicacion(
                    new String[]{
                            "com.google.android.youtube"
                    },
                    "https://www.youtube.com"
            );

            return;
        }

        // CHROME
        if (contiene(texto, "chrome")) {

            abrirAplicacion(
                    new String[]{
                            "com.android.chrome"
                    },
                    "https://www.google.com"
            );

            return;
        }

        // INSTAGRAM
        if (contiene(texto, "instagram", "insta")) {

            abrirAplicacion(
                    new String[]{
                            "com.instagram.android"
                    },
                    "https://www.instagram.com"
            );

            return;
        }

        // FACEBOOK
        if (contiene(texto, "facebook", "facebook")) {

            abrirAplicacion(
                    new String[]{
                            "com.facebook.katana"
                    },
                    "https://www.facebook.com"
            );

            return;
        }

        // TIKTOK
        if (contiene(texto, "tiktok", "tik tok")) {

            abrirAplicacion(
                    new String[]{
                            "com.zhiliaoapp.musically"
                    },
                    "https://www.tiktok.com"
            );

            return;
        }

        // TELEGRAM
        if (contiene(texto, "telegram")) {

            abrirAplicacion(
                    new String[]{
                            "org.telegram.messenger"
                    },
                    "https://telegram.org"
            );

            return;
        }

        // SPOTIFY
        if (contiene(texto, "spotify")) {

            abrirAplicacion(
                    new String[]{
                            "com.spotify.music"
                    },
                    "https://open.spotify.com"
            );

            return;
        }

        // GMAIL
        if (contiene(texto, "gmail", "correo", "email")) {

            abrirAplicacion(
                    new String[]{
                            "com.google.android.gm"
                    },
                    "https://mail.google.com"
            );

            return;
        }

        // GOOGLE MAPS
        if (contiene(
                texto,
                "google maps",
                "maps",
                "mapas"
        )) {

            abrirAplicacion(
                    new String[]{
                            "com.google.android.apps.maps"
                    },
                    "https://maps.google.com"
            );

            return;
        }

        // CÁMARA
        if (contiene(
                texto,
                "cámara",
                "camara",
                "foto"
        )) {

            try {

                Intent intent =
                        new Intent(
                                "android.media.action.IMAGE_CAPTURE"
                        );

                startActivity(intent);

                statusText.setText(
                        "Abriendo cámara..."
                );

            } catch (Exception e) {

                statusText.setText(
                        "No pude abrir la cámara"
                );
            }

            return;
        }

        // CONTACTOS
        if (contiene(texto, "contactos", "contacto")) {

            abrirConfiguracion(
                    android.provider.ContactsContract.Contacts.CONTENT_URI,
                    "Abriendo contactos..."
            );

            return;
        }

        // TELÉFONO
        if (contiene(
                texto,
                "teléfono",
                "telefono",
                "llamadas",
                "llamada"
        )) {

            abrirAplicacion(
                    new String[]{
                            "com.google.android.dialer",
                            "com.android.dialer"
                    },
                    "tel:"
            );

            return;
        }

        // MENSAJES
        if (contiene(
                texto,
                "mensajes",
                "mensaje",
                "sms"
        )) {

            abrirAplicacion(
                    new String[]{
                            "com.google.android.apps.messaging",
                            "com.android.mms"
                    },
                    "sms:"
            );

            return;
        }

        // PLAY STORE
        if (contiene(
                texto,
                "play store",
                "playstore",
                "tienda"
        )) {

            abrirAplicacion(
                    new String[]{
                            "com.android.vending"
                    },
                    "https://play.google.com/store"
            );

            return;
        }

        // AJUSTES
        if (contiene(
                texto,
                "ajustes",
                "configuración",
                "configuracion"
        )) {

            try {

                Intent intent =
                        new Intent(Settings.ACTION_SETTINGS);

                startActivity(intent);

                statusText.setText(
                        "Abriendo ajustes..."
                );

            } catch (Exception e) {

                statusText.setText(
                        "No pude abrir los ajustes"
                );
            }

            return;
        }

        statusText.setText(
                "Todavía no conozco ese comando"
        );
    }

    private boolean contiene(
            String texto,
            String... palabras) {

        for (String palabra : palabras) {

            if (texto.contains(palabra)) {

                return true;
            }
        }

        return false;
    }

    private void abrirAplicacion(
            String[] paquetes,
            String fallbackUrl) {

        PackageManager packageManager =
                getPackageManager();

        for (String paquete : paquetes) {

            try {

                Intent intent =
                        packageManager.getLaunchIntentForPackage(
                                paquete
                        );

                if (intent != null) {

                    startActivity(intent);

                    statusText.setText(
                            "Abriendo aplicación..."
                    );

                    return;
                }

            } catch (Exception ignored) {
            }
        }

        // Solo llegamos aquí si la aplicación
        // no está instalada o Android no permite abrirla.
        abrirWeb(fallbackUrl);
    }

    private void abrirWeb(String url) {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            startActivity(intent);

            statusText.setText(
                    "Abriendo en el navegador..."
            );

        } catch (Exception e) {

            statusText.setText(
                    "No pude abrir la aplicación"
            );
        }
    }

    private void abrirConfiguracion(
            Uri uri,
            String mensaje) {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            uri
                    );

            startActivity(intent);

            statusText.setText(mensaje);

        } catch (Exception e) {

            statusText.setText(
                    "No pude abrir esa aplicación"
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_RECORD_AUDIO) {

            if (grantResults.length > 0 &&
                    grantResults[0]
                            == PackageManager.PERMISSION_GRANTED) {

                statusText.setText(
                        "Permiso concedido. LUXION está listo."
                );

                iniciarEscucha();

            } else {

                statusText.setText(
                        "Necesito permiso para usar el micrófono"
                );
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (speechRecognizer != null) {

            speechRecognizer.destroy();

            speechRecognizer = null;
        }

        super.onDestroy();
    }
}
