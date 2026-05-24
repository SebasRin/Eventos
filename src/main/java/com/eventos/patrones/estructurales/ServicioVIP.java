package com.eventos.patrones.estructurales;

public class ServicioVIP extends ServicioDecorador {

    private static final double PRECIO = 80_000;

    public ServicioVIP(IServicioAdicional s) {
        super(s);
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " + Acceso VIP";
    }

    @Override
    public double getPrecioExtra() {
        return super.getPrecioExtra() + PRECIO;
    }
}