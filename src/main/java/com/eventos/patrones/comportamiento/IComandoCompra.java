package com.eventos.patrones.comportamiento;

public interface IComandoCompra {
    void ejecutar();
    void deshacer();
    String getDescripcion();
}
