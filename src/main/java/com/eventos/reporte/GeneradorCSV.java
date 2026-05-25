package com.eventos.reporte;

import com.eventos.modelo.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeneradorCSV implements IGeneradorReporte {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] generar(List<Compra> compras, TipoReporte tipo,
                          LocalDate desde, LocalDate hasta) throws Exception {

        // Filtrar compras por fecha
        List<Compra> filtradas = new ArrayList<>();
        for (Compra c : compras) {
            LocalDate fecha = c.getFechaCreacion().toLocalDate();
            if (!fecha.isBefore(desde) && !fecha.isAfter(hasta)) {
                filtradas.add(c);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

        // BOM para Excel
        baos.write(0xEF);
        baos.write(0xBB);
        baos.write(0xBF);

        switch (tipo) {
            case VENTAS_POR_PERIODO:
                writer.write("ID Compra,Usuario,Evento,Fecha,Total,Estado\n");
                for (Compra c : filtradas) {
                    writer.write(String.format("%s,%s,%s,%s,%.0f,%s\n",
                            c.getIdCompra().substring(0, 8),
                            c.getUsuario().getNombre(),
                            c.getEvento().getNombre(),
                            c.getFechaCreacion().toLocalDate().format(FMT),
                            c.getTotal(),
                            c.getEstado()));
                }
                break;

            case OCUPACION_POR_ZONA:
                writer.write("Evento,Zona,Capacidad,Vendidos,Porcentaje\n");
                // Usar mapa para evitar duplicados
                Map<String, OcupacionDataCSV> mapaOcupacion = new LinkedHashMap<>();
                for (Compra c : filtradas) {
                    for (Zona z : c.getEvento().getRecinto().getZonas()) {
                        String key = c.getEvento().getNombre() + "|" + z.getNombre();
                        if (!mapaOcupacion.containsKey(key)) {
                            OcupacionDataCSV data = new OcupacionDataCSV();
                            data.evento = c.getEvento().getNombre();
                            data.zona = z.getNombre();
                            data.capacidad = z.getCapacidad();
                            data.vendidos = 0;
                            mapaOcupacion.put(key, data);
                        }
                        for (Entrada e : c.getEntradas()) {
                            if (e.getZona().getNombre().equals(z.getNombre())) {
                                mapaOcupacion.get(key).vendidos++;
                            }
                        }
                    }
                }
                for (OcupacionDataCSV data : mapaOcupacion.values()) {
                    double pct = data.capacidad > 0 ? (data.vendidos * 100.0 / data.capacidad) : 0;
                    writer.write(String.format("%s,%s,%d,%d,%.1f%%\n",
                            data.evento, data.zona, data.capacidad, data.vendidos, pct));
                }
                break;

            case TASA_CANCELACION:
                long total = filtradas.size();
                long canceladas = 0;
                for (Compra c : filtradas) {
                    if (c.getEstado() == EstadoCompra.CANCELADA || c.getEstado() == EstadoCompra.REEMBOLSADA) {
                        canceladas++;
                    }
                }
                double tasa = total > 0 ? (canceladas * 100.0 / total) : 0;
                writer.write("Total Compras,Canceladas/Reembolsadas,Tasa de cancelación\n");
                writer.write(String.format("%d,%d,%.2f%%\n", total, canceladas, tasa));
                break;

            case INGRESOS_SERVICIOS:
                writer.write("Servicio,Ingresos\n");
                double vipTotal = 0, seguroTotal = 0, merchTotal = 0, parqTotal = 0;
                for (Compra c : filtradas) {
                    for (String servicio : c.getServiciosAdicionales()) {
                        if (servicio.contains("VIP")) {
                            vipTotal += 80000;
                        } else if (servicio.contains("Seguro")) {
                            seguroTotal += 25000;
                        } else if (servicio.contains("merchandising")) {
                            merchTotal += 45000;
                        } else if (servicio.contains("Parqueadero")) {
                            parqTotal += 15000;
                        }
                    }
                }
                writer.write(String.format("Acceso VIP,%.0f\n", vipTotal));
                writer.write(String.format("Seguro de cancelación,%.0f\n", seguroTotal));
                writer.write(String.format("Kit merchandising,%.0f\n", merchTotal));
                writer.write(String.format("Parqueadero,%.0f\n", parqTotal));
                if (vipTotal + seguroTotal + merchTotal + parqTotal == 0) {
                    writer.write("No se registraron ingresos por servicios,0\n");
                }
                break;

            case TOP_EVENTOS:
                writer.write("Evento,Entradas Vendidas,Ingresos Totales\n");
                Map<String, Integer> ventasEvento = new HashMap<>();
                Map<String, Double> ingresosEvento = new HashMap<>();
                for (Compra c : filtradas) {
                    String nombre = c.getEvento().getNombre();
                    ventasEvento.put(nombre, ventasEvento.getOrDefault(nombre, 0) + c.getEntradas().size());
                    ingresosEvento.put(nombre, ingresosEvento.getOrDefault(nombre, 0.0) + c.getTotal());
                }
                for (String evento : ventasEvento.keySet()) {
                    writer.write(String.format("%s,%d,%.0f\n",
                            evento, ventasEvento.get(evento), ingresosEvento.get(evento)));
                }
                break;

            default:
                writer.write("Reporte: " + tipo + "\n");
                writer.write("Período: " + desde.format(FMT) + " → " + hasta.format(FMT) + "\n");
                writer.write("Total compras: " + filtradas.size() + "\n");
                break;
        }

        writer.flush();
        return baos.toByteArray();
    }

    // Clase auxiliar para ocupación
    private static class OcupacionDataCSV {
        String evento;
        String zona;
        int capacidad;
        int vendidos;
    }
}