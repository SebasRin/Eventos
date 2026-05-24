package com.eventos.modelo;

import com.eventos.patrones.comportamiento.IObservadorEvento;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Evento {

    private final String idEvento;
    private String nombre;
    private CategoriaEvento categoria;
    private String descripcion;
    private String ciudad;
    private LocalDateTime fechaHora;
    private EstadoEvento estado;
    private String politicaCancelacion;
    private String politicaReembolso;
    private Recinto recinto;

    // lista de observadores de cambios de estado
    private final List<IObservadorEvento> observadores = new ArrayList<>();

    public Evento(String nombre, CategoriaEvento categoria, String descripcion,
                  String ciudad, LocalDateTime fechaHora, Recinto recinto) {
        this.idEvento           = UUID.randomUUID().toString();
        this.nombre             = nombre;
        this.categoria          = categoria;
        this.descripcion        = descripcion;
        this.ciudad             = ciudad;
        this.fechaHora          = fechaHora;
        this.recinto            = recinto;
        this.estado             = EstadoEvento.BORRADOR;
        this.politicaCancelacion = "Cancelación sin cargo hasta 48h antes";
        this.politicaReembolso   = "Reembolso del 80% si cancela con +24h de anticipación";
    }

    // transiciones de estado
    public void publicar() {
        if (estado == EstadoEvento.BORRADOR || estado == EstadoEvento.PAUSADO) {
            cambiarEstado(EstadoEvento.PUBLICADO);
        }
    }

    public void pausar() {
        if (estado == EstadoEvento.PUBLICADO) {
            cambiarEstado(EstadoEvento.PAUSADO);
        }
    }

    public void cancelar() {
        if (estado != EstadoEvento.FINALIZADO) {
            cambiarEstado(EstadoEvento.CANCELADO);
        }
    }

    public void finalizar() {
        cambiarEstado(EstadoEvento.FINALIZADO);
    }

    // disponibilidad por zona
    public int getDisponibilidadTotal() {
        int total = 0;
        for (Zona zona : recinto.getZonas()) {
            total += zona.getDisponibles();
        }
        return total;
    }

    //Observer
    public void agregarObservador(IObservadorEvento o) { observadores.add(o); }
    public void eliminarObservador(IObservadorEvento o){ observadores.remove(o); }

    private void cambiarEstado(EstadoEvento nuevo) {
        EstadoEvento anterior = this.estado;
        this.estado = nuevo;
        for (IObservadorEvento o : observadores) {
            o.eventoActualizado(this, anterior);
        }
    }

    public String getIdEvento() {
        return idEvento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public CategoriaEvento getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaEvento categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvento estado) {
        this.estado = estado;
    }

    public String getPoliticaCancelacion() {
        return politicaCancelacion;
    }

    public void setPoliticaCancelacion(String politicaCancelacion) {
        this.politicaCancelacion = politicaCancelacion;
    }

    public String getPoliticaReembolso() {
        return politicaReembolso;
    }

    public void setPoliticaReembolso(String politicaReembolso) {
        this.politicaReembolso = politicaReembolso;
    }

    public Recinto getRecinto() {
        return recinto;
    }

    public void setRecinto(Recinto recinto) {
        this.recinto = recinto;
    }

    public List<IObservadorEvento> getObservadores() {
        return observadores;
    }

    @Override
    public String toString() { return nombre + " [" + estado + "] - " + ciudad; }
}
