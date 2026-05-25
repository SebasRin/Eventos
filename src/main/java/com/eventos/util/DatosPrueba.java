package com.eventos.util;

import com.eventos.modelo.*;
import com.eventos.patrones.comportamiento.*;
import com.eventos.patrones.creacionales.*;
import com.eventos.patrones.creacionales.ConciertoFactory;
import com.eventos.patrones.creacionales.TeatroFactory;
import com.eventos.patrones.creacionales.ConferenciaFactory;

import java.time.LocalDateTime;

public class DatosPrueba {

    public static void inicializar() {
        GestorSistema gs = GestorSistema.getInstance();

        // ── 1. USUARIOS ───────────────────────────────────────────────
        Usuario ana    = new Usuario("Ana García",   "ana@mail.com",    "3001234567");
        Usuario carlos = new Usuario("Carlos Pérez", "carlos@mail.com", "3109876543");
        Usuario maria  = new Usuario("María López",  "maria@mail.com",  "3205551234");
        Usuario admin  = new Usuario("Admin Sistema","admin@eventos.co","0000000000");

        ana.agregarMetodoPago("Visa *4321");
        ana.agregarMetodoPago("PSE — Bancolombia");
        carlos.agregarMetodoPago("Mastercard *8765");
        carlos.agregarMetodoPago("PSE — Davivienda");
        maria.agregarMetodoPago("Efectivo");

        gs.usuarios().guardar(ana);
        gs.usuarios().guardar(carlos);
        gs.usuarios().guardar(maria);
        gs.usuarios().guardar(admin);

        // ── 2. RECINTOS CON ZONAS Y ASIENTOS ────────────────────────
        Recinto estadio = new Recinto("Estadio Centenario", "Av. 32 #15-70", "Armenia");
        poblarZonas(estadio,
                new String[]{"VIP",        "Preferencial", "General"},
                new int[]   { 100,           300,            800    },
                new double[]{350_000,        180_000,         80_000},
                new int[]   {  2,              4,               8   });
        gs.recintos().guardar(estadio);

        Recinto teatro = new Recinto("Teatro Solís", "Calle 12 #8-20", "Ibagué");
        poblarZonas(teatro,
                new String[]{"Palco",  "Platea", "Galería"},
                new int[]   { 60,        200,       150   },
                new double[]{280_000,   150_000,    70_000},
                new int[]   {  2,          4,          4  });
        gs.recintos().guardar(teatro);

        Recinto convenios = new Recinto("Centro de Convenciones", "Cra 5 #3-10", "Bogotá");
        poblarZonas(convenios,
                new String[]{"Ponente",  "Profesional", "Estudiante"},
                new int[]   { 30,           200,           500      },
                new double[]{500_000,       200_000,        50_000  },
                new int[]   {  1,             4,              8     });
        gs.recintos().guardar(convenios);

        // ── 3. EVENTOS ────────────────────────────────────────────────
        ConciertoFactory   conciertoFab = new ConciertoFactory();
        TeatroFactory      teatroFab    = new TeatroFactory();
        ConferenciaFactory confFab      = new ConferenciaFactory();

        Evento concierto = conciertoFab.crearEvento(
                "Concierto Juanes",
                "El rey del rock en español en vivo",
                "Armenia",
                LocalDateTime.of(2026, 8, 15, 20, 0),
                estadio);
        concierto.publicar();
        gs.eventos().guardar(concierto);

        Evento obra = teatroFab.crearEvento(
                "Hamlet — Compañía Nacional",
                "La tragedia de Shakespeare en versión contemporánea",
                "Ibagué",
                LocalDateTime.of(2026, 9, 5, 19, 30),
                teatro);
        obra.publicar();
        gs.eventos().guardar(obra);

        Evento conferencia = confFab.crearEvento(
                "JavaConf Colombia 2026",
                "Conferencia anual de la comunidad Java",
                "Bogotá",
                LocalDateTime.of(2026, 10, 20, 8, 0),
                convenios);
        conferencia.publicar();
        gs.eventos().guardar(conferencia);

        Evento borrador = conciertoFab.crearEvento(
                "Festival Estéreo Picnic",
                "Festival de música alternativa",
                "Bogotá",
                LocalDateTime.of(2026, 11, 1, 12, 0),
                estadio);

        // ── 4. COMPRAS DE EJEMPLO ─────────────────────────────────────
        crearCompraEjemplo(ana,    concierto,   0, gs);
        crearCompraEjemplo(carlos, concierto,   1, gs);
        crearCompraEjemplo(maria,  conferencia, 2, gs);

        // ── 5. INCIDENCIA DE EJEMPLO ─────────────────────────────────
        gs.registrarIncidencia(
                TipoIncidencia.DOBLE_RESERVA,
                "Intento de doble reserva en asiento A-01 del Concierto Juanes",
                concierto.getIdEvento());

        System.out.println("══════════════════════════════════════════");
        System.out.println(" Datos de prueba inicializados:");
        System.out.println("  Usuarios : " + gs.usuarios().getAll().size());
        System.out.println("  Recintos : " + gs.recintos().getAll().size());
        System.out.println("  Eventos  : " + gs.eventos().getAll().size());
        System.out.println("  Compras  : " + gs.compras().getAll().size());
        System.out.println("  Incidencias: " + gs.incidencias().getAll().size());
        System.out.println("══════════════════════════════════════════");
        System.out.println(" Correos para login:");
        System.out.println("  ana@mail.com | carlos@mail.com | maria@mail.com");
        System.out.println("══════════════════════════════════════════");
    }

    private static void poblarZonas(Recinto recinto,
                                    String[] nombres, int[] caps, double[] precios, int[] numFilas) {

        for (int i = 0; i < nombres.length; i++) {
            Zona zona = new Zona(nombres[i], caps[i], precios[i]);

            int asientosPorFila = caps[i] / numFilas[i];
            for (int f = 0; f < numFilas[i]; f++) {
                String letraFila = String.valueOf((char)('A' + f));
                for (int n = 1; n <= asientosPorFila; n++) {
                    zona.agregarAsiento(new Asiento(letraFila, n));
                }
            }
            recinto.agregarZona(zona);
        }
    }

    private static void crearCompraEjemplo(Usuario usuario, Evento evento,
                                           int zonaIdx, GestorSistema gs) {
        try {
            Zona zona = evento.getRecinto().getZonas().get(zonaIdx);

            // Buscar asiento disponible (sin Stream)
            Asiento asiento = null;
            for (Asiento a : zona.getAsientos()) {
                if (a.getEstado() == EstadoAsiento.DISPONIBLE) {
                    asiento = a;
                    break;
                }
            }

            if (asiento == null) {
                System.err.println("[DATOS] Sin asientos disponibles en " + zona.getNombre());
                return;
            }

            asiento.reservar();
            Entrada entrada = new Entrada(zona, asiento, zona.getPrecioBase());

            CompraBuilder builder = new CompraBuilder()
                    .setUsuario(usuario)
                    .setEvento(evento)
                    .addEntrada(entrada);

            Compra compra = builder.build();

            compra.agregarObservador(new NotificadorEmail());
            compra.agregarObservador(new NotificadorSMS());
            compra.agregarObservador(new RegistradorIncidencias());

            Pago pago = new Pago(compra.getTotal(), new PagoTarjeta("Visa", "0000"));
            compra.pagar(pago);

        } catch (Exception e) {
            System.err.println("[DATOS] Error al crear compra de ejemplo: " + e.getMessage());
        }
    }
}