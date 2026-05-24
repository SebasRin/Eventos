package com.eventos.patrones.creacionales;
import com.eventos.modelo.*;
import java.time.LocalDateTime;

public class TeatroFactory extends EventoFactory{

    @Override
    public Evento crearEvento(String nombre, String descripcion,
                              String ciudad, LocalDateTime fechaHora,
                              Recinto recinto) {
        return new Evento(nombre, CategoriaEvento.TEATRO, descripcion,
                ciudad, fechaHora, recinto);
    }

    @Override
    protected void configurarZonasDefecto(Evento evento, Recinto recinto) {
        if (recinto.getZonas().isEmpty()) {
            recinto.agregarZona(new Zona("Palco",   100, 280_000));
            recinto.agregarZona(new Zona("Platea",  400, 150_000));
            recinto.agregarZona(new Zona("Galería", 300,  70_000));
        }
    }
}
