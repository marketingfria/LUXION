package com.luxion.assistant;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Locale;
import java.util.Random;

public final class YouTubeSearch {

    private YouTubeSearch() {
        // Evita crear objetos de esta clase.
    }

    /**
     * Procesa una orden relacionada con YouTube.
     *
     * Ejemplos:
     * "busca música"
     * "busca reguetón"
     * "busca GTA 5"
     * "busca videos de animales"
     * "quiero ver fútbol"
     * "pon música"
     * "abre youtube"
     */
    public static boolean ejecutar(Context context, String texto) {

        if (context == null || texto == null) {
            return false;
        }

        String comando = normalizar(texto);

        if (comando.isEmpty()) {
            return false;
        }

        // Detectar si la orden está relacionada con YouTube.
        if (!esOrdenYouTube(comando)) {
            return false;
        }

        String busqueda = obtenerBusqueda(comando);

        abrirYouTube(context, busqueda);

        return true;
    }

    /**
     * Determina si el usuario está hablando de YouTube.
     */
    private static boolean esOrdenYouTube(String comando) {

        return comando.contains("youtube")
                || comando.contains("busca")
                || comando.contains("buscar")
                || comando.contains("buscame")
                || comando.contains("búscame")
                || comando.contains("pon ")
                || comando.startsWith("poner ")
                || comando.contains("quiero ver")
                || comando.contains("quiero escuchar")
                || comando.contains("videos")
                || comando.contains("video");
    }

    /**
     * Obtiene aquello que el usuario quiere buscar.
     */
    private static String obtenerBusqueda(String comando) {

        String texto = comando;

        // Elimina la palabra YouTube.
        texto = texto.replace("youtube", " ");

        // Frases que indican una búsqueda.
        String[] palabras = {
                "busca",
                "buscar",
                "buscame",
                "búscame",
                "quiero buscar",
                "quiero ver",
                "quiero escuchar",
                "pon",
                "poner",
                "muéstrame",
                "muestrame"
        };

        for (String palabra : palabras) {
            texto = texto.replace(palabra, " ");
        }

        // Limpieza de palabras innecesarias.
        texto = texto.replace("en ", " ")
                .replace("videos de ", " ")
                .replace("video de ", " ")
                .replace("videos ", " ")
                .replace("video ", " ")
                .replace("algo de ", " ")
                .replace("alguna ", " ")
                .replace("algún ", " ")
                .replace("algun ", " ")
                .replace("un video ", " ")
                .replace("un vídeo ", " ");

        texto = limpiarEspacios(texto);

        /*
         * Si el usuario solamente dijo:
         *
         * "busca"
         * "busca un video"
         * "pon algo"
         * "quiero ver videos"
         *
         * elegimos una categoría automáticamente.
         */
        if (esBusquedaVacia(texto)) {
            return categoriaAleatoria();
        }

        return texto;
    }

    /**
     * Detecta si realmente no se especificó qué buscar.
     */
    private static boolean esBusquedaVacia(String texto) {

        if (texto == null || texto.trim().isEmpty()) {
            return true;
        }

        String limpio = texto.toLowerCase(Locale.ROOT).trim();

        return limpio.equals("un")
                || limpio.equals("una")
                || limpio.equals("algo")
                || limpio.equals("algo ")
                || limpio.equals("video")
                || limpio.equals("videos")
                || limpio.equals("vídeo")
                || limpio.equals("vídeos")
                || limpio.equals("quiero")
                || limpio.equals("ver")
                || limpio.equals("escuchar");
    }

    /**
     * Selecciona una categoría cuando el usuario no especifica ninguna.
     */
    private static String categoriaAleatoria() {

        String[] categorias = {

                "música popular",
                "música para entrenar",
                "reguetón",
                "salsa",
                "bachata",
                "rock",
                "electrónica",
                "hip hop",
                "videos divertidos",
                "humor",
                "animales divertidos",
                "fútbol",
                "baloncesto",
                "videojuegos",
                "GTA 5",
                "Minecraft",
                "tecnología",
                "Android",
                "curiosidades",
                "ciencia",
                "viajes",
                "naturaleza",
                "películas",
                "trailers de películas",
                "documentales",
                "recetas fáciles"
        };

        Random random = new Random();

        return categorias[random.nextInt(categorias.length)];
    }

    /**
     * Abre la aplicación de YouTube si está instalada.
     * Si no está instalada, abre YouTube en el navegador.
     */
    private static void abrirYouTube(Context context, String busqueda) {

        String consulta = busqueda == null
                ? ""
                : busqueda.trim();

        try {

            /*
             * Intent específico para la aplicación YouTube.
             */
            Intent appIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                            "https://www.youtube.com/results?search_query="
                                    + Uri.encode(consulta)
                    )
            );

            appIntent.setPackage("com.google.android.youtube");

            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(appIntent);

            return;

        } catch (Exception ignored) {
            // YouTube no está disponible.
        }

        /*
         * Si no existe la aplicación YouTube,
         * utilizamos el navegador.
         */
        try {

            Intent navegador = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                            "https://www.youtube.com/results?search_query="
                                    + Uri.encode(consulta)
                    )
            );

            navegador.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(navegador);

        } catch (Exception ignored) {
            // No se pudo abrir YouTube ni navegador.
        }
    }

    /**
     * Normaliza el texto reconocido por el micrófono.
     */
    private static String normalizar(String texto) {

        return limpiarEspacios(
                texto.toLowerCase(Locale.ROOT)
                        .replace("¿", " ")
                        .replace("?", " ")
                        .replace("¡", " ")
                        .replace("!", " ")
                        .trim()
        );
    }

    /**
     * Elimina espacios repetidos.
     */
    private static String limpiarEspacios(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replaceAll("\\s+", " ")
                .trim();
    }
}
