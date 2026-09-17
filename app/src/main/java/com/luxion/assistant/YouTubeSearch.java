package com.luxion.assistant;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class YouTubeSearch {

    private YouTubeSearch() {
        // Clase de utilidad.
    }

    /**
     * Realiza una búsqueda libre en YouTube.
     *
     * Ejemplos:
     *
     * "pasión winne"
     * "GTA 6"
     * "noticias de criptomonedas"
     * "cómo reparar un teléfono"
     * "música para entrenar"
     *
     * No utiliza categorías ni una lista limitada.
     */
    public static boolean buscar(
            Context context,
            String busqueda) {

        if (context == null) {
            return false;
        }

        if (busqueda == null) {
            return false;
        }

        String texto =
                busqueda.trim();

        if (texto.isEmpty()) {
            return false;
        }

        /*
         * Construimos directamente la búsqueda
         * de YouTube con TODO el texto recibido.
         */
        String url =
                "https://www.youtube.com/results?search_query="
                        + Uri.encode(texto);

        /*
         * Primero intentamos abrir la aplicación
         * oficial de YouTube.
         */
        try {

            Intent youtube =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            youtube.setPackage(
                    "com.google.android.youtube"
            );

            youtube.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(youtube);

            return true;

        } catch (Exception ignored) {
        }

        /*
         * Si YouTube no está instalado o no puede
         * abrirse directamente, utilizamos el navegador.
         */
        try {

            Intent navegador =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            navegador.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(navegador);

            return true;

        } catch (Exception ignored) {

            return false;
        }
    }
}
