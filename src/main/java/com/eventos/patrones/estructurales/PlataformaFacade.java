package com.eventos.patrones.estructurales;

import com.eventos.modelo.*;
import com.eventos.patrones.comportamiento.*;
import com.eventos.patrones.creacionales.*;
import com.eventos.reporte.*;

import java.time.LocalDate;
import java.util.List;

public class PlataformaFacade {

    private static PlataformaFacade instancia;

    private final HistorialComandos      historial  = new HistorialComandos();
    private final NotificadorEmail       notifEmail = new NotificadorEmail();
    private final NotificadorSMS         notifSMS   = new NotificadorSMS();
    private final RegistradorIncidencias regInc     = new RegistradorIncidencias();

    private PlataformaFacade() {}

    public static PlataformaFacade getInstance() {
        if (instancia == null) {
            instancia = new PlataformaFacade();
        }
        return instancia;
    }

    public List<Evento> buscarEventos(String ciudad, CategoriaEvento categoria) {
        return GestorSistema.getInstance().eventos().buscar(ciudad, categoria);
    }

    public Compra crearCompra(Usuario usuario, Evento evento,
                              List<Entrada> entradas, List<String> servicios) {

        CompraBuilder builder = new CompraBuilder()
                .setUsuario(usuario)
                .setEvento(evento);

        for (Entrada entrada : entradas) {
            builder.addEntrada(entrada);
        }

        for (String servicio : servicios) {
            builder.addServicio(servicio);
        }

        Compra compra = builder.build();

        compra.agregarObservador(notifEmail);
        compra.agregarObservador(notifSMS);
        compra.agregarObservador(regInc);

        return compra;
    }

    public boolean pagarCompra(Compra compra, IEstrategiaPago estrategia) {
        Pago pago = new Pago(compra.getTotal(), estrategia);
        ComandoPagarCompra cmd = new ComandoPagarCompra(compra, pago);
        historial.ejecutar(cmd);
        return pago.getEstado() == EstadoPago.APROBADO;
    }

    public void cancelarCompra(Compra compra) {
        ComandoCancelarCompra cmd = new ComandoCancelarCompra(compra);
        historial.ejecutar(cmd);
    }

    public byte[] generarReporte(TipoReporte tipo,
                                 LocalDate desde,
                                 LocalDate hasta,
                                 String formato) throws Exception {

        List<Compra> todasLasCompras = GestorSistema.getInstance().compras().getAll();

        IGeneradorReporte generador;

        if ("PDF".equalsIgnoreCase(formato)) {
            generador = new GeneradorPDF();
        } else {
            generador = new GeneradorCSV();
        }

        return generador.generar(todasLasCompras, tipo, desde, hasta);
    }

    public byte[] generarReporteUsuario(Usuario usuario, TipoReporte tipo,
                                        LocalDate desde, LocalDate hasta,
                                        String formato) throws Exception {

        List<Compra> comprasUsuario = GestorSistema.getInstance().compras()
                .porUsuario(usuario.getIdUsuario());

        IGeneradorReporte generador;
        if ("PDF".equalsIgnoreCase(formato)) {
            generador = new GeneradorPDF();
        } else {
            generador = new GeneradorCSV();
        }

        return generador.generar(comprasUsuario, tipo, desde, hasta);
    }

    public HistorialComandos getHistorial() {
        return historial;
    }
}