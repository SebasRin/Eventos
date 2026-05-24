package com.eventos.patrones.estructurales;

public class PasarelaPagoAdapter implements IPasarelaPago{
    private final SistemaPagoExterno externo = new SistemaPagoExterno();

    @Override
    public boolean procesar(double monto, String metodoPago) {
        SistemaPagoExterno.TransaccionDTO dto =
                new SistemaPagoExterno.TransaccionDTO(monto, metodoPago);
        SistemaPagoExterno.RespuestaDTO r = externo.ejecutarTransaccion(dto);
        return r.exitosa();
    }

    @Override
    public boolean reembolsar(String idPago, double monto) {
        SistemaPagoExterno.RespuestaDTO r = externo.solicitarDevolucion(idPago, monto);
        return r.exitosa();
    }
}
