package com.eventos.patrones.comportamiento;

import com.eventos.modelo.*;

//Command concreto procesa el pago de una compra
public class ComandoPagarCompra implements IComandoCompra {

    private final Compra compra;
    private final Pago   pago;
    private EstadoCompra estadoAnterior;

    public ComandoPagarCompra(Compra compra, Pago pago) {
        this.compra = compra;
        this.pago   = pago;
    }

    @Override
    public void ejecutar() {
        estadoAnterior = compra.getEstado();
        compra.pagar(pago);
    }

    @Override
    public void deshacer() {
        System.out.println("[UNDO] Pago revertido — compra " + compra.getIdCompra().substring(0,8));
    }

    @Override
    public String getDescripcion() {
        return "Pagar compra " + compra.getIdCompra().substring(0,8)
                + " — $" + String.format("%.0f", compra.getTotal());
    }
}
