package com.javier.appcultivo.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.List;

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
        // Con holder guardo la referencia a los elementos de mi uisual de cada item
        Noticia noticia = noticias.get(position);
        holder.txtTitulo.setText((String) noticia.getTitulo());
        holder.txtResumen.setText((String) noticia.getResumen());
        holder.txtFecha.setText((String) noticia.getPublication());

        String imagenUrl = noticia.getImagenUrl(); // ahora contiene la URL directa

        System.out.println("Mi URL PARA LA IMAGEN: " +  imagenUrl);

        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imagenUrl)
                    //.placeholder(R.drawable.placeholder) // opcional, mientras carga
                    .into(holder.imgNoticia);
        }

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
        TextView  txtTitulo, txtResumen, txtFecha;

        public NoticiaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNoticia = itemView.findViewById(R.id.imgNoticia);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtResumen = itemView.findViewById(R.id.txtResumen);
            txtFecha = itemView.findViewById(R.id.txtFecha);
        }
    }
}
