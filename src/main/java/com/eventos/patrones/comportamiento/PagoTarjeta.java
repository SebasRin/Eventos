package com.eventos.patrones.comportamiento;


public class PagoTarjeta implements IEstrategiaPago {

    private final String numero;   // últimos 4 dígitos
    private final String tipo;     // "Visa", "Mastercard"

    public PagoTarjeta(String tipo, String numero) {
        this.tipo   = tipo;
        this.numero = numero;
    }

    @Override
    public boolean pagar(double monto) {
        System.out.printf("[PAGO] Tarjeta %s *%s — $%.0f aprobado%n", tipo, numero, monto);
        return true;
    }

    @Override
    public boolean reembolsar(double monto) {
        System.out.printf("[REEMBOLSO] Tarjeta %s *%s — $%.0f reembolsado%n", tipo, numero, monto);
        return true;
    }

    @Override
    public String getDescripcion() {
        return tipo + " *" + numero;
    }
}