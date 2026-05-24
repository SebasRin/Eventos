package com.eventos.patrones.estructurales;

public interface IPasarelaPago {
    boolean procesar(double monto, String metodoPago);
    boolean reembolsar(String idPago, double monto);
}
