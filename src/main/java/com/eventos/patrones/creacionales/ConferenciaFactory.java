package com.eventos.patrones.creacionales;
import com.eventos.modelo.*;

import java.time.LocalDateTime;


public class ConferenciaFactory extends EventoFactory{

    @Override
    public Evento crearEvento(String nombre, String descripcion,
                              String ciudad, LocalDateTime fechaHora,
                              Recinto recinto) {
        return new Evento(nombre, CategoriaEvento.CONFERENCIA, descripcion,
                ciudad, fechaHora, recinto);
    }

    @Override
    protected void configurarZonasDefecto(Evento evento, Recinto recinto) {
        if (recinto.getZonas().isEmpty()) {
            recinto.agregarZona(new Zona("Ponente",   50,  500_000));
            recinto.agregarZona(new Zona("Profesional", 300, 200_000));
            recinto.agregarZona(new Zona("Estudiante", 600,  50_000));
        }
    }
}
