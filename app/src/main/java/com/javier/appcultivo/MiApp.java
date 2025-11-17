package com.javier.appcultivo;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class MiApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        if (FirebaseApp.getApps(this).isEmpty()) {
            // La tengo que iniciar manualmente porque desde mi archivo.json no me deja y me da error
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId(BuildConfig.APPLICATION_ID)
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    //.setDatabaseUrl() // Opcional si usase Base de datos en tiempo real
                    .build();

            FirebaseApp.initializeApp(this, options);
            Log.d("MiApp", "FirebaseApp inicializada manualmente");
        } else {
            Log.d("MiApp", "FirebaseApp ya estaba inicializada");
        }
    }
}
