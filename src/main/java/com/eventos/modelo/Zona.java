package com.eventos.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Zona {

    private final String idZona;
    private String nombre;
    private int capacidad;
    private double precioBase;
    private final List<Asiento> asientos = new ArrayList<>();

    public Zona(String nombre, int capacidad, double precioBase) {
        this.idZona     = UUID.randomUUID().toString();
        this.nombre     = nombre;
        this.capacidad  = capacidad;
        this.precioBase = precioBase;
    }


    public void agregarAsiento(Asiento a) { asientos.add(a); }
    public void eliminarAsiento(Asiento a){ asientos.remove(a); }


    public int getOcupacion() {
        int contador = 0;
        for (Asiento asiento : asientos) {
            EstadoAsiento estado = asiento.getEstado();
            if (estado == EstadoAsiento.VENDIDO || estado == EstadoAsiento.RESERVADO) {
                contador++;
            }
        }
        return contador;
    }



    public int getDisponibles() {
        int contador = 0;
        for (Asiento asiento : asientos) {
            if (asiento.getEstado() == EstadoAsiento.DISPONIBLE) {
                contador++;
            }
        }
        return contador;
    }

    public String getIdZona() {
        return idZona;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public List<Asiento> getAsientos() {
        return asientos;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public void setPrecioBase(double precioBase) {
        this.precioBase = precioBase;
    }

    @Override
    public String toString() { return nombre + " ($" + String.format("%.0f", precioBase) + ")"; }
}