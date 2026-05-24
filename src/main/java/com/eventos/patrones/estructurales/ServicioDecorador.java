package com.eventos.patrones.estructurales;

//Decorador abstracto delega al componente envuelto.
public abstract class ServicioDecorador implements IServicioAdicional {

    protected final IServicioAdicional wrapped;

    public ServicioDecorador(IServicioAdicional s) {
        this.wrapped = s;
    }

    @Override
    public String getDescripcion() {
        return wrapped.getDescripcion();
    }

    @Override
    public double getPrecioExtra() {
        return wrapped.getPrecioExtra();
    }
}