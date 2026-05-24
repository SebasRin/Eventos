package com.eventos.patrones.estructurales;

public class ServicioParqueadero extends ServicioDecorador {

    private static final double PRECIO = 15_000;

    public ServicioParqueadero(IServicioAdicional s) {
        super(s);
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " + Parqueadero";
    }

    @Override
    public double getPrecioExtra() {
        return super.getPrecioExtra() + PRECIO;
    }
}