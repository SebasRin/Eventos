package com.eventos.repositorio;

import com.eventos.modelo.*;
import java.util.*;


public class RepositorioCompras {
    private final Map<String, Compra> datos = new LinkedHashMap<>();

    public void guardar(Compra c){
        datos.put(c.getIdCompra(), c);
    }
    public Optional<Compra> buscarPorId(String id) {
        return Optional.ofNullable(datos.get(id));
    }
    public List<Compra> getAll() {
        return new ArrayList<>(datos.values());
    }

    public List<Compra> porUsuario(String idUsuario) {
        List<Compra> resultado = new ArrayList<>();

        for (Compra c : datos.values()) {
            if (c.getUsuario().getIdUsuario().equals(idUsuario)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public List<Compra> porEstado(EstadoCompra estado) {
        List<Compra> resultado = new ArrayList<>();

        for (Compra c : datos.values()) {
            if (c.getEstado() == estado) {
                resultado.add(c);
            }
        }
        return resultado;
    }
}
