package com.javier.appcultivo.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.javier.appcultivo.R;
import com.javier.appcultivo.modelo.Noticia;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoticiasAdapter extends RecyclerView.Adapter<NoticiasAdapter.NoticiaViewHolder> {

    private List<Noticia> noticias;
    private Context context;

    public NoticiasAdapter(Context context, List<Noticia> listaNoticias) {
        this.context = context;
        this.noticias = (listaNoticias != null) ? listaNoticias : new ArrayList<>();;
    }

    public void setNoticias(List<Noticia> noticias) {
        this.noticias.clear();
        this.noticias.addAll(noticias);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoticiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_noticia, parent, false);
        return new NoticiaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticiaViewHolder holder, int position) {
        // Con holder guardo la referencia a los elementos de mi ui de cada item
        Noticia noticia = noticias.get(position);
        holder.txtTitulo.setText((String) noticia.getTitulo());
        holder.txtResumen.setText((String) noticia.getResumen());
        holder.txtFecha.setText((String) noticia.getPublicacion());
        //noticia.setImagenUrl("https://efeagro.com/wp-content/uploads/2015/04/Agro-Sevilla-aceitunas-rellenas.jpg");
        //holder.txtUrlImagen.setText((String) noticia.getImagenUrl());

        String imagenUrl = noticia.getImagenUrl(); //holder.txtUrlImagen.getText().toString();// ahora contiene la URL directa
        //Log.d("NoticiaAdapter", "Imagen URL: " + imagenUrl);
        System.out.println("Mi URL PARA LA IMAGEN: " +  imagenUrl);

        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imagenUrl)
                    //.placeholder(R.drawable.placeholder) // opcional, mientras carga
                    .into(holder.imgNoticia);
        }
//        // Imagen desde Base64
//        String base64Image = noticia.getImagenUrl();
//        if (base64Image != null && base64Image.startsWith("data:image")) {
//            String pureBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
//            try {
//                byte[] decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT);
//                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
//                holder.imgNoticia.setImageBitmap(bitmap);
//            } catch (IllegalArgumentException e) {
//                //holder.imgNoticia.setImageResource(R.drawable.placeholder);
//                System.out.println("No se pudo cargar la imagen");
//            }
//        }

//        String imagenBase64 = noticia.getImagenUrl(); // tu Base64
//
//        if (imagenBase64 != null && !imagenBase64.isEmpty()) {
//            // Glide + Base64
//            //Utilizo Glide para cargar la imagen desde mi firestore y Base64 para decodificar la imagen
//            Glide.with(context) // Glide lo decodifica y muestra el Bitmap
//                    .asBitmap()
//                    .load(Base64.decode(imagenBase64.split(",")[1], Base64.DEFAULT)) // Elimino el prefijo "data:image/png;base64,"
//                    .into(holder.imgNoticia); // Convierto la cadena a bytes
//        }
//        else {
//            holder.imgNoticia.setImageResource(R.drawable.placeholder); // opcional imagen del placeholder.xml
//        }
        holder.txtTitulo.setOnClickListener(v -> {
            String url = noticia.getLink();
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return noticias.size();
    }

    static class NoticiaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgNoticia;
        TextView  txtUrlImagen,txtTitulo, txtResumen, txtFecha;

        public NoticiaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNoticia = itemView.findViewById(R.id.imgNoticia);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtResumen = itemView.findViewById(R.id.txtResumen);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            //txtUrlImagen = itemView.findViewById(R.id.txtUrlImagen);
        }
    }
}
