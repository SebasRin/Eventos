package com.eventos.patrones.comportamiento;

import com.eventos.modelo.Evento;
import com.eventos.modelo.EstadoEvento;

public interface IObservadorEvento {
    void eventoActualizado(Evento evento, EstadoEvento estadoAnterior);
}
