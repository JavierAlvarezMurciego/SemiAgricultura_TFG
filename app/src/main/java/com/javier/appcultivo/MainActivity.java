package com.javier.appcultivo;

import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.javier.appcultivo.modelo.AjusteFragmento;
import com.javier.appcultivo.modelo.InicioFragmento;
import com.javier.appcultivo.modelo.MercadoFragmento;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_main2);

        LinearLayout barra = findViewById(R.id.barra_menu);

        // Las tres pantallas
        LinearLayout itemInicio = (LinearLayout) barra.getChildAt(0);
        LinearLayout itemMercado = (LinearLayout) barra.getChildAt(1);
        LinearLayout itemAjustes = (LinearLayout) barra.getChildAt(2);

        // Los click listeners
        itemInicio.setOnClickListener(v -> reemplazarFragmento(new InicioFragmento()));
        itemMercado.setOnClickListener(v -> reemplazarFragmento(new MercadoFragmento()));
        itemAjustes.setOnClickListener(v -> reemplazarFragmento(new AjusteFragmento()));

        // Fragmento inicial
        reemplazarFragmento(new InicioFragmento());
    }

    private void reemplazarFragmento(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmento_contenedor, fragment)
                .commit();
    }
}