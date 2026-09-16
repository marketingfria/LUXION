package com.luxion.assistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FloatingService extends Service {

    private static final String CHANNEL_ID =
            "LUXION_SERVICE";

    private WindowManager windowManager;
    private TextView floatingButton;

    private WindowManager.LayoutParams params;

    private float toqueInicialX;
    private float toqueInicialY;

    private int posicionInicialX;
    private int posicionInicialY;

    private boolean seEstaMoviendo = false;

    @Override
    public void onCreate() {
        super.onCreate();

        crearCanalNotificacion();
        iniciarServicioForeground();

        windowManager =
                (WindowManager) getSystemService(
                        WINDOW_SERVICE
                );

        crearBotonFlotante();
    }

    private void crearCanalNotificacion() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "LUXION",
                            NotificationManager.IMPORTANCE_LOW
                    );

            channel.setDescription(
                    "Servicio del asistente LUXION"
            );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {
                manager.createNotificationChannel(
                        channel
                );
            }
        }
    }

    private void iniciarServicioForeground() {

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

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
                .setContentTitle(
                        "LUXION activo"
                )
                .setContentText(
                        "Toca la L para hablar con LUXION"
                )
                .setSmallIcon(
                        android.R.drawable.ic_btn_speak_now
                )
                .setOngoing(true);

        startForeground(
                1001,
                builder.build()
        );
    }

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
                Color.rgb(30, 30, 35)
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

    private void activarMicrofono() {

        Intent intent =
                new Intent(
                        this,
                        MainActivity.class
                );

        intent.putExtra(
                "LUXION_ESCCHAR",
                true
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        try {

            startActivity(intent);

        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroy() {

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

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
