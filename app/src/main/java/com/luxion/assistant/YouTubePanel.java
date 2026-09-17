package com.luxion.assistant;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class YouTubePanel {

    private YouTubePanel() {
        // Clase de utilidad.
    }

    /**
     * Crea el panel de búsqueda de YouTube.
     *
     * No necesita XML.
     */
    public static View crear(Context context) {

        LinearLayout panel = new LinearLayout(context);

        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        panel.setPadding(30, 30, 30, 30);
        panel.setBackgroundColor(Color.rgb(16, 16, 20));

        // TÍTULO
        TextView titulo = new TextView(context);

        titulo.setText("▶  YOUTUBE");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(28);
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titulo.setGravity(Gravity.CENTER);

        panel.addView(
                titulo,
                parametros(0, 10)
        );

        // DESCRIPCIÓN
        TextView descripcion = new TextView(context);

        descripcion.setText(
                "Busca cualquier tema en YouTube"
        );

        descripcion.setTextColor(Color.LTGRAY);
        descripcion.setTextSize(16);
        descripcion.setGravity(Gravity.CENTER);

        panel.addView(
                descripcion,
                parametros(0, 15)
        );

        // CAMPO DE BÚSQUEDA
        EditText busqueda = new EditText(context);

        busqueda.setHint(
                "Escribe lo que quieras buscar..."
        );

        busqueda.setHintTextColor(Color.GRAY);
        busqueda.setTextColor(Color.WHITE);
        busqueda.setTextSize(17);
        busqueda.setSingleLine(true);
        busqueda.setPadding(25, 18, 25, 18);
        busqueda.setBackgroundColor(
                Color.rgb(35, 35, 42)
        );

        panel.addView(
                busqueda,
                parametros(0, 12)
        );

        // BOTÓN BUSCAR
        Button botonBuscar = boton(
                context,
                "🔎  BUSCAR EN YOUTUBE"
        );

        botonBuscar.setOnClickListener(v -> {

            String texto = busqueda
                    .getText()
                    .toString()
                    .trim();

            if (texto.isEmpty()) {

                busqueda.setError(
                        "Escribe algo para buscar"
                );

                busqueda.requestFocus();

                return;
            }

            ocultarTeclado(
                    context,
                    busqueda
            );

            YouTubeSearch.buscar(
                    context,
                    texto
            );
        });

        panel.addView(
                botonBuscar,
                parametros(0, 10)
        );

        // SORPRÉNDEME
        Button botonAleatorio = boton(
                context,
                "🎲  SORPRÉNDEME"
        );

        botonAleatorio.setOnClickListener(v -> {

            ocultarTeclado(
                    context,
                    busqueda
            );

            YouTubeSearch.buscar(
                    context,
                    "busca"
            );
        });

        panel.addView(
                botonAleatorio,
                parametros(0, 20)
        );

        // SEPARADOR
        TextView separador = new TextView(context);

        separador.setText(
                "── CATEGORÍAS ──"
        );

        separador.setTextColor(Color.GRAY);
        separador.setGravity(Gravity.CENTER);
        separador.setTextSize(14);

        panel.addView(
                separador,
                parametros(0, 10)
        );

        // CATEGORÍAS
        agregarCategoria(
                context,
                panel,
                "🎵  Música",
                "música nueva"
        );

        agregarCategoria(
                context,
                panel,
                "🎮  Videojuegos",
                "videojuegos"
        );

        agregarCategoria(
                context,
                panel,
                "⚽  Deportes",
                "deportes"
        );

        agregarCategoria(
                context,
                panel,
                "😂  Humor",
                "humor"
        );

        agregarCategoria(
                context,
                panel,
                "💻  Tecnología",
                "tecnología"
        );

        agregarCategoria(
                context,
                panel,
                "📰  Noticias",
                "noticias"
        );

        agregarCategoria(
                context,
                panel,
                "🌎  Curiosidades",
                "curiosidades"
        );

        agregarCategoria(
                context,
                panel,
                "🎬  Películas",
                "películas y trailers"
        );

        return panel;
    }

    /**
     * Crea un botón del panel.
     */
    private static Button boton(
            Context context,
            String texto
    ) {

        Button boton = new Button(context);

        boton.setText(texto);
        boton.setTextColor(Color.WHITE);
        boton.setTextSize(16);
        boton.setAllCaps(false);
        boton.setPadding(
                15,
                15,
                15,
                15
        );

        return boton;
    }

    /**
     * Añade una categoría.
     */
    private static void agregarCategoria(
            Context context,
            LinearLayout panel,
            String nombre,
            String busqueda
    ) {

        Button boton = boton(
                context,
                nombre
        );

        boton.setOnClickListener(v ->
                YouTubeSearch.buscar(
                        context,
                        busqueda
                )
        );

        panel.addView(
                boton,
                parametros(0, 5)
        );
    }

    /**
     * Parámetros de tamaño y margen.
     */
    private static LinearLayout.LayoutParams parametros(
            int margenHorizontal,
            int margenVertical
    ) {

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(
                margenHorizontal,
                margenVertical,
                margenHorizontal,
                margenVertical
        );

        return parametros;
    }

    /**
     * Oculta el teclado después de realizar una búsqueda.
     */
    private static void ocultarTeclado(
            Context context,
            View vista
    ) {

        try {

            InputMethodManager teclado =
                    (InputMethodManager)
                            context.getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                            );

            if (teclado != null) {

                teclado.hideSoftInputFromWindow(
                        vista.getWindowToken(),
                        0
                );
            }

        } catch (Exception ignored) {
        }
    }
}
