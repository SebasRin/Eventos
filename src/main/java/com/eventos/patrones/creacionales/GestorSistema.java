package com.eventos.patrones.creacionales;

import com.eventos.modelo.*;
import com.eventos.repositorio.*;

public class GestorSistema {

    private static volatile GestorSistema instancia;

    //Repositorios
    private final RepositorioEventos    repositorioEventos;
    private final RepositorioUsuarios   repositorioUsuarios;
    private final RepositorioCompras    repositorioCompras;
    private final RepositorioRecintos   repositorioRecintos;
    private final RepositorioIncidencias repositorioIncidencias;

    private GestorSistema() {
        repositorioEventos     = new RepositorioEventos();
        repositorioUsuarios    = new RepositorioUsuarios();
        repositorioCompras     = new RepositorioCompras();
        repositorioRecintos    = new RepositorioRecintos();
        repositorioIncidencias = new RepositorioIncidencias();
    }


    public static GestorSistema getInstance() {
        if (instancia == null) {
            synchronized (GestorSistema.class) {
                if (instancia == null) {
                    instancia = new GestorSistema();
                }
            }
        }
        return instancia;
    }


    //Delegacion
    public RepositorioEventos    eventos()     { return repositorioEventos; }
    public RepositorioUsuarios   usuarios()    { return repositorioUsuarios; }
    public RepositorioCompras    compras()     { return repositorioCompras; }
    public RepositorioRecintos   recintos()    { return repositorioRecintos; }
    public RepositorioIncidencias incidencias(){ return repositorioIncidencias; }

    //Registra una incidencia en el sistema
    public void registrarIncidencia(TipoIncidencia tipo, String desc, String entidad) {
        repositorioIncidencias.guardar(new Incidencia(tipo, desc, entidad));
    }

}
