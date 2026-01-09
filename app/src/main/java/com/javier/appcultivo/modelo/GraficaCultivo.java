package com.javier.appcultivo.modelo;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.javier.appcultivo.R;
import com.javier.appcultivo.firebase.FirestoreService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GraficaCultivo extends AppCompatActivity {
    private LineChart lineChart;
    private FirestoreService firestoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grafica_cultivo);

        lineChart = findViewById(R.id.graficaCultivo);
        firestoreService = new FirestoreService();

        // Recibo el cultivo que voy a usar para crear la grafica
        String nombreCultivo = getIntent().getStringExtra("nombreCultivo");

        firestoreService.getHistoricoPrecio(
                Collections.singletonList(nombreCultivo),
                (preciosCultivo, fechas) -> {

                    List<Double> precios = preciosCultivo.get(nombreCultivo);
                    if (precios == null || precios.isEmpty()) return;

                    // Crear entries solo para valores válidos
                    List<Entry> entries = new ArrayList<>();
                    List<String> fechasEtiquetas = new ArrayList<>();
                    int index = 0; // índice real para el gráfico

                    for (int i = 0; i < fechas.size(); i++) {
                        double precio = precios.get(i);

                        if (!Double.isNaN(precio)) { // omitir siempre el "S/C"
                            entries.add(new Entry(i,(float) precio));
                            fechasEtiquetas.add(fechas.get(i));
                            index++;
                        }
                    }

                    // Configurar LineDataSet
                    LineDataSet dataSet = new LineDataSet(entries, nombreCultivo);
                    dataSet.setColor(Color.BLUE);
                    dataSet.setLineWidth(2f);
                    dataSet.setDrawCircles(false);
                    dataSet.setDrawValues(false);

                    LineData lineData = new LineData(dataSet);
                    lineChart.setData(lineData);

                    // Configurar eje X
                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setGranularity(1f);
                    xAxis.setLabelRotationAngle(-45f);
                    xAxis.setAvoidFirstLastClipping(true);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(fechasEtiquetas));

                    // Configurar eje Y
                    double minPrecio = 0;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        minPrecio = precios.stream()
                                .filter(p -> !Double.isNaN(p))
                                .min(Double::compareTo)
                                .orElse(0.0);
                    }
                    double maxPrecio = 0;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        maxPrecio = precios.stream()
                                .filter(p -> !Double.isNaN(p))
                                .max(Double::compareTo)
                                .orElse(10.0);
                    }

                    YAxis leftAxis = lineChart.getAxisLeft();
                    leftAxis.setGranularity(1f); // paso de 1 en 1
                    leftAxis.setGranularityEnabled(true);
                    leftAxis.setAxisMinimum((float) Math.floor(minPrecio));
                    leftAxis.setAxisMaximum((float) Math.ceil(maxPrecio));
                    leftAxis.setLabelCount((int)(Math.ceil(maxPrecio) - Math.floor(minPrecio) + 1), true);

                    lineChart.getAxisRight().setEnabled(false);    // desactivo eje derecho
                    lineChart.getDescription().setEnabled(false); // desactivo la descripción

                    // Scroll y zoom
                    lineChart.setDragEnabled(true);
                    lineChart.setScaleXEnabled(false);
                    lineChart.setScaleYEnabled(false);
                    lineChart.setVisibleXRangeMaximum(12f);

                    // Muevo la vista para que empiece en la última semana
                    float ultimaSemana = fechas.size() - 1;
                    lineChart.moveViewToX(ultimaSemana);

                    // Leyenda y offsets
                    Legend legend = lineChart.getLegend();
                    legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
                    legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                    legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                    legend.setDrawInside(false);
                    legend.setWordWrapEnabled(true);
                    legend.setYOffset(15f);
                    legend.setTextSize(20f);

                    lineChart.setExtraBottomOffset(40f);
                    lineChart.setExtraTopOffset(10f);
                    lineChart.setExtraLeftOffset(10f);
                    lineChart.setExtraRightOffset(10f);


                    lineChart.invalidate(); // refrescar gráfico
                });
    }
}
