package com.eventos.modelo;

import java.util.UUID;


public class Asiento {

    private final String idAsiento;
    private String fila;
    private int numero;
    private EstadoAsiento estado;

    public Asiento(String fila, int numero) {
        this.idAsiento = UUID.randomUUID().toString();
        this.fila      = fila;
        this.numero    = numero;
        this.estado    = EstadoAsiento.DISPONIBLE;
    }

    // cambios de estado
    public boolean reservar() {
        if (estado == EstadoAsiento.DISPONIBLE) {
            estado = EstadoAsiento.RESERVADO;
            return true;
        }
        return false;
    }

    public boolean vender() {
        if (estado == EstadoAsiento.RESERVADO || estado == EstadoAsiento.DISPONIBLE) {
            estado = EstadoAsiento.VENDIDO;
            return true;
        }
        return false;
    }

    public boolean liberar() {
        if (estado != EstadoAsiento.BLOQUEADO) {
            estado = EstadoAsiento.DISPONIBLE;
            return true;
        }
        return false;
    }

    public void bloquear()   { estado = EstadoAsiento.BLOQUEADO; }




    public String getIdAsiento() {
        return idAsiento;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public EstadoAsiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoAsiento estado) {
        this.estado = estado;
    }

    @Override
    public String toString() { return "Fila " + fila + " - Asiento " + numero + " [" + estado + "]"; }
}
