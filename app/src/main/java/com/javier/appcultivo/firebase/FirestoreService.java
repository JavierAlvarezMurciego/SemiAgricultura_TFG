package com.javier.appcultivo.firebase;


import android.os.Build;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.javier.appcultivo.R;
import com.javier.appcultivo.modelo.Cultivo;
import com.javier.appcultivo.modelo.Noticia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreService {
    private FirebaseFirestore db;
    private CollectionReference fechasRef,noticiasRef;

    public FirestoreService() {
        // Firebase ya lo inicializo en la clase MiApp
        db = FirebaseFirestore.getInstance();
        fechasRef = db.collection("cereales")
                .document("cotizaciones_por_fecha")
                .collection("fechas");
        noticiasRef = db.collection("noticias");
    }


    public interface CultivosCallback {
        void onCallback(List<Cultivo> cultivos);
    }

    public interface NoticiasCallBack{
        void onCallback(List<Noticia> noticias);
    }

    public void getNoticias(final NoticiasCallBack noticiasCallBack){
        noticiasRef.orderBy("publicacion", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Noticia> listaNoticias = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Convierte cada documento a objeto Noticia
                            Noticia noticia = document.toObject(Noticia.class);
                            if (noticia != null) {
                                // Asignar la URL de la imagen desde Firestore
                                noticia.setImagenUrl(document.getString("imagen_url"));
                                listaNoticias.add(noticia);
                            }
                        }

                        // Llama al callback pasando la lista de noticias
                        noticiasCallBack.onCallback(listaNoticias);

                    } else {
                        Log.e("Noticias", "Error al obtener noticias", task.getException());
                    }
                });
    }


    // Obtener los cultivos de una fecha específica
    public void getCultivoByFecha(String fecha, final CultivosCallback callback) {
        DocumentReference docRef = fechasRef.document(fecha);

        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Cultivo> listaCultivos = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        Map<String, Object> campos = documentSnapshot.getData();
                        Map<String, Integer> iconoCultivos = new HashMap<>();
                        iconoCultivos.put("Centeno", R.drawable.centeno_espiga);
                        iconoCultivos.put("Maíz *", R.drawable.maiz);// quitar el * de aquí y de la base de datos
                        iconoCultivos.put("Cebada **", R.drawable.cebada);// quitar el ** de aquí y de la base de datos
                        iconoCultivos.put("Trigo Pienso", R.drawable.trigo2);
                        iconoCultivos.put("Triticale", R.drawable.triticale);
                        iconoCultivos.put("Avena", R.drawable.avena);

                        for (Map.Entry<String, Object> entry : campos.entrySet()) {
                            Map<String, Object> precios = (Map<String, Object>) entry.getValue();
                            String precioActual = precios.get("precio_actual").toString();
                            String precioAnterior = precios.get("precio_anterior").toString();

                            Cultivo cultivo = new Cultivo(entry.getKey(), precioActual, precioAnterior);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                cultivo.setIconoResId(iconoCultivos.getOrDefault(cultivo.getNombre(),R.drawable.ic_default));
                            }
                            listaCultivos.add(cultivo);
                        }
                    } else {
                        System.out.println("No existe el documento para la fecha: " + fecha);
                    }
                    callback.onCallback(listaCultivos);
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error al obtener documento: " + e.getMessage());
                    callback.onCallback(new ArrayList<>());
                });
    }

    public void cargarUltimaFecha(final CultivosCallback callback) {
        fechasRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    String ultimaFecha = null;

                    // Buscar la fecha más reciente comparando "DD-MM-YYYY"
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String fecha = doc.getId(); // ID en formato "DD-MM-YYYY"
                        if (ultimaFecha == null || compararFechas(fecha, ultimaFecha) > 0) {
                            ultimaFecha = fecha;
                        }
                    }

                    if (ultimaFecha != null) {
                        // Obtengo los cultivos de la última fecha
                        getCultivoByFecha(ultimaFecha, callback);
                    } else {
                        callback.onCallback(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error al obtener fechas: " + e.getMessage());
                    callback.onCallback(new ArrayList<>());
                });
    }

    // Función auxiliar para comparar fechas "DD-MM-YYYY"
    private int compararFechas(String f1, String f2) {
        String[] p1 = f1.split("-");
        String[] p2 = f2.split("-");
        // Convertir a "YYYYMMDD" para que la comparación de strings funcione
        String c1 = p1[2] + p1[1] + p1[0];
        String c2 = p2[2] + p2[1] + p2[0];
        return c1.compareTo(c2);
    }

}
