package com.javier.appcultivo.modelo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
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
import java.util.stream.Collectors;

public class InicioFragmento extends Fragment {

    private RecyclerView recyclerView;
    private ViewPager2 viewPager2;
    private CultivoAdapter cultivoAdapter;
    private NoticiasAdapter noticiasAdapter;
    private List<Noticia> noticiaList = new ArrayList<>();
    private List<Cultivo> cultivoList = new ArrayList<>();
    private List<Cultivo> cultivoListFav = new ArrayList<>();
    private FirestoreService firestoreService;
    private Handler handler;
    private Runnable runnable;
    private boolean estado;
    private LinearLayout layoutPuntos;
    private ImageView[] puntos;
    private Button bAnyadirCultivoFav;
    private ActivityResultLauncher <Intent> actualizarGrafFav;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Cargar lo que tenga la interfaz gráfica de Inicio
        View view = inflater.inflate(R.layout.fragmento_inicio, container, false);

        // Configuro el recyclerview de favoritos
        recyclerView = view.findViewById(R.id.recyclerViewCultivosFavoritos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cultivoAdapter = new CultivoAdapter(cultivoListFav);
        recyclerView.setAdapter(cultivoAdapter);

        // Configuro el viewpager2 para mostrar las noticias
        viewPager2 = view.findViewById(R.id.viewPagerNoticias);
        noticiasAdapter = new NoticiasAdapter(getContext(), noticiaList);
        viewPager2.setAdapter(noticiasAdapter);

        bAnyadirCultivoFav = view.findViewById(R.id.bCultFav);

        layoutPuntos = view.findViewById(R.id.puntitos);

        firestoreService = new FirestoreService();

        // Actualizo los favoritos si se elimina un cultivo
        actualizarGrafFav= registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if(data != null && data.hasExtra("cultivoEliminado")){
                            String eliminado = data.getStringExtra("cultivoEliminado");
                            eliminarFavorito(eliminado);
                        }
                    }
                }
        );

        escucharCultivosEnTiempoReal();
        cargarNoticias();

        setPuntos(); // Antes de movimientoNoticas para que carguen mis puntos (drawable)
        movimientoNoticias();

        bAnyadirCultivoFav.setOnClickListener(v -> mostrarDialogoCultivos()); // Acción al btn para mostrar los cult

        return view;
    }

    private void cargarNoticias() {
        // Cargo las noticias al servicio de firestore de forma asíncrona
        firestoreService.getNoticias(noticias -> {
            noticiaList.clear(); // Limpio la lista actual para evitar duplicados
            noticiaList.addAll(noticias); // Añado las noticias desde firebase
            noticiasAdapter.notifyDataSetChanged(); // Notifico al adaptador para que se actualize la interfaz con los nuevos datos
            anyadirIndPuntos(); // Inicializo los indicadores visuales (puntitos)
            setPuntoActual(0); // Pongo el puntito el primero
        });
    }

    private void escucharCultivosEnTiempoReal() {
        // Cargo los cultivos al servicio de firestore de forma asíncrona
        firestoreService.cargarUltimaFecha(cultivos -> {
            cultivoList.clear(); // Limpio la lista actual para evitar duplicados
            cultivoList.addAll(cultivos); // Añado las cultivos desde firebase
            cargarFavoritos();
        });
    }

    private void movimientoNoticias() {
        // Configuro el auto-deslizamiento de noticias
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (noticiaList.size() > 0) {
                    int nextItem = (viewPager2.getCurrentItem() + 1) % noticiaList.size();
                    viewPager2.setCurrentItem(nextItem, true); // Cambio la noticia con animación
                    handler.postDelayed(this, 7000); // cambia cada 7 segundos
                }
            }
        };

        // Espero a que se termine de dibujar
        viewPager2.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            RecyclerView recycler = (RecyclerView) viewPager2.getChildAt(0);

            if (recycler != null) {
                for (int i = 0; i < recycler.getChildCount(); i++) {
                    View item = recycler.getChildAt(i);
                    if (item.getTag() == null) { // Compruebo que no haya listeners
                        item.setTag(true); // Marco el item para no repetirlo
                        setTouchListenerRecursivo(item); // Aplico el listener recursivamente a item y todos sus hijos
                    }
                }
            }
        });

        empezarMovNoticia();
    }

    // Método recursivo para aplicar OnTouchListener a todos los hijos de un View
    private void setTouchListenerRecursivo(View v) {
        v.setOnTouchListener((view, e) -> {
            int accion = e.getActionMasked(); // Obtengo la opción que el usuario está realizando
            if (accion == MotionEvent.ACTION_DOWN || accion == MotionEvent.ACTION_MOVE) {
                if (!estado) {
                    estado = true;
                    pararMovNotica();
                }
            } else if (accion == MotionEvent.ACTION_UP || accion == MotionEvent.ACTION_CANCEL) {
                estado = false;
                empezarMovNoticia();
            }
            return false; // permite que el click normal del item siga funcionando
        });

        // Aplico el listener al grupo
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            // Recorro cada hijo y le llamo de forma recursiva
            for (int i = 0; i < vg.getChildCount(); i++) {
                setTouchListenerRecursivo(vg.getChildAt(i));
            }
        }
    }
    private void empezarMovNoticia(){
        handler.removeCallbacks(runnable); // Elimino cualquier llamada para evitar duplicados
        handler.postDelayed(runnable, 7000); // Programo el auto-deslizamiento cada 7 seg
    }

    private void pararMovNotica(){
        handler.removeCallbacks(runnable);
    } // Lo detengo eliminando el handler

    private void setPuntos() {
        // Registro un listener para detectar el cambio de página
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int posicion) {
                setPuntoActual(posicion); // Actualizo los puntitos
            }
        });
    }

    private void anyadirIndPuntos() {
        if (noticiaList.isEmpty()) return;

        puntos = new ImageView[noticiaList.size()]; // Creo un array para los puntitos, uno por noticia
        layoutPuntos.removeAllViews(); // Limpio los puntitos

        // Recorro cada posición para crear un punto nuevo
        for (int i = 0; i < puntos.length; i++) {
            puntos[i] = new ImageView(getContext());
            puntos[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.punto_inactivo));
            // Defino los margenes entre los puntos
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            puntos[i].setLayoutParams(params);
            layoutPuntos.addView(puntos[i]); // Agrego el punto
        }
    }

    private void setPuntoActual(int index) {
        // Me aseguro de que el fragmento ese agregado y que los puntos existan
        if (!isAdded()) return;
        if (puntos == null) return;
        if (index >= puntos.length) return;

        // Recorro todos los puntos para actualizar cual esta activo
        for (int i = 0; i < puntos.length; i++) {
            if (i == index) {
                puntos[i].setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.punto_activo));
            } else {
                puntos[i].setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.punto_inactivo));
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable); // Detengo cualquier runnable pendiente para evitar fugas en memoria
    }

    @Override
    public void onResume(){
        super.onResume();
        cargarFavoritos(); // Cada vez que vuelvo a inicio recargo los favoritos
    }

    private void mostrarDialogoCultivos() {
        // Creo un dialogo para seleccionar los cultivos
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Selecciona uno o varios cultivos de la Lonja de León");

        // Inflo la vista del diálogo con un RecyclerView
        View elMercado = LayoutInflater.from(getContext()).inflate(R.layout.dialogo_cultivo, null);
        RecyclerView recyclerDialog = elMercado.findViewById(R.id.recyclerDialogCultivos);
        recyclerDialog.setLayoutManager(new LinearLayoutManager(getContext()));

        // Copio los cultivos que aún no son favoritos
        List<Cultivo> copCult = new ArrayList<>();
        for (Cultivo c : cultivoList) {
            if (!cultivoListFav.contains(c)) {
                copCult.add(c);
            }
        }

        // Adapter temporal en modo selección
        CultivoAdapter adapterDialog = new CultivoAdapter(copCult); // lineChart null
        recyclerDialog.setAdapter(adapterDialog);

        // Creo el diálogo y lo hago final para usarlo dentro del listener
        final AlertDialog dialog = builder.setView(elMercado).create();

        adapterDialog.setModoSeleccionFavoritos(true, cultivo -> {
            // Si el cultivo no estaba en  favoritos lo agrego al final
            if(!cultivoListFav.contains(cultivo)){
                cultivoListFav.add( cultivo);
                cultivoAdapter.notifyItemInserted(cultivoListFav.size() -1);

                // Lo elimino de la lista temporal y actualizo el adapter
                int pos = copCult.indexOf(cultivo);
                if (pos != -1) {
                    copCult.remove(pos);
                    adapterDialog.notifyItemRemoved(pos);
                }

                guardarFavoritos();
            }
        });

        dialog.show();
    }

    // Guarda los favoritos en local
    private void guardarFavoritos() {
        // Aquí es donde lo almacenaré los favoritos en local con SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("mis_favoritos", Context.MODE_PRIVATE);
        String favoritosStr = null;

        // Si la versión de android lo permite, genero un string con los nombres separados por "|"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            favoritosStr = cultivoListFav.stream()
                    .map(Cultivo::getNombre)
                    .collect(Collectors.joining("|"));
        }
        preferences.edit().putString("favoritos", favoritosStr).apply(); // Guardo el string en local
    }

    // Carga los favoritos desde local
    private void cargarFavoritos() {
        // Leo el string de favoritos desde SharedPreferences (local)
        SharedPreferences preferences = requireContext().getSharedPreferences("mis_favoritos", Context.MODE_PRIVATE);
        String favoritosStr = preferences.getString("favoritos", "");

        cultivoListFav.clear();

        // Si hay favoritos guardados, los busca en la lista completa y los agrego
        if (!favoritosStr.isEmpty()) {
            String[] nombres = favoritosStr.split("\\|"); // uso el mismo separador "|"
            for (String nombre : nombres) {
                for (Cultivo c : cultivoList) {
                    if (c.getNombre().equals(nombre)) {
                        cultivoListFav.add(c);
                        break;
                    }
                }
            }
        }

        cultivoAdapter.notifyDataSetChanged();
    }

    private void eliminarFavorito(String nombreCultivo){
        // Busco el cultivo en favoritos
        for(int i = 0; i < cultivoListFav.size(); i++){
            if(cultivoListFav.get(i).getNombre().equals(nombreCultivo)){
                // Lo elimino y notifico los cambios
                cultivoListFav.remove(i);
                cultivoAdapter.notifyItemRemoved(i);
                cultivoAdapter.notifyItemRangeChanged(i,cultivoListFav.size());

                guardarFavoritos(); // Guardo cambios localmente
                return;
            }
        }
    }
}

