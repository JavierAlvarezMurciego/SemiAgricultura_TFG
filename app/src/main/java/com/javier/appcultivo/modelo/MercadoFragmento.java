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

import com.javier.appcultivo.R;
import com.javier.appcultivo.firebase.FirestoreService;
import com.javier.appcultivo.ui.CultivoAdapter;

import java.util.ArrayList;
import java.util.List;

public class MercadoFragmento extends Fragment {

    private RecyclerView recyclerView;
    private CultivoAdapter cultivoAdapter;
    private List<Cultivo> cultivoList = new ArrayList<>();
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Cargar lo que tenga la interfaz gráfica de Mercado
        View view = inflater.inflate(R.layout.fragmento_mercado, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCultivosFavoritos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cultivoAdapter = new CultivoAdapter(cultivoList);
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
