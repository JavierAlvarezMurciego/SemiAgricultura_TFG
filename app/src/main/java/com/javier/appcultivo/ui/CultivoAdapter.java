package com.javier.appcultivo.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.javier.appcultivo.R;
import com.javier.appcultivo.firebase.FirestoreService;
import com.javier.appcultivo.modelo.Cultivo;
import com.javier.appcultivo.modelo.GraficaCultivo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CultivoAdapter extends RecyclerView.Adapter<CultivoAdapter.CultivoViewHolder> {

    private List<Cultivo> cultivoList;
    private FirestoreService firestoreService;
    private LineChart lineChart;

    // Este método permite pasar la lista completa desde la Activity
    public void setCultivos(List<Cultivo> cultivos) {
        this.cultivoList = cultivos;
        notifyDataSetChanged(); // actualiza la UI
    }
    public CultivoAdapter(List<Cultivo> cultivoList, LineChart lineChart) {
        this.cultivoList = cultivoList;
        this.firestoreService = new FirestoreService();
        this.lineChart = lineChart;
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
        holder.precioActualTextView.setText(cultivo.getPrecioActual().toString()); //Poner el precio actual en el textView
        holder.precioAnteriorTextView.setText(cultivo.getPrecioAnterior().toString()); //Poner el precio anterior en el textView

        int colorAct, colorAnt;

        try {
            Double precioActual = Double.parseDouble(cultivo.getPrecioActual());
            Double precioAnterior = Double.parseDouble(cultivo.getPrecioAnterior());

            if(precioActual>precioAnterior){
                colorAct = Color.GREEN;
            }else if(precioActual<precioAnterior){
                colorAct = Color.RED;
            }else{
                colorAct = Color.BLACK;
            }

            if(precioAnterior>precioActual){
                colorAnt = Color.GREEN;
            }else if(precioAnterior<precioActual){
                colorAnt = Color.RED;
            }else{
                colorAnt = Color.BLACK;
            }

            holder.precioActualTextView.setTextColor(colorAct);
            holder.precioAnteriorTextView.setTextColor(colorAnt);

            holder.itemView.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, GraficaCultivo.class);
                intent.putExtra("nombreCultivo", cultivo.getNombre());
                context.startActivity(intent);
            });

        } catch (NumberFormatException e) {
            holder.precioActualTextView.setTextColor(Color.BLACK);
            holder.precioAnteriorTextView.setTextColor(Color.BLACK);
        }
    }

    private void crearGrafica(String nomCultivo){
        firestoreService.getHistoricoPrecio(Collections.singletonList(nomCultivo), (preciosCultivo, fechas) -> {
            List<Entry> entries = new ArrayList<>();
            List<Double> precios = preciosCultivo.get(nomCultivo);

            if (precios != null){
                for (int i = 0; i < precios.size(); i++){
                    entries.add(new Entry(i, precios.get(i).floatValue()));
                }
            }

            LineDataSet dataSet = new LineDataSet(entries, nomCultivo);
            dataSet.setColor(Color.BLUE);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(3f);

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);

            lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(fechas));
            lineChart.getXAxis().setGranularity(1f);
            lineChart.getAxisRight().setEnabled(false);
            lineChart.getDescription().setEnabled(false);
            lineChart.invalidate();
        });
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