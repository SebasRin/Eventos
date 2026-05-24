package com.eventos.patrones.comportamiento;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;

//Invocador del patrón Command.

public class HistorialComandos {

    private final Deque<IComandoCompra> pila = new ArrayDeque<>();

    public void ejecutar(IComandoCompra comando) {
        comando.ejecutar();
        pila.push(comando);
        System.out.println("[HISTORIAL] " + comando.getDescripcion());
    }

    public void deshacer() {
        if (!pila.isEmpty()) {
            IComandoCompra ultimo = pila.pop();
            ultimo.deshacer();
        }
    }

    public List<String> getHistorial() {
        List<String> lista = new ArrayList<>();
        for (IComandoCompra c : pila) {
            lista.add(c.getDescripcion());
        }
        return lista;
    }
}