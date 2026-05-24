package com.eventos.patrones.comportamiento;

import com.eventos.modelo.*;
import com.eventos.patrones.creacionales.GestorSistema;


public class RegistradorIncidencias implements IObservadorCompra {

    @Override
    public void compraActualizada(Compra compra, EstadoCompra estadoAnterior) {
        if (compra.getEstado() == EstadoCompra.INCIDENCIA) {
            GestorSistema.getInstance().registrarIncidencia(
                    TipoIncidencia.OTRO,
                    "Compra pasó a INCIDENCIA desde " + estadoAnterior,
                    compra.getIdCompra());
        }
        if (compra.getEstado() == EstadoCompra.REEMBOLSADA) {
            GestorSistema.getInstance().registrarIncidencia(
                    TipoIncidencia.REEMBOLSO,
                    "Reembolso generado para compra " + compra.getIdCompra().substring(0, 8),
                    compra.getIdCompra());
        }
    }
}