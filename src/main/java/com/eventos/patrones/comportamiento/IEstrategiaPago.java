package com.eventos.patrones.comportamiento;

public interface IEstrategiaPago {
    boolean pagar(double monto);
    boolean reembolsar(double monto);
    String  getDescripcion();
}
