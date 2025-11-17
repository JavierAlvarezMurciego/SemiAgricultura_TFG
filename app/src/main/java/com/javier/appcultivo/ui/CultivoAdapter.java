package com.javier.appcultivo.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.javier.appcultivo.R;
import com.javier.appcultivo.modelo.Cultivo;

import java.util.List;

public class CultivoAdapter extends RecyclerView.Adapter<CultivoAdapter.CultivoViewHolder> {

    private List<Cultivo> cultivoList;

    // Este método permite pasar la lista completa desde la Activity
    public void setCultivos(List<Cultivo> cultivos) {
        this.cultivoList = cultivos;
        notifyDataSetChanged(); // actualiza la UI
    }
    public CultivoAdapter(List<Cultivo> cultivoList) {
        this.cultivoList = cultivoList;
    }

    @Override
    public CultivoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cultivo, parent, false);
        return new CultivoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CultivoViewHolder holder, int position) {
        Cultivo cultivo = cultivoList.get(position);

        holder.nombreTextView.setText(cultivo.getNombre());
        holder.icono.setImageResource(cultivo.getIconoResId());
        holder.precioActualTextView.setText(cultivo.getPrecioActual()); //Poner el precio actual en el textView
        holder.precioAnteriorTextView.setText(cultivo.getPrecioAnterior()); //Poner el precio anterior en el textView

//        try {
////            int precioActual = Integer.parseInt(cultivo.getPrecioActual());
////            int precioAnterior = Integer.parseInt(cultivo.getPrecioAnterior());
////            holder.precioTextView.setText(String.format("%.2f", precioActual));
////            holder.precioTextView.setText(String.format("%.2f", precioAnterior));
//        } catch (NumberFormatException e) {
//            // Si el String no se puede convertir a número, mostramos el valor tal cual
//            //holder.precioTextView.setText(cultivo.getPrecioActual());
//            holder.precioTextView.setText("No hay precios disponibles");
//        }
    }

    @Override
    public int getItemCount() {
        return cultivoList.size();
    }

    public static class CultivoViewHolder extends RecyclerView.ViewHolder {
        TextView nombreTextView, precioActualTextView, precioAnteriorTextView;
        ImageView icono;

        public CultivoViewHolder(View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nombreCultivoTextView);
            precioActualTextView = itemView.findViewById(R.id.numPrecioActual);
            precioAnteriorTextView = itemView.findViewById(R.id.numPrecioAnterior);
            icono = itemView.findViewById(R.id.iconoCutlivo);
        }
    }
}