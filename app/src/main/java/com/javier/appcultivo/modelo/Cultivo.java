package com.javier.appcultivo.modelo;


public class Cultivo {
    private String nombre,precioActual,precioAnterior;
    private int iconoResId;

    // Constructor vacío necesario para Firestore
    public Cultivo() {}

    public Cultivo(String nombre, String precioActual, String precioAnterior) {
        this.nombre = nombre;
        this.precioActual = precioActual;
        this.precioAnterior = precioAnterior;
        this.iconoResId = 0;
    }

    public String getNombre() {
        return nombre;
    }
    public String getPrecioActual() {return precioActual;}
    public String getPrecioAnterior() {return precioAnterior;}
    public int getIconoResId(){return iconoResId;}

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setPrecioActual(String precioActual) {this.precioActual = precioActual;}
    public void setPrecioAnterior(String precioAnterior){this.precioAnterior = precioAnterior;}
    public void setIconoResId(int iconoResId){this.iconoResId = iconoResId;}
}
