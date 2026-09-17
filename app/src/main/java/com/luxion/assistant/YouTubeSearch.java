package com.luxion.assistant;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Locale;
import java.util.Random;

public final class YouTubeSearch {

    private YouTubeSearch() {
        // Clase de utilidad.
    }

    /**
     * Recibe una petición y la convierte en una búsqueda de YouTube.
     *
     * Ejemplos:
     *
     * "busca GTA 6 en YouTube"
     * "quiero ver videos de gatos"
     * "quiero escuchar música de Bad Bunny"
     * "búscame un tutorial de Android"
     * "pon canciones de salsa"
     */
    public static boolean buscar(Context context, String peticion) {

        if (context == null || peticion == null) {
            return false;
        }

        String texto = limpiar(peticion);

        if (texto.isEmpty()) {
            return false;
        }

        String busqueda = extraerBusqueda(texto);

        if (busqueda.isEmpty()) {
            busqueda = categoriaAleatoria();
        }

        return abrirYouTube(context, busqueda);
    }

    /**
     * Extrae el tema que realmente quiere buscar el usuario.
     *
     * No depende de un tema concreto.
     */
    private static String extraerBusqueda(String texto) {

        String resultado = texto;

        // Elimina "youtube".
        resultado = reemplazar(resultado, "youtube");

        // Frases comunes que indican una búsqueda.
        String[] frases = {
                "quiero que busques",
                "quiero buscar",
                "quiero que busque",
                "me puedes buscar",
                "puedes buscar",
                "puedes buscarme",
                "buscame",
                "búscame",
                "buscarme",
                "busca",
                "buscar",
                "quiero ver",
                "quiero escuchar",
                "quiero mirar",
                "me gustaría ver",
                "me gustaria ver",
                "me gustaría escuchar",
                "me gustaria escuchar",
                "ponme",
                "pon",
                "muéstrame",
                "muestrame"
        };

        for (String frase : frases) {
            resultado = reemplazar(resultado, frase);
        }

        // Elimina conectores que no forman parte de la búsqueda.
        String[] conectores = {
                "en ",
                "por favor",
                "porfavor",
                "un video de",
                "un vídeo de",
                "videos de",
                "vídeos de",
                "video de",
                "vídeo de",
                "videos sobre",
                "vídeos sobre",
                "video sobre",
                "vídeo sobre",
                "algo sobre",
                "algo de",
                "una canción de",
                "una cancion de",
                "canciones de"
        };

        for (String conector : conectores) {
            resultado = reemplazar(resultado, conector);
        }

        resultado = limpiar(resultado);

        // Evita búsquedas sin contenido.
        if (esVacio(resultado)) {
            return "";
        }

        return resultado;
    }

    /**
     * Abre directamente los resultados de YouTube.
     */
    private static boolean abrirYouTube(Context context, String busqueda) {

        try {

            String url =
                    "https://www.youtube.com/results?search_query="
                            + Uri.encode(busqueda);

            /*
             * Primero intentamos abrir la aplicación oficial.
             */
            Intent youtube = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
            );

            youtube.setPackage("com.google.android.youtube");

            youtube.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(youtube);

            return true;

        } catch (Exception ignored) {
            /*
             * YouTube no está instalado o no puede abrirse
             * mediante el Intent específico.
             */
        }

        /*
         * Como alternativa, abrimos YouTube mediante
         * el navegador.
         */
        try {

            String url =
                    "https://www.youtube.com/results?search_query="
                            + Uri.encode(busqueda);

            Intent navegador = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
            );

            navegador.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(navegador);

            return true;

        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Si el usuario no especifica ningún tema,
     * LUXION puede elegir uno automáticamente.
     */
    private static String categoriaAleatoria() {

        String[] categorias = {

                "música nueva",
                "música para entrenar",
                "reguetón",
                "salsa",
                "bachata",
                "rock",
                "electrónica",
                "hip hop",

                "GTA 6",
                "GTA 5",
                "Minecraft",
                "videojuegos",

                "fútbol",
                "baloncesto",
                "deportes",

                "humor",
                "videos divertidos",
                "animales",
                "curiosidades",

                "tecnología",
                "Android",
                "inteligencia artificial",

                "viajes",
                "naturaleza",
                "documentales",

                "recetas",
                "cocina"
        };

        Random random = new Random();

        return categorias[
                random.nextInt(categorias.length)
        ];
    }

    /**
     * Comprueba si no existe una búsqueda real.
     */
    private static boolean esVacio(String texto) {

        if (texto == null || texto.trim().isEmpty()) {
            return true;
        }

        String valor = texto.toLowerCase(Locale.ROOT).trim();

        return valor.equals("algo")
                || valor.equals("un video")
                || valor.equals("un vídeo")
                || valor.equals("videos")
                || valor.equals("vídeos")
                || valor.equals("video")
                || valor.equals("vídeo")
                || valor.equals("cualquier cosa")
                || valor.equals("cualquier cosa en youtube");
    }

    /**
     * Reemplazo seguro ignorando mayúsculas/minúsculas.
     */
    private static String reemplazar(
            String texto,
            String objetivo
    ) {

        return texto.replace(
                objetivo.toLowerCase(Locale.ROOT),
                " "
        );
    }

    /**
     * Limpia espacios y signos innecesarios.
     */
    private static String limpiar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .toLowerCase(Locale.ROOT)
                .replace("¿", " ")
                .replace("?", " ")
                .replace("¡", " ")
                .replace("!", " ")
                .replace(",", " ")
                .replace(".", " ")
                .replace(";", " ")
                .replace(":", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
