package com.eventos.modelo;

import com.eventos.patrones.comportamiento.IEstrategiaPago;

import java.time.LocalDateTime;
import java.util.UUID;


public class Pago {

    private final String idPago;
    private double monto;
    private String metodoDescripcion;
    private LocalDateTime fechaPago;
    private EstadoPago estado;
    private IEstrategiaPago estrategia;   // Strategy

    public Pago(double monto, IEstrategiaPago estrategia) {
        this.idPago   = UUID.randomUUID().toString();
        this.monto    = monto;
        this.estrategia = estrategia;
        this.metodoDescripcion = estrategia.getDescripcion();
        this.estado   = EstadoPago.PENDIENTE;
    }


    public boolean procesar() {
        boolean ok = estrategia.pagar(monto);
        this.fechaPago = LocalDateTime.now();
        if (ok) {
            this.estado = EstadoPago.APROBADO;
        } else {
            this.estado = EstadoPago.RECHAZADO;
        }
        return ok;
    }


    public boolean reembolsar() {
        boolean ok = estrategia.reembolsar(monto);
        if (ok) {
            this.estado = EstadoPago.REEMBOLSADO;
        }
        return ok;
    }

    public String getIdPago() {
        return idPago;
    }

    public double getMonto() {
        return monto;
    }

    public String getMetodoDescripcion() {
        return metodoDescripcion;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public IEstrategiaPago getEstrategia() {
        return estrategia;
    }
}