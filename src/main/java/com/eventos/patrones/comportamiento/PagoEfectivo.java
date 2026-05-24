package com.eventos.patrones.comportamiento;

public class PagoEfectivo implements IEstrategiaPago {

    @Override
    public boolean pagar(double monto) {
        System.out.printf("[EFECTIVO] $%.0f recibido en caja%n", monto);
        return true;
    }

    @Override
    public boolean reembolsar(double monto) {
        System.out.printf("[EFECTIVO] Reembolso $%.0f en caja%n", monto);
        return true;
    }

    @Override
    public String getDescripcion() {
        return "Efectivo (punto autorizado)";
    }
}
