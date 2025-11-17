package com.javier.appcultivo;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.javier.appcultivo.firebase.FirestoreService;
import com.javier.appcultivo.modelo.Cultivo;
import com.javier.appcultivo.modelo.Noticia;
import com.javier.appcultivo.ui.CultivoAdapter;
import com.javier.appcultivo.ui.NoticiasAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ViewPager2 viewPager2;
//    private Handler sliderHandler = new Handler();
//    private Runnable sliderRunnable;
    private CultivoAdapter cultivoAdapter;
    private NoticiasAdapter noticiasAdapter;
    private List<Noticia> noticiaList = new ArrayList<>();
    private List<Cultivo> cultivoList = new ArrayList<>();
    private FirestoreService firestoreService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Asegurar la inicialización cuando lo intentaba con el archivo.json
//        if (FirebaseApp.getApps(this).isEmpty()) {
//            FirebaseApp.initializeApp(this);
//            Log.d("MainActivity", "Firebase inicializado manualmente en MainActivity");
//        }
        setContentView(R.layout.activity_main);

        firestoreService = new FirestoreService();

        recyclerView = findViewById(R.id.recyclerViewCultivos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Inicializo el adapter con la lista de cultivos vacía
        cultivoAdapter = new CultivoAdapter(cultivoList);
        recyclerView.setAdapter(cultivoAdapter);

        viewPager2 = findViewById(R.id.viewPagerNoticias);
        // Inicializo el adapter con la lista de noticias vacía
        noticiasAdapter = new NoticiasAdapter(this, noticiaList);
        viewPager2.setAdapter(noticiasAdapter);

        // Auto-deslizamiento de noticias
//        sliderHandler = new Handler();
//        sliderRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (noticiaList.size() > 0) {
//                    int nextItem = (viewPager2.getCurrentItem() + 1) % noticiaList.size();
//                    viewPager2.setCurrentItem(nextItem, true);
//                    sliderHandler.postDelayed(this, 5000); // cambia cada 5 segundos
//                }
//            }
//        };
//        sliderHandler.postDelayed(sliderRunnable, 5000);

        escucharCultivosEnTiempoReal();
        cargarUltimaFecha();
        cargarNoticias();
    }

    private void cargarNoticias() {
        firestoreService.getNoticias(new FirestoreService.NoticiasCallBack() {
            @Override
            public void onCallback(List<Noticia> noticias) {
                // Actualiza los datos del adapter
                noticiasAdapter.setNoticias(noticias);
                // Notifica al RecyclerView que los datos han cambiado
                noticiasAdapter.notifyDataSetChanged();
            }
        });
    }

    private void cargarUltimaFecha() {
        firestoreService.cargarUltimaFecha(cultivos -> {
            // Actualiza el adapter con los datos recibidos
            cultivoAdapter.setCultivos(cultivos);
        });
    }
    private void escucharCultivosEnTiempoReal() {
        firestoreService.cargarUltimaFecha(cultivos -> {
            cultivoList.clear();
            cultivoList.addAll(cultivos);
            runOnUiThread(() -> cultivoAdapter.notifyDataSetChanged());
            Log.d("MainActivity", "Escuchando cultivos: " + cultivos.size() + " documentos.");
        });
    }

}
