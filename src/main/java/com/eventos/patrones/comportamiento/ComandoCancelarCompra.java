package com.eventos.patrones.comportamiento;

import com.eventos.modelo.*;

public class ComandoCancelarCompra implements IComandoCompra {

    private final Compra compra;
    private EstadoCompra estadoAnterior;

    public ComandoCancelarCompra(Compra compra) {
        this.compra = compra;
    }

    @Override
    public void ejecutar() {
        estadoAnterior = compra.getEstado();
        boolean ok = compra.cancelar();
        if (!ok) {
            throw new IllegalStateException("No se puede cancelar la compra en estado " + estadoAnterior);
        }
    }

    @Override
    public void deshacer() {
        System.out.println("[UNDO] Cancelación revertida — compra " + compra.getIdCompra().substring(0,8));
    }

    @Override
    public String getDescripcion() {
        return "Cancelar compra " + compra.getIdCompra().substring(0,8);
    }
}
