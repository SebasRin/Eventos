package com.eventos.reporte;

import com.eventos.modelo.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeneradorPDF implements IGeneradorReporte {

    private static final float MARGIN = 40f;
    private static final float LINE_H = 16f;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] generar(List<Compra> compras, TipoReporte tipo,
                          LocalDate desde, LocalDate hasta) throws Exception {

        List<Compra> filtradas = new ArrayList<>();
        for (Compra c : compras) {
            LocalDate f = c.getFechaCreacion().toLocalDate();
            if (!f.isBefore(desde) && !f.isAfter(hasta)) {
                filtradas.add(c);
            }
        }

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
            PDType1Font fontPlain = PDType1Font.HELVETICA;

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = page.getMediaBox().getHeight() - MARGIN;

            // Título
            cs.beginText();
            cs.setFont(fontBold, 14);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("Plataforma de Eventos — Reporte: " + tipo);
            cs.endText();
            y -= LINE_H * 1.5f;

            // Subtítulo
            cs.beginText();
            cs.setFont(fontPlain, 10);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("Período: " + desde.format(FMT) + " al " + hasta.format(FMT)
                    + "   |   Total compras: " + filtradas.size());
            cs.endText();
            y -= LINE_H * 2;

            // Generar según tipo
            switch (tipo) {
                case VENTAS_POR_PERIODO:
                    y = generarReporteVentas(cs, doc, fontBold, fontPlain, filtradas, y);
                    break;
                case OCUPACION_POR_ZONA:
                    y = generarReporteOcupacion(cs, doc, fontBold, fontPlain, filtradas, y);
                    break;
                case TASA_CANCELACION:
                    y = generarReporteCancelacion(cs, doc, fontBold, fontPlain, filtradas, y);
                    break;
                case INGRESOS_SERVICIOS:
                    y = generarReporteIngresosServicios(cs, doc, fontBold, fontPlain, filtradas, y);
                    break;
                case TOP_EVENTOS:
                    y = generarReporteTopEventos(cs, doc, fontBold, fontPlain, filtradas, y);
                    break;
                default:
                    cs.beginText();
                    cs.setFont(fontPlain, 10);
                    cs.newLineAtOffset(MARGIN, y);
                    cs.showText("Reporte no disponible para el tipo seleccionado.");
                    cs.endText();
                    break;
            }

            // Pie de página
            y = MARGIN;
            cs.beginText();
            cs.setFont(fontPlain, 8);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("Generado: " + LocalDate.now().format(FMT)
                    + "  |  Sistema de Gestión de Eventos PGII");
            cs.endText();

            cs.close();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private float generarReporteVentas(PDPageContentStream cs, PDDocument doc,
                                       PDType1Font fontBold, PDType1Font fontPlain,
                                       List<Compra> compras, float y) throws Exception {
        // Encabezados
        cs.beginText();
        cs.setFont(fontBold, 9);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(String.format("%-12s %-20s %-25s %-12s %12s %-12s",
                "ID", "Usuario", "Evento", "Fecha", "Total", "Estado"));
        cs.endText();
        y -= LINE_H;

        // Datos
        cs.setFont(fontPlain, 9);
        for (Compra c : compras) {
            if (y < MARGIN + 50) {
                cs.endText();
                cs.close();
                PDPage nuevaPagina = new PDPage(PDRectangle.A4);
                doc.addPage(nuevaPagina);
                cs = new PDPageContentStream(doc, nuevaPagina);
                y = nuevaPagina.getMediaBox().getHeight() - MARGIN;
                cs.beginText();
                cs.setFont(fontBold, 9);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(String.format("%-12s %-20s %-25s %-12s %12s %-12s",
                        "ID", "Usuario", "Evento", "Fecha", "Total", "Estado"));
                cs.endText();
                y -= LINE_H;
                cs.setFont(fontPlain, 9);
            }
            cs.beginText();
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(String.format("%-12s %-20s %-25s %-12s $%10.0f %-12s",
                    c.getIdCompra().substring(0, 8),
                    truncar(c.getUsuario().getNombre(), 18),
                    truncar(c.getEvento().getNombre(), 23),
                    c.getFechaCreacion().toLocalDate().format(FMT),
                    c.getTotal(),
                    c.getEstado()));
            cs.endText();
            y -= LINE_H;
        }
        return y;
    }

    private float generarReporteOcupacion(PDPageContentStream cs, PDDocument doc,
                                          PDType1Font fontBold, PDType1Font fontPlain,
                                          List<Compra> compras, float y) throws Exception {

        // Usar un mapa para agrupar por evento y zona (sin duplicados)
        java.util.Map<String, OcupacionData> mapaOcupacion = new java.util.LinkedHashMap<>();

        for (Compra c : compras) {
            for (Zona z : c.getEvento().getRecinto().getZonas()) {
                String key = c.getEvento().getNombre() + "|" + z.getNombre();
                if (!mapaOcupacion.containsKey(key)) {
                    OcupacionData data = new OcupacionData();
                    data.evento = c.getEvento().getNombre();
                    data.zona = z.getNombre();
                    data.capacidad = z.getCapacidad();
                    data.vendidos = 0;
                    mapaOcupacion.put(key, data);
                }
                // Contar asientos vendidos en esta compra para esta zona
                for (Entrada e : c.getEntradas()) {
                    if (e.getZona().getNombre().equals(z.getNombre())) {
                        mapaOcupacion.get(key).vendidos++;
                    }
                }
            }
        }

        // Encabezados
        cs.beginText();
        cs.setFont(fontBold, 9);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(String.format("%-30s %-20s %10s %10s %10s",
                "Evento", "Zona", "Capacidad", "Vendidos", "Porcentaje"));
        cs.endText();
        y -= LINE_H;

        cs.setFont(fontPlain, 9);
        for (OcupacionData data : mapaOcupacion.values()) {
            if (y < MARGIN + 50) {
                cs.endText();
                cs.close();
                PDPage nuevaPagina = new PDPage(PDRectangle.A4);
                doc.addPage(nuevaPagina);
                cs = new PDPageContentStream(doc, nuevaPagina);
                y = nuevaPagina.getMediaBox().getHeight() - MARGIN;
                cs.beginText();
                cs.setFont(fontBold, 9);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(String.format("%-30s %-20s %10s %10s %10s",
                        "Evento", "Zona", "Capacidad", "Vendidos", "Porcentaje"));
                cs.endText();
                y -= LINE_H;
                cs.setFont(fontPlain, 9);
            }
            double porcentaje = data.capacidad > 0 ? (data.vendidos * 100.0 / data.capacidad) : 0;
            cs.beginText();
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(String.format("%-30s %-20s %10d %10d %9.1f%%",
                    truncar(data.evento, 28),
                    truncar(data.zona, 18),
                    data.capacidad, data.vendidos, porcentaje));
            cs.endText();
            y -= LINE_H;
        }

        return y; // ⚠️ IMPORTANTE: debe retornar y
    }

    // Clase auxiliar dentro de GeneradorPDF
    private static class OcupacionData {
        String evento;
        String zona;
        int capacidad;
        int vendidos;
    }

    private float generarReporteCancelacion(PDPageContentStream cs, PDDocument doc,
                                            PDType1Font fontBold, PDType1Font fontPlain,
                                            List<Compra> compras, float y) throws Exception {
        long total = compras.size();
        long canceladas = 0;
        for (Compra c : compras) {
            if (c.getEstado() == EstadoCompra.CANCELADA || c.getEstado() == EstadoCompra.REEMBOLSADA) {
                canceladas++;
            }
        }
        double tasa = total > 0 ? (canceladas * 100.0 / total) : 0;

        cs.beginText();
        cs.setFont(fontBold, 12);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("RESUMEN DE CANCELACIONES");
        cs.endText();
        y -= LINE_H * 2;

        cs.beginText();
        cs.setFont(fontPlain, 11);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("Total de compras en el período: " + total);
        cs.endText();
        y -= LINE_H;

        cs.beginText();
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("Compras canceladas/reembolsadas: " + canceladas);
        cs.endText();
        y -= LINE_H;

        cs.beginText();
        cs.setFont(fontBold, 13);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(String.format("Tasa de cancelación: %.2f%%", tasa));
        cs.endText();
        y -= LINE_H * 2;

        if (canceladas > 0) {
            cs.beginText();
            cs.setFont(fontBold, 10);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("Compras canceladas/reembolsadas:");
            cs.endText();
            y -= LINE_H;

            cs.setFont(fontPlain, 9);
            for (Compra c : compras) {
                if (c.getEstado() == EstadoCompra.CANCELADA || c.getEstado() == EstadoCompra.REEMBOLSADA) {
                    if (y < MARGIN + 50) {
                        cs.endText();
                        cs.close();
                        PDPage nuevaPagina = new PDPage(PDRectangle.A4);
                        doc.addPage(nuevaPagina);
                        cs = new PDPageContentStream(doc, nuevaPagina);
                        y = nuevaPagina.getMediaBox().getHeight() - MARGIN;
                        cs.beginText();
                        cs.setFont(fontBold, 9);
                        cs.newLineAtOffset(MARGIN, y);
                        cs.showText("Lista de compras canceladas/reembolsadas:");
                        cs.endText();
                        y -= LINE_H;
                        cs.setFont(fontPlain, 9);
                    }
                    cs.beginText();
                    cs.newLineAtOffset(MARGIN, y);
                    cs.showText(String.format("  • %s - %s - %s - $%.0f",
                            c.getIdCompra().substring(0, 8),
                            c.getUsuario().getNombre(),
                            c.getEvento().getNombre(),
                            c.getTotal()));
                    cs.endText();
                    y -= LINE_H;
                }
            }
        }
        return y;
    }

    private float generarReporteTopEventos(PDPageContentStream cs, PDDocument doc,
                                           PDType1Font fontBold, PDType1Font fontPlain,
                                           List<Compra> compras, float y) throws Exception {

        Map<String, Integer> ventasPorEvento = new LinkedHashMap<>();
        Map<String, Double> ingresosPorEvento = new LinkedHashMap<>();

        for (Compra c : compras) {
            String nombreEvento = c.getEvento().getNombre();
            int numEntradas = c.getEntradas().size();
            double totalCompra = c.getTotal();

            ventasPorEvento.put(nombreEvento, ventasPorEvento.getOrDefault(nombreEvento, 0) + numEntradas);
            ingresosPorEvento.put(nombreEvento, ingresosPorEvento.getOrDefault(nombreEvento, 0.0) + totalCompra);
        }

        // Convertir a lista para ordenar
        List<Map.Entry<String, Integer>> listaVentas = new ArrayList<>(ventasPorEvento.entrySet());
        listaVentas.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Map.Entry<String, Double>> listaIngresos = new ArrayList<>(ingresosPorEvento.entrySet());
        listaIngresos.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Título
        cs.beginText();
        cs.setFont(fontBold, 12);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("TOP EVENTOS POR VENTAS");
        cs.endText();
        y -= LINE_H * 2;

        // Tabla top eventos por cantidad de entradas
        cs.beginText();
        cs.setFont(fontBold, 9);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(String.format("%-4s %-35s %10s", "Pos", "Evento", "Entradas"));
        cs.endText();
        y -= LINE_H;

        cs.setFont(fontPlain, 9);
        int pos = 1;
        for (Map.Entry<String, Integer> entry : listaVentas) {
            if (y < MARGIN + 50) {
                cs.endText();
                cs.close();
                PDPage nuevaPagina = new PDPage(PDRectangle.A4);
                doc.addPage(nuevaPagina);
                cs = new PDPageContentStream(doc, nuevaPagina);
                y = nuevaPagina.getMediaBox().getHeight() - MARGIN;
                cs.beginText();
                cs.setFont(fontBold, 9);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(String.format("%-4s %-35s %10s", "Pos", "Evento", "Entradas"));
                cs.endText();
                y -= LINE_H;
                cs.setFont(fontPlain, 9);
            }
            cs.beginText();
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(String.format("%-4d %-35s %10d", pos++, truncar(entry.getKey(), 33), entry.getValue()));
            cs.endText();
            y -= LINE_H;
        }

        y -= LINE_H;

        // Top eventos por ingresos
        cs.beginText();
        cs.setFont(fontBold, 12);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("TOP EVENTOS POR INGRESOS");
        cs.endText();
        y -= LINE_H * 2;

        cs.beginText();
        cs.setFont(fontBold, 9);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(String.format("%-4s %-35s %15s", "Pos", "Evento", "Ingresos"));
        cs.endText();
        y -= LINE_H;

        cs.setFont(fontPlain, 9);
        pos = 1;
        for (Map.Entry<String, Double> entry : listaIngresos) {
            if (y < MARGIN + 50) {
                cs.endText();
                cs.close();
                PDPage nuevaPagina = new PDPage(PDRectangle.A4);
                doc.addPage(nuevaPagina);
                cs = new PDPageContentStream(doc, nuevaPagina);
                y = nuevaPagina.getMediaBox().getHeight() - MARGIN;
                cs.beginText();
                cs.setFont(fontBold, 9);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(String.format("%-4s %-35s %15s", "Pos", "Evento", "Ingresos"));
                cs.endText();
                y -= LINE_H;
                cs.setFont(fontPlain, 9);
            }
            cs.beginText();
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(String.format("%-4d %-35s $%,14.0f", pos++, truncar(entry.getKey(), 33), entry.getValue()));
            cs.endText();
            y -= LINE_H;
        }

        return y;
    }

    private float generarReporteIngresosServicios(PDPageContentStream cs, PDDocument doc,
                                                  PDType1Font fontBold, PDType1Font fontPlain,
                                                  List<Compra> compras, float y) throws Exception {

        Map<String, Double> ingresosPorServicio = new LinkedHashMap<>();
        ingresosPorServicio.put("Acceso VIP", 0.0);
        ingresosPorServicio.put("Seguro de cancelación", 0.0);
        ingresosPorServicio.put("Kit merchandising", 0.0);
        ingresosPorServicio.put("Parqueadero", 0.0);

        double totalIngresos = 0;

        for (Compra c : compras) {
            for (String servicio : c.getServiciosAdicionales()) {
                if (servicio.contains("VIP")) {
                    ingresosPorServicio.put("Acceso VIP", ingresosPorServicio.get("Acceso VIP") + 80000);
                    totalIngresos += 80000;
                } else if (servicio.contains("Seguro")) {
                    ingresosPorServicio.put("Seguro de cancelación", ingresosPorServicio.get("Seguro de cancelación") + 25000);
                    totalIngresos += 25000;
                } else if (servicio.contains("merchandising")) {
                    ingresosPorServicio.put("Kit merchandising", ingresosPorServicio.get("Kit merchandising") + 45000);
                    totalIngresos += 45000;
                } else if (servicio.contains("Parqueadero")) {
                    ingresosPorServicio.put("Parqueadero", ingresosPorServicio.get("Parqueadero") + 15000);
                    totalIngresos += 15000;
                }
            }
        }

        // Título del reporte
        cs.beginText();
        cs.setFont(fontBold, 12);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("INGRESOS POR SERVICIOS ADICIONALES");
        cs.endText();
        y -= LINE_H * 2;

        cs.beginText();
        cs.setFont(fontPlain, 11);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("Total de compras analizadas: " + compras.size());
        cs.endText();
        y -= LINE_H;

        cs.beginText();
        cs.setFont(fontBold, 11);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("Total ingresos por servicios: $" + String.format("%,.0f", totalIngresos));
        cs.endText();
        y -= LINE_H * 2;

        // Tabla de ingresos por servicio
        cs.beginText();
        cs.setFont(fontBold, 9);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(String.format("%-25s %15s", "Servicio", "Ingresos"));
        cs.endText();
        y -= LINE_H;

        cs.setFont(fontPlain, 9);
        for (Map.Entry<String, Double> entry : ingresosPorServicio.entrySet()) {
            if (entry.getValue() > 0) {
                if (y < MARGIN + 50) {
                    cs.endText();
                    cs.close();
                    PDPage nuevaPagina = new PDPage(PDRectangle.A4);
                    doc.addPage(nuevaPagina);
                    cs = new PDPageContentStream(doc, nuevaPagina);
                    y = nuevaPagina.getMediaBox().getHeight() - MARGIN;
                    cs.beginText();
                    cs.setFont(fontBold, 9);
                    cs.newLineAtOffset(MARGIN, y);
                    cs.showText(String.format("%-25s %15s", "Servicio", "Ingresos"));
                    cs.endText();
                    y -= LINE_H;
                    cs.setFont(fontPlain, 9);
                }
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(String.format("%-25s $%,15.0f", entry.getKey(), entry.getValue()));
                cs.endText();
                y -= LINE_H;
            }
        }

        if (totalIngresos == 0) {
            cs.beginText();
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("No se registraron ingresos por servicios adicionales en el período.");
            cs.endText();
        }

        return y;
    }

    private String truncar(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }
}