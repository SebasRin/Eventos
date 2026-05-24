package com.eventos.patrones.comportamiento;

import com.eventos.modelo.Compra;
import com.eventos.modelo.EstadoCompra;

public class NotificadorSMS implements IObservadorCompra {

    @Override
    public void compraActualizada(Compra compra, EstadoCompra estadoAnterior) {
        String tel = compra.getUsuario().getTelefono();
        System.out.printf("[SMS] → %s | Compra %s: %s%n",
                tel, compra.getIdCompra().substring(0, 8), compra.getEstado());
    }
}