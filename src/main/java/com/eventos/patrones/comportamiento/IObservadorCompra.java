package com.eventos.patrones.comportamiento;

import com.eventos.modelo.Compra;
import com.eventos.modelo.EstadoCompra;

public interface IObservadorCompra {
    void compraActualizada(Compra compra, EstadoCompra estadoAnterior);
}
