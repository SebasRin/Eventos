package com.eventos.patrones.creacionales;
import com.eventos.modelo.*;

import java.time.LocalDateTime;

public class ConciertoFactory extends EventoFactory {

    @Override
    public Evento crearEvento(String nombre, String descripcion,
                              String ciudad, LocalDateTime fechaHora,
                              Recinto recinto) {
        return new Evento(nombre, CategoriaEvento.CONCIERTO, descripcion,
                ciudad, fechaHora, recinto);
    }

    @Override
    protected void configurarZonasDefecto(Evento evento, Recinto recinto) {
        if (recinto.getZonas().isEmpty()) {
            recinto.agregarZona(new Zona("VIP",         200,  350_000));
            recinto.agregarZona(new Zona("Preferencial", 500,  180_000));
            recinto.agregarZona(new Zona("General",     1500,  80_000));
        }
    }
}
