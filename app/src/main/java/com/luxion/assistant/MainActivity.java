package com.luxion.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Contenedor principal
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setPadding(40, 40, 40, 40);
        mainLayout.setBackgroundColor(Color.rgb(10, 10, 18));

        // Título
        TextView title = new TextView(this);
        title.setText("LUXION");
        title.setTextSize(42);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        // Subtítulo
        TextView subtitle = new TextView(this);
        subtitle.setText("AI Android Assistant");
        subtitle.setTextSize(18);
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER);

        // Estado
        statusText = new TextView(this);
        statusText.setText("Pulsa el botón para hablar");
        statusText.setTextSize(17);
        statusText.setTextColor(Color.LTGRAY);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 60, 0, 30);

        // Botón
        Button voiceButton = new Button(this);
        voiceButton.setText("🎙  ACTIVAR LUXION");
        voiceButton.setTextSize(16);

        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusText.setText("LUXION está listo...");
            }
        });

        // Añadir elementos
        mainLayout.addView(title);
        mainLayout.addView(subtitle);
        mainLayout.addView(statusText);
        mainLayout.addView(voiceButton);

        // Mostrar interfaz
        setContentView(mainLayout);
    }
}
