package com.eventos.patrones.estructurales;

public class SistemaPagoExterno {
    public record TransaccionDTO(double monto, String metodo) {}
    public record RespuestaDTO(boolean exitosa, String codigo) {}

    public RespuestaDTO ejecutarTransaccion(TransaccionDTO dto) {
        System.out.printf("[EXT-PAY] Procesando $%.0f via %s%n", dto.monto(), dto.metodo());
        return new RespuestaDTO(true, "TXN-" + System.currentTimeMillis());
    }

    public RespuestaDTO solicitarDevolucion(String idPago, double monto) {
        System.out.printf("[EXT-PAY] Devolución %s — $%.0f%n", idPago, monto);
        return new RespuestaDTO(true, "REF-" + System.currentTimeMillis());
    }
}
