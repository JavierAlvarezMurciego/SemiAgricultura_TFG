package com.javier.appcultivo.modelo;

import android.annotation.SuppressLint;
import android.graphics.Color;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.javier.appcultivo.R;
import com.javier.appcultivo.firebase.FirestoreService;
import com.javier.appcultivo.ui.CultivoAdapter;
import com.javier.appcultivo.ui.NoticiasAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MercadoFragmento extends Fragment {

    private RecyclerView recyclerView;
    private CultivoAdapter cultivoAdapter;
    private List<Cultivo> cultivoList = new ArrayList<>();
    private FirestoreService firestoreService;
    private LineChart lineChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Cargar lo que tenga la interfaz gráfica de Mercado (cambiar lo de activity_main)
        View view = inflater.inflate(R.layout.fragmento_mercado, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCultivos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

//        lineChart = viewGrafica.findViewById(R.id.graficaCultivo);
//        lineChart.setVisibility(View.GONE);

        cultivoAdapter = new CultivoAdapter(cultivoList,lineChart);
        recyclerView.setAdapter(cultivoAdapter);

        firestoreService = new FirestoreService();
        escucharCultivosEnTiempoReal();

        return view;
    }


    private void escucharCultivosEnTiempoReal() {
        firestoreService.cargarUltimaFecha(cultivos -> {
            cultivoList.clear();
            cultivoList.addAll(cultivos);
            getActivity().runOnUiThread(() -> cultivoAdapter.notifyDataSetChanged());
        });
    }
}
