package com.eventos.patrones.comportamiento;

import com.eventos.modelo.Compra;
import com.eventos.modelo.EstadoCompra;

public class NotificadorEmail implements IObservadorCompra {

    @Override
    public void compraActualizada(Compra compra, EstadoCompra estadoAnterior) {
        String destinatario = compra.getUsuario().getCorreo();
        String asunto = switch (compra.getEstado()) {
            case CONFIRMADA  -> "¡Tu compra fue confirmada!";
            case CANCELADA   -> "Tu compra fue cancelada";
            case REEMBOLSADA -> "Tu reembolso está en camino";
            default          -> "Actualización de tu compra";
        };
        System.out.printf("[EMAIL] → %s | Asunto: %s | Compra: %s%n",
                destinatario, asunto, compra.getIdCompra().substring(0, 8));
    }
}
