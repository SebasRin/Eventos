package com.eventos.modelo;

import java.time.LocalDateTime;
import java.util.UUID;

public class Incidencia {

    private final String idIncidencia;
    private TipoIncidencia tipo;
    private String descripcion;
    private final LocalDateTime fecha;
    private String entidadAfectada;
    // id de Evento, Compra o Usuario

    public Incidencia(TipoIncidencia tipo, String descripcion, String entidadAfectada) {
        this.idIncidencia    = UUID.randomUUID().toString();
        this.tipo            = tipo;
        this.descripcion     = descripcion;
        this.fecha           = LocalDateTime.now();
        this.entidadAfectada = entidadAfectada;
    }

    public String getIdIncidencia() {
        return idIncidencia;
    }

    public TipoIncidencia getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getEntidadAfectada() {
        return entidadAfectada;
    }

    @Override
    public String toString() {
        return "[" + tipo + "] " + descripcion + " @ " + fecha.toLocalDate();
    }
}
