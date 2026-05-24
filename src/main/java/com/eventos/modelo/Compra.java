package com.eventos.modelo;

import com.eventos.patrones.comportamiento.IObservadorCompra;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Compra {

    private final String idCompra;
    private final Usuario usuario;
    private final Evento evento;
    private final LocalDateTime fechaCreacion;
    private double total;
    private EstadoCompra estado;
    private Pago pago;

    private final List<Entrada> entradas = new ArrayList<>();
    private final List<String>  serviciosAdicionales = new ArrayList<>();

    // Observer: observadores de cambios de estado
    private final List<IObservadorCompra> observadores = new ArrayList<>();

    public Compra(Usuario usuario, Evento evento,
                  List<Entrada> entradas, List<String> servicios) {
        this.idCompra      = UUID.randomUUID().toString();
        this.usuario       = usuario;
        this.evento        = evento;
        this.fechaCreacion = LocalDateTime.now();
        this.estado        = EstadoCompra.CREADA;
        this.entradas.addAll(entradas);
        this.serviciosAdicionales.addAll(servicios);
        calcularTotal();

    }

    private void calcularTotal() {
        this.total = entradas.stream().mapToDouble(Entrada::getPrecioFinal).sum();
    }


    public boolean pagar(Pago pago) {
        this.pago = pago;
        boolean ok = pago.procesar();
        if (ok) {
            for (Entrada e : entradas) {
                if (e.getAsiento() != null) {
                    e.getAsiento().vender();
                }
            }
            cambiarEstado(EstadoCompra.PAGADA);
            cambiarEstado(EstadoCompra.CONFIRMADA);
        }
        return ok;
    }


    public boolean cancelar() {
        if (estado == EstadoCompra.CREADA) {
            for (Entrada e : entradas) {
                if (e.getAsiento() != null) {
                    e.getAsiento().liberar();
                }
            }
            entradas.forEach(Entrada::anular);
            cambiarEstado(EstadoCompra.CANCELADA);
            return true;
        }
        if (estado == EstadoCompra.CONFIRMADA && pago != null) {
            pago.reembolsar();
            for (Entrada entrada : entradas) {
                entrada.anular();
            }
            cambiarEstado(EstadoCompra.REEMBOLSADA);
            return true;
        }
        return false;
    }

    public void agregarObservador(IObservadorCompra o) { observadores.add(o); }
    public void eliminarObservador(IObservadorCompra o){ observadores.remove(o); }

    private void cambiarEstado(EstadoCompra nuevo) {
        EstadoCompra anterior = this.estado;
        this.estado = nuevo;
        observadores.forEach(o -> o.compraActualizada(this, anterior));
    }

    public String getIdCompra() {
        return idCompra;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Evento getEvento() {
        return evento;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public EstadoCompra getEstado() {
        return estado;
    }

    public double getTotal() {
        return total;
    }

    public Pago getPago() {
        return pago;
    }

    public List<Entrada> getEntradas() {
        return entradas;
    }

    public List<String> getServiciosAdicionales() {
        return serviciosAdicionales;
    }

    public List<IObservadorCompra> getObservadores() {
        return observadores;
    }

    public void agregarServicio(String s){
        serviciosAdicionales.add(s); calcularTotal();
    }

    @Override
    public String toString() {
        return "Compra[" + idCompra.substring(0,8) + "] " + evento.getNombre()
                + " - " + estado + " $" + String.format("%.0f", total);
    }
}
