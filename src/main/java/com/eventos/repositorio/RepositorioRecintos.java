package com.eventos.repositorio;

import com.eventos.modelo.Recinto;
import java.util.*;


public class RepositorioRecintos {
    private final Map<String, Recinto> datos = new LinkedHashMap<>();

    public void guardar(Recinto r)  {
        datos.put(r.getIdRecinto(), r);
    }
    public void eliminar(String id) {
        datos.remove(id);
    }
    public Optional<Recinto> buscarPorId(String id) {
        return Optional.ofNullable(datos.get(id));
    }
    public List<Recinto> getAll() {
        return new ArrayList<>(datos.values());
    }
}