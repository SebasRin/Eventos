package com.eventos.patrones.comportamiento;


public class PagoPSE implements IEstrategiaPago {

    private final String banco;

    public PagoPSE(String banco) {
        this.banco = banco;
    }

    @Override
    public boolean pagar(double monto) {
        System.out.printf("[PSE] Banco %s — $%.0f transferido%n", banco, monto);
        return true;
    }

    @Override
    public boolean reembolsar(double monto) {
        System.out.printf("[PSE] Reembolso %s — $%.0f%n", banco, monto);
        return true;
    }

    @Override
    public String getDescripcion() {
        return "PSE — " + banco;
    }
}
