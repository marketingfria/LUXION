package com.luxion.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.graphics.Color;
import android.graphics.Typeface;
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

        // Si el permiso ya fue concedido anteriormente,
        // no se vuelve a solicitar.
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

        // -------------------------
        // TÍTULO
        // -------------------------

        TextView title = new TextView(this);

        title.setText("LUXION");
        title.setTextSize(42);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        // -------------------------
        // SUBTÍTULO
        // -------------------------

        TextView subtitle = new TextView(this);

        subtitle.setText("AI Android Assistant");
        subtitle.setTextSize(18);
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER);

        // -------------------------
        // ESTADO
        // -------------------------

        statusText = new TextView(this);

        statusText.setText("Preparando LUXION...");
        statusText.setTextSize(17);
        statusText.setTextColor(Color.LTGRAY);
        statusText.setGravity(Gravity.CENTER);

        statusText.setPadding(0, 50, 0, 25);

        // -------------------------
        // RESULTADO DE VOZ
        // -------------------------

        resultText = new TextView(this);

        resultText.setText("Aquí aparecerá lo que digas");
        resultText.setTextSize(20);
        resultText.setTextColor(Color.WHITE);
        resultText.setGravity(Gravity.CENTER);

        resultText.setPadding(20, 20, 20, 30);

        // -------------------------
        // BOTÓN
        // -------------------------

        Button voiceButton = new Button(this);

        voiceButton.setText("🎙  ACTIVAR LUXION");
        voiceButton.setTextSize(16);

        voiceButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                activarMicrofono();
            }
        });

        // -------------------------
        // AGREGAR ELEMENTOS
        // -------------------------

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

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

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
                        // No necesitamos utilizar el nivel de sonido.
                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {
                        // No necesitamos utilizar el audio directamente.
                    }

                    @Override
                    public void onEndOfSpeech() {

                        listening = false;

                        statusText.setText(
                                "Procesando voz..."
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

                            case SpeechRecognizer.ERROR_CLIENT:
                                mensaje = "Error del reconocimiento";
                                break;

                            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                                mensaje = "Permiso de micrófono no concedido";
                                break;

                            case SpeechRecognizer.ERROR_NETWORK:
                                mensaje = "Error de red";
                                break;

                            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                                mensaje = "Tiempo de espera agotado";
                                break;

                            case SpeechRecognizer.ERROR_NO_MATCH:
                                mensaje = "No entendí lo que dijiste";
                                break;

                            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                                mensaje = "El reconocimiento está ocupado";
                                break;

                            case SpeechRecognizer.ERROR_SERVER:
                                mensaje = "Error del servicio de voz";
                                break;

                            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                                mensaje = "No detecté tu voz";
                                break;

                            default:
                                mensaje = "No pude reconocer la voz";
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

                        if (resultados != null &&
                                !resultados.isEmpty()) {

                            String texto = resultados.get(0);

                            resultText.setText(
                                    "Tú dijiste:\n" + texto
                            );

                            statusText.setText(
                                    "LUXION te ha escuchado"
                            );

                        } else {

                            statusText.setText(
                                    "No se recibió ninguna frase"
                            );
                        }
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {

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
                        // No necesitamos eventos adicionales.
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

        // Comprobar permiso
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
                    "El reconocimiento de voz no está disponible"
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

                // Después de conceder el permiso,
                // iniciamos el reconocimiento.
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
