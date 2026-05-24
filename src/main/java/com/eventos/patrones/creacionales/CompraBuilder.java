package com.eventos.patrones.creacionales;

import com.eventos.modelo.*;

import java.util.ArrayList;
import java.util.List;


public class CompraBuilder {

    private Usuario usuario;
    private Evento  evento;
    private final List<Entrada> entradas  = new ArrayList<>();
    private final List<String>  servicios = new ArrayList<>();

    public CompraBuilder setUsuario(Usuario u) {
        this.usuario = u;
        return this;
    }

    public CompraBuilder setEvento(Evento e) {
        this.evento = e;
        return this;
    }

    public CompraBuilder addEntrada(Entrada e) {
        entradas.add(e);
        return this;
    }

    //servicios adicionales (VIP, seguro, merchandising)
    public CompraBuilder addServicio(String descripcionServicio) {
        servicios.add(descripcionServicio);
        return this;
    }

    public Compra build() {
        validar();
        Compra compra = new Compra(usuario, evento, entradas, servicios);
        usuario.agregarCompra(compra);
        GestorSistema.getInstance().compras().guardar(compra);
        return compra;
    }

    private void validar() {
        if (usuario == null) throw new IllegalStateException("La compra requiere un usuario.");
        if (evento  == null) throw new IllegalStateException("La compra requiere un evento.");
        if (entradas.isEmpty()) throw new IllegalStateException("Debe seleccionar al menos una entrada.");
        if (evento.getEstado() != com.eventos.modelo.EstadoEvento.PUBLICADO)
            throw new IllegalStateException("El evento no está disponible para compra.");
    }
}