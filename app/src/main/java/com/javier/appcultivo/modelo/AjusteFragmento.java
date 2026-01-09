package com.javier.appcultivo.modelo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.javier.appcultivo.R;

public class AjusteFragmento extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Cargar lo que tenga la interfaz gráfica de Ajustes (cambiar lo de activity_main)
        return inflater.inflate(R.layout.fragmento_inicio, container, false);
    }
}
