package com.eventos.patrones.estructurales;

public class ServicioMerchandising extends ServicioDecorador {

    private static final double PRECIO = 45_000;

    public ServicioMerchandising(IServicioAdicional s) {
        super(s);
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " + Kit merchandising";
    }

    @Override
    public double getPrecioExtra() {
        return super.getPrecioExtra() + PRECIO;
    }
}