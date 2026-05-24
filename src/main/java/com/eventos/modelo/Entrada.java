package com.eventos.modelo;

import java.util.UUID;


public class Entrada {

    private final String idEntrada;
    private Zona zona;
    private Asiento asiento;       // null si la zona no maneja asientos numerados
    private double precioFinal;
    private EstadoEntrada estado;

    public Entrada(Zona zona, Asiento asiento, double precioFinal) {
        this.idEntrada   = UUID.randomUUID().toString();
        this.zona        = zona;
        this.asiento     = asiento;
        this.precioFinal = precioFinal;
        this.estado      = EstadoEntrada.ACTIVA;
    }


    public void anular() {
        this.estado = EstadoEntrada.ANULADA;
        if (asiento != null) {
            asiento.liberar();
        }
    }

    public void marcarUsada() {
        this.estado = EstadoEntrada.USADA;
    }



    public String getIdEntrada() {
        return idEntrada;
    }

    public Zona getZona() {
        return zona;
    }

    public Asiento getAsiento() {
        return asiento;
    }

    public double getPrecioFinal() {
        return precioFinal;
    }

    public EstadoEntrada getEstado() {
        return estado;
    }

    @Override
    public String toString() {
        String seat = asiento != null ? " " + asiento : "";
        return "Entrada: " + zona.getNombre() + seat + " $" + String.format("%.0f", precioFinal);
    }
}
