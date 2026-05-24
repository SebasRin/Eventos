package com.eventos.patrones.estructurales;

public class ServicioSeguroCancelacion extends ServicioDecorador {

    private static final double PRECIO = 25_000;

    public ServicioSeguroCancelacion(IServicioAdicional s) {
        super(s);
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " + Seguro de cancelación";
    }

    @Override
    public double getPrecioExtra() {
        return super.getPrecioExtra() + PRECIO;
    }
}
