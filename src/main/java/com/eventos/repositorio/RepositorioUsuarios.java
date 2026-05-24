package com.eventos.repositorio;

import com.eventos.modelo.Usuario;
import java.util.*;


public class RepositorioUsuarios {
    private final Map<String, Usuario> datos = new LinkedHashMap<>();

    public void guardar(Usuario u)  {
        datos.put(u.getIdUsuario(), u);
    }
    public void eliminar(String id) {
        datos.remove(id);
    }
    public Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(datos.get(id));
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        for (Usuario u : datos.values()) {
            if (u.getCorreo().equalsIgnoreCase(correo)) {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    public List<Usuario> getAll() { return new ArrayList<>(datos.values()); }
}