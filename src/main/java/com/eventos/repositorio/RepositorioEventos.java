package com.eventos.repositorio;

import com.eventos.modelo.*;
import java.util.*;


public class RepositorioEventos {
    private final Map<String, Evento> datos = new LinkedHashMap<>();

    public void guardar(Evento e)  {
        datos.put(e.getIdEvento(), e);
    }
    public void eliminar(String id){
        datos.remove(id);
    }
    public Optional<Evento> buscarPorId(String id) {
        return Optional.ofNullable(datos.get(id));
    }
    public List<Evento> getAll() {
        return new ArrayList<>(datos.values());
    }

    public List<Evento> buscar(String ciudad, CategoriaEvento cat) {
        List<Evento> resultado = new ArrayList<>();

        for (Evento e : datos.values()) {
            if (e.getEstado() != EstadoEvento.PUBLICADO) {
                continue;
            }
            if (ciudad != null && !ciudad.isBlank() && !e.getCiudad().equalsIgnoreCase(ciudad)) {
                continue;
            }
            if (cat != null && e.getCategoria() != cat) {
                continue;
            }
            resultado.add(e);
        }
        return resultado;
    }
}