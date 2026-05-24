package com.eventos.patrones.creacionales;

import com.eventos.modelo.*;

import java.time.LocalDateTime;


public abstract class EventoFactory {


    public abstract Evento crearEvento(String nombre, String descripcion,
                                       String ciudad, LocalDateTime fechaHora,
                                       Recinto recinto);

    public Evento crearYRegistrar(String nombre, String descripcion,
                                  String ciudad, LocalDateTime fechaHora,
                                  Recinto recinto) {
        Evento evento = crearEvento(nombre, descripcion, ciudad, fechaHora, recinto);
        configurarZonasDefecto(evento, recinto);
        GestorSistema.getInstance().eventos().guardar(evento);
        return evento;
    }

    protected void configurarZonasDefecto(Evento evento, Recinto recinto) {
        // por defecto no agrega zonas extra, las subclases pueden sobrescribir
    }



}