package com.eventos.repositorio;

import com.eventos.modelo.*;
import java.util.*;


public class RepositorioIncidencias {
    private final List<Incidencia> datos = new ArrayList<>();

    public void guardar(Incidencia i) {
        datos.add(i);
    }
    public List<Incidencia> getAll()  {
        return new ArrayList<>(datos);
    }

    public List<Incidencia> porTipo(TipoIncidencia tipo) {
        List<Incidencia> resultado = new ArrayList<>();

        for (Incidencia i : datos) {
            if (i.getTipo() == tipo) {
                resultado.add(i);
            }
        }
        return resultado;
    }
}