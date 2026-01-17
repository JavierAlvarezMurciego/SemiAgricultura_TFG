package com.javier.appcultivo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class ActividadSplash extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle guardarInstancia){
        super.onCreate(guardarInstancia);
        setContentView(R.layout.splash_actividad);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent intent = new Intent(ActividadSplash.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }, 2000);
    }
}
