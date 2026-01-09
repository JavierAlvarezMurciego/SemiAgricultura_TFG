package com.javier.appcultivo.modelo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.github.mikephil.charting.charts.LineChart;
import com.javier.appcultivo.R;
import com.javier.appcultivo.firebase.FirestoreService;
import com.javier.appcultivo.ui.CultivoAdapter;
import com.javier.appcultivo.ui.NoticiasAdapter;

import java.util.ArrayList;
import java.util.List;

public class InicioFragmento extends Fragment {

    private RecyclerView recyclerView;
    private ViewPager2 viewPager2;
    private CultivoAdapter cultivoAdapter;
    private NoticiasAdapter noticiasAdapter;
    private List<Noticia> noticiaList = new ArrayList<>();
    private List<Cultivo> cultivoList = new ArrayList<>();
    private FirestoreService firestoreService;
    private LineChart lineChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmento_inicio, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCultivos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

//        lineChart = view.findViewById(R.id.graficaCultivo);
//        lineChart.setVisibility(View.GONE);

        cultivoAdapter = new CultivoAdapter(cultivoList,lineChart);
        recyclerView.setAdapter(cultivoAdapter);

        viewPager2 = view.findViewById(R.id.viewPagerNoticias);
        noticiasAdapter = new NoticiasAdapter(getContext(), noticiaList);
        viewPager2.setAdapter(noticiasAdapter);

        firestoreService = new FirestoreService();
        escucharCultivosEnTiempoReal();
        cargarUltimaFecha();
        cargarNoticias();

        return view;
    }

    private void cargarNoticias() {
        firestoreService.getNoticias(noticias -> {
            noticiaList.clear();
            noticiaList.addAll(noticias);
            noticiasAdapter.notifyDataSetChanged();
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
            getActivity().runOnUiThread(() -> cultivoAdapter.notifyDataSetChanged());
        });
    }
}

