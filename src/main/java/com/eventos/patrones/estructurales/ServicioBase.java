package com.eventos.patrones.estructurales;

//Componente base sin servicios adicionales
public class ServicioBase implements IServicioAdicional {
    @Override
    public String getDescripcion() {
        return "Entrada estándar";
    }

    @Override
    public double getPrecioExtra() {
        return 0;
    }
}