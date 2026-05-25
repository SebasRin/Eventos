package com.eventos.ui.controller;

import com.eventos.modelo.*;
import com.eventos.patrones.comportamiento.*;
import com.eventos.patrones.creacionales.GestorSistema;
import com.eventos.patrones.estructurales.PlataformaFacade;
import com.eventos.reporte.GeneradorCSV;
import com.eventos.reporte.GeneradorPDF;
import com.eventos.reporte.IGeneradorReporte;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentanaUsuario {

    private Stage stage;
    private Usuario usuarioActual;
    private final PlataformaFacade facade = PlataformaFacade.getInstance();

    public void mostrar() {
        mostrarLogin();
    }

    // ══════════════════════════════════════════════════════
    //  RF-001: Login
    // ══════════════════════════════════════════════════════
    private void mostrarLogin() {
        stage = new Stage();
        stage.setTitle("Iniciar sesión — Plataforma de Eventos");

        VBox form = new VBox(12);
        form.setPadding(new Insets(36));
        form.setAlignment(Pos.CENTER);
        form.setStyle("-fx-background-color: #f5f5ff;");

        Label lblTitulo = new Label("Iniciar sesión");
        lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2a2a5a;");

        TextField tfCorreo = new TextField();
        tfCorreo.setPromptText("Correo electrónico");
        tfCorreo.setPrefWidth(300);

        PasswordField pfPass = new PasswordField();
        pfPass.setPromptText("Contraseña (cualquier texto — modo prueba)");
        pfPass.setPrefWidth(300);

        Label lblError = new Label("");
        lblError.setStyle("-fx-text-fill: #cc2222;");

        Button btnIngresar = new Button("Ingresar");
        Button btnRegistro = new Button("Crear cuenta nueva");

        btnIngresar.setPrefWidth(300);
        btnIngresar.setStyle("-fx-background-color:#3a3a8a;-fx-text-fill:white;-fx-cursor:hand;-fx-font-size:13px;");

        btnIngresar.setOnAction(e -> {
            String correo = tfCorreo.getText().trim();
            if (correo.isBlank()) { lblError.setText("Ingresa tu correo."); return; }

            Optional<Usuario> encontrado = GestorSistema.getInstance().usuarios().buscarPorCorreo(correo);

            if (encontrado.isPresent()) {
                usuarioActual = encontrado.get();
                stage.close();
                mostrarDashboard();
            } else {
                lblError.setText("Correo no encontrado. Use: ana@mail.com, carlos@mail.com o maria@mail.com");
            }
        });

        btnRegistro.setOnAction(e -> mostrarRegistro());

        tfCorreo.setOnAction(e -> btnIngresar.fire());
        pfPass.setOnAction(e -> btnIngresar.fire());

        Label lblAyuda = new Label("Correos de prueba: ana@mail.com | carlos@mail.com | maria@mail.com");
        lblAyuda.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
        lblAyuda.setWrapText(true);

        form.getChildren().addAll(
                lblTitulo,
                new Label("Correo:"), tfCorreo,
                new Label("Contraseña:"), pfPass,
                lblError,
                btnIngresar,
                new Separator(),
                btnRegistro,
                lblAyuda);

        stage.setScene(new Scene(form, 380, 380));
        stage.show();
    }

    // ══════════════════════════════════════════════════════
    //  RF-001: Registro de nuevo usuario
    // ══════════════════════════════════════════════════════
    private void mostrarRegistro() {
        Stage s = new Stage();
        s.setTitle("Crear cuenta");

        VBox form = new VBox(10);
        form.setPadding(new Insets(24));

        TextField tfNombre = new TextField();
        tfNombre.setPromptText("Nombre completo");
        TextField tfCorreo = new TextField();
        tfCorreo.setPromptText("Correo electrónico");
        TextField tfTelefono = new TextField();
        tfTelefono.setPromptText("Teléfono");

        Label lblError = new Label("");
        lblError.setStyle("-fx-text-fill: red;");

        Button btnGuardar = new Button("✅ Registrarse");
        btnGuardar.setStyle("-fx-background-color:#2a6a2a;-fx-text-fill:white;-fx-cursor:hand;");
        btnGuardar.setPrefWidth(260);

        btnGuardar.setOnAction(e -> {
            if (tfNombre.getText().isBlank() || tfCorreo.getText().isBlank()) {
                lblError.setText("Nombre y correo son obligatorios.");
                return;
            }
            if (GestorSistema.getInstance().usuarios().buscarPorCorreo(tfCorreo.getText()).isPresent()) {
                lblError.setText("Ese correo ya está registrado.");
                return;
            }
            Usuario nuevo = new Usuario(tfNombre.getText(), tfCorreo.getText(), tfTelefono.getText());
            GestorSistema.getInstance().usuarios().guardar(nuevo);
            usuarioActual = nuevo;
            s.close();
            stage.close();
            mostrarDashboard();
        });

        form.getChildren().addAll(
                new Label("Crear nueva cuenta"),
                new Label("Nombre:"), tfNombre,
                new Label("Correo:"), tfCorreo,
                new Label("Teléfono:"), tfTelefono,
                lblError, btnGuardar);

        s.setScene(new Scene(form, 320, 280));
        s.show();
    }

    // ══════════════════════════════════════════════════════
    //  Dashboard principal tras iniciar sesión
    // ══════════════════════════════════════════════════════
    private void mostrarDashboard() {
        Stage dash = new Stage();
        dash.setTitle("Bienvenido, " + usuarioActual.getNombre() + " — Portal de Usuario");

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                tabExplorarEventos(),
                tabMisCompras(),
                tabMiPerfil(),
                tabMisReportes()
        );

        dash.setScene(new Scene(tabs, 860, 600));
        dash.show();
    }

    // ══════════════════════════════════════════════════════
    //  Tab: Explorar Eventos
    // ══════════════════════════════════════════════════════
    private Tab tabExplorarEventos() {
        Tab tab = new Tab("🔍 Explorar Eventos");
        VBox content = new VBox(10);
        content.setPadding(new Insets(16));

        HBox filtros = new HBox(10);
        filtros.setAlignment(Pos.CENTER_LEFT);
        TextField tfCiudad = new TextField();
        tfCiudad.setPromptText("Ciudad (vacío = todas)");
        tfCiudad.setPrefWidth(180);

        ComboBox<String> cbCategoria = new ComboBox<>(
                FXCollections.observableArrayList("Todas", "CONCIERTO", "TEATRO", "CONFERENCIA"));
        cbCategoria.setValue("Todas");

        Button btnBuscar = new Button("🔍 Buscar");
        btnBuscar.setStyle("-fx-background-color:#3a3a8a;-fx-text-fill:white;-fx-cursor:hand;");

        filtros.getChildren().addAll(
                new Label("Ciudad:"), tfCiudad,
                new Label("Categoría:"), cbCategoria,
                btnBuscar);

        ListView<Evento> listaEventos = new ListView<>();
        listaEventos.setPrefHeight(200);

        Label lblDetalle = new Label("Seleccione un evento para ver sus detalles.");
        lblDetalle.setWrapText(true);
        lblDetalle.setStyle("-fx-font-size:12px;");

        Button btnComprar = new Button("🛒 Comprar entrada");
        btnComprar.setStyle("-fx-background-color:#2a6a2a;-fx-text-fill:white;-fx-font-size:13px;-fx-cursor:hand;");
        btnComprar.setDisable(true);

        listaEventos.setItems(FXCollections.observableArrayList(facade.buscarEventos("", null)));

        btnBuscar.setOnAction(e -> {
            String ciudad = tfCiudad.getText().trim();
            String catStr = cbCategoria.getValue();
            CategoriaEvento cat = "Todas".equals(catStr) ? null : CategoriaEvento.valueOf(catStr);
            listaEventos.setItems(FXCollections.observableArrayList(facade.buscarEventos(ciudad, cat)));
        });

        listaEventos.setOnMouseClicked(event -> {
            Evento sel = listaEventos.getSelectionModel().getSelectedItem();
            if (sel == null) return;

            StringBuilder sb = new StringBuilder();
            sb.append("📅 ").append(sel.getFechaHora()).append("\n");
            sb.append("📍 ").append(sel.getRecinto().getNombre())
                    .append(", ").append(sel.getCiudad()).append("\n");
            sb.append("📝 ").append(sel.getDescripcion()).append("\n\n");
            sb.append("🪑 Zonas disponibles:\n");
            for (Zona z : sel.getRecinto().getZonas()) {
                sb.append("   • ").append(z.getNombre())
                        .append("  →  $").append(String.format("%,.0f", z.getPrecioBase()))
                        .append("  |  ").append(z.getDisponibles()).append(" asientos libres\n");
            }
            sb.append("\n📋 Política de cancelación: ").append(sel.getPoliticaCancelacion());
            sb.append("\n💰 Política de reembolso:   ").append(sel.getPoliticaReembolso());

            lblDetalle.setText(sb.toString());
            btnComprar.setDisable(false);
        });

        btnComprar.setOnAction(e -> {
            Evento sel = listaEventos.getSelectionModel().getSelectedItem();
            if (sel != null) mostrarDialogoCompra(sel);
        });

        content.getChildren().addAll(filtros, listaEventos, lblDetalle, btnComprar);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    // ══════════════════════════════════════════════════════
    //  Diálogo de compra
    // ══════════════════════════════════════════════════════
    private void mostrarDialogoCompra(Evento evento) {
        Stage s = new Stage();
        s.setTitle("Comprar — " + evento.getNombre());

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        Label lblZona = new Label("1. Seleccione la zona:");
        ComboBox<Zona> cbZona = new ComboBox<>(
                FXCollections.observableArrayList(evento.getRecinto().getZonas()));
        cbZona.setPromptText("— Elige una zona —");
        cbZona.setPrefWidth(260);

        Label lblServ = new Label("2. Servicios adicionales (opcionales):");
        CheckBox cbVIP = new CheckBox("Acceso VIP  (+$80.000)");
        CheckBox cbSeguro = new CheckBox("Seguro de cancelación  (+$25.000)");
        CheckBox cbMerch = new CheckBox("Kit merchandising  (+$45.000)");
        CheckBox cbParq = new CheckBox("Parqueadero  (+$15.000)");

        Label lblPago = new Label("3. Método de pago:");
        List<String> metodos = usuarioActual.getMetodosPago();
        ComboBox<String> cbPago = new ComboBox<>(FXCollections.observableArrayList(
                metodos.isEmpty() ? List.of("Efectivo") : metodos));
        if (!cbPago.getItems().isEmpty()) cbPago.setValue(cbPago.getItems().get(0));

        Label lblTotal = new Label("Total: —");
        lblTotal.setStyle("-fx-font-weight:bold;-fx-font-size:14px;");

        Runnable actualizarTotal = () -> {
            Zona z = cbZona.getValue();
            double total = (z != null ? z.getPrecioBase() : 0)
                    + (cbVIP.isSelected() ? 80_000 : 0)
                    + (cbSeguro.isSelected() ? 25_000 : 0)
                    + (cbMerch.isSelected() ? 45_000 : 0)
                    + (cbParq.isSelected() ? 15_000 : 0);
            lblTotal.setText("Total: $" + String.format("%,.0f", total));
        };

        cbZona.setOnAction(e -> actualizarTotal.run());
        cbVIP.setOnAction(e -> actualizarTotal.run());
        cbSeguro.setOnAction(e -> actualizarTotal.run());
        cbMerch.setOnAction(e -> actualizarTotal.run());
        cbParq.setOnAction(e -> actualizarTotal.run());

        Button btnConfirmar = new Button("✅ Confirmar y Pagar");
        btnConfirmar.setStyle("-fx-background-color:#2a6a2a;-fx-text-fill:white;-fx-font-size:13px;-fx-cursor:hand;");
        btnConfirmar.setPrefWidth(260);

        btnConfirmar.setOnAction(e -> {
            Zona zona = cbZona.getValue();
            if (zona == null) {
                alerta("Error", "Seleccione una zona.");
                return;
            }
            if (zona.getDisponibles() == 0) {
                alerta("Error", "Zona sin disponibilidad.");
                return;
            }
            if (cbPago.getValue() == null) {
                alerta("Error", "Seleccione un método de pago.");
                return;
            }

            Asiento asiento = null;
            for (Asiento a : zona.getAsientos()) {
                if (a.getEstado() == EstadoAsiento.DISPONIBLE) {
                    asiento = a;
                    break;
                }
            }
            if (asiento == null) {
                alerta("Error", "No hay asientos disponibles.");
                return;
            }

            asiento.reservar();
            Entrada entrada = new Entrada(zona, asiento, zona.getPrecioBase());

            List<String> servicios = new ArrayList<>();
            if (cbVIP.isSelected()) servicios.add("Acceso VIP (+$80.000)");
            if (cbSeguro.isSelected()) servicios.add("Seguro de cancelación (+$25.000)");
            if (cbMerch.isSelected()) servicios.add("Kit merchandising (+$45.000)");
            if (cbParq.isSelected()) servicios.add("Parqueadero (+$15.000)");

            Compra compra = facade.crearCompra(usuarioActual, evento, List.of(entrada), servicios);

            IEstrategiaPago estrategia = resolverEstrategia(cbPago.getValue());
            boolean ok = facade.pagarCompra(compra, estrategia);

            if (ok) {
                alerta("¡Compra exitosa! 🎉",
                        "Zona: " + zona.getNombre() + "\n" +
                                "Asiento: " + asiento + "\n" +
                                "Método: " + cbPago.getValue() + "\n" +
                                "Total: $" + String.format("%,.0f", compra.getTotal()));
                s.close();
            } else {
                alerta("Error de pago", "El pago no fue procesado. Intente de nuevo.");
                asiento.liberar();
            }
        });

        form.getChildren().addAll(
                new Label("Evento: " + evento.getNombre()),
                new Separator(),
                lblZona, cbZona,
                lblServ, cbVIP, cbSeguro, cbMerch, cbParq,
                lblPago, cbPago,
                lblTotal,
                new Separator(),
                btnConfirmar);

        s.setScene(new Scene(new ScrollPane(form), 360, 460));
        s.setTitle("Comprar entrada — " + evento.getNombre());
        s.show();
    }

    // ══════════════════════════════════════════════════════
    //  Tab: Mis compras
    // ══════════════════════════════════════════════════════
    private Tab tabMisCompras() {
        Tab tab = new Tab("🧾 Mis Compras");
        VBox content = new VBox(10);
        content.setPadding(new Insets(16));

        ListView<Compra> lista = new ListView<>();
        lista.setPrefHeight(240);

        Label lblDetalle = new Label("Seleccione una compra para ver su detalle.");
        lblDetalle.setWrapText(true);
        lblDetalle.setStyle("-fx-font-size:12px;");

        Button btnCancelar = new Button("❌ Cancelar compra");
        Button btnRefrescar = new Button("🔄 Actualizar");

        btnCancelar.setStyle("-fx-background-color:#8a2a2a;-fx-text-fill:white;-fx-cursor:hand;");
        btnCancelar.setDisable(true);

        Runnable cargar = () -> {
            List<Compra> mis = GestorSistema.getInstance().compras()
                    .porUsuario(usuarioActual.getIdUsuario());
            lista.setItems(FXCollections.observableArrayList(mis));
        };
        cargar.run();

        lista.setOnMouseClicked(e -> {
            Compra sel = lista.getSelectionModel().getSelectedItem();
            if (sel == null) return;

            StringBuilder sb = new StringBuilder();
            sb.append("🎫 Evento:  ").append(sel.getEvento().getNombre()).append("\n");
            sb.append("📌 Estado:  ").append(sel.getEstado()).append("\n");
            sb.append("💰 Total:   $").append(String.format("%,.0f", sel.getTotal())).append("\n");
            sb.append("📅 Fecha:   ").append(sel.getFechaCreacion().toLocalDate()).append("\n");
            sb.append("\n🪑 Entradas:\n");
            for (Entrada en : sel.getEntradas()) {
                sb.append("   • ").append(en).append("\n");
            }
            if (!sel.getServiciosAdicionales().isEmpty()) {
                sb.append("\n➕ Servicios: ").append(sel.getServiciosAdicionales());
            }
            if (sel.getPago() != null) {
                sb.append("\n💳 Pago: ").append(sel.getPago().getMetodoDescripcion())
                        .append(" [").append(sel.getPago().getEstado()).append("]");
            }

            lblDetalle.setText(sb.toString());

            btnCancelar.setDisable(sel.getEstado() != EstadoCompra.CREADA &&
                    sel.getEstado() != EstadoCompra.CONFIRMADA);
        });

        btnCancelar.setOnAction(e -> {
            Compra sel = lista.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            try {
                facade.cancelarCompra(sel);
                alerta("Cancelación procesada",
                        "Estado actual: " + sel.getEstado() + "\n" +
                                (sel.getEstado() == EstadoCompra.REEMBOLSADA
                                        ? "Tu reembolso está en camino." : ""));
                cargar.run();
            } catch (IllegalStateException ex) {
                alerta("No se puede cancelar", ex.getMessage());
            }
        });

        btnRefrescar.setOnAction(e -> cargar.run());

        HBox botones = new HBox(8, btnCancelar, btnRefrescar);
        content.getChildren().addAll(lista, lblDetalle, botones);
        tab.setContent(content);
        return tab;
    }

    // ══════════════════════════════════════════════════════
    //  Tab: Mi Perfil
    // ══════════════════════════════════════════════════════
    private Tab tabMiPerfil() {
        Tab tab = new Tab("👤 Mi Perfil");
        VBox form = new VBox(10);
        form.setPadding(new Insets(16));

        TextField tfNombre = new TextField(usuarioActual.getNombre());
        TextField tfCorreo = new TextField(usuarioActual.getCorreo());
        TextField tfTelefono = new TextField(usuarioActual.getTelefono());

        Button btnGuardar = new Button("💾 Guardar cambios");
        btnGuardar.setStyle("-fx-background-color:#3a3a8a;-fx-text-fill:white;-fx-cursor:hand;");
        btnGuardar.setOnAction(e -> {
            usuarioActual.setNombre(tfNombre.getText());
            usuarioActual.setCorreo(tfCorreo.getText());
            usuarioActual.setTelefono(tfTelefono.getText());
            alerta("Perfil actualizado", "Los datos fueron guardados correctamente.");
        });

        Label lblMetodos = new Label("Mis métodos de pago:");
        lblMetodos.setStyle("-fx-font-weight:bold;");
        ListView<String> listaMetodos = new ListView<>(
                FXCollections.observableArrayList(usuarioActual.getMetodosPago()));
        listaMetodos.setPrefHeight(100);

        TextField tfNuevoMetodo = new TextField();
        tfNuevoMetodo.setPromptText("Nuevo método (ej: Nequi, PSE — Bancolombia)");

        Button btnAgregar = new Button("➕ Agregar");
        Button btnEliminar = new Button("❌ Eliminar seleccionado");

        btnAgregar.setOnAction(e -> {
            String m = tfNuevoMetodo.getText().trim();
            if (!m.isBlank()) {
                usuarioActual.agregarMetodoPago(m);
                listaMetodos.setItems(FXCollections.observableArrayList(usuarioActual.getMetodosPago()));
                tfNuevoMetodo.clear();
            }
        });

        btnEliminar.setOnAction(e -> {
            String sel = listaMetodos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                usuarioActual.eliminarMetodoPago(sel);
                listaMetodos.setItems(FXCollections.observableArrayList(usuarioActual.getMetodosPago()));
            }
        });

        form.getChildren().addAll(
                new Label("Nombre:"), tfNombre,
                new Label("Correo:"), tfCorreo,
                new Label("Teléfono:"), tfTelefono,
                btnGuardar,
                new Separator(),
                lblMetodos, listaMetodos,
                new HBox(8, tfNuevoMetodo, btnAgregar),
                btnEliminar);

        tab.setContent(new ScrollPane(form));
        return tab;
    }

    // ══════════════════════════════════════════════════════
    //  Tab: Mis Reportes
    // ══════════════════════════════════════════════════════
    private Tab tabMisReportes() {
        Tab tab = new Tab("📊 Mis Reportes");
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        ComboBox<String> cbFormato = new ComboBox<>(
                FXCollections.observableArrayList("CSV", "PDF"));
        cbFormato.setValue("CSV");

        DatePicker dpDesde = new DatePicker(LocalDate.now().minusMonths(6));
        DatePicker dpHasta = new DatePicker(LocalDate.now());

        Label lblEstado = new Label("");
        lblEstado.setWrapText(true);

        Button btnExportar = new Button("📥 Exportar mis compras");
        btnExportar.setStyle("-fx-background-color:#3a5a8a;-fx-text-fill:white;-fx-cursor:hand;");
        btnExportar.setPrefWidth(240);

        btnExportar.setOnAction(e -> {
            try {
                List<Compra> misCompras = GestorSistema.getInstance().compras()
                        .porUsuario(usuarioActual.getIdUsuario());

                List<Compra> filtradas = new ArrayList<>();
                for (Compra c : misCompras) {
                    LocalDate fecha = c.getFechaCreacion().toLocalDate();
                    if (!fecha.isBefore(dpDesde.getValue()) && !fecha.isAfter(dpHasta.getValue())) {
                        filtradas.add(c);
                    }
                }

                IGeneradorReporte generador;
                if ("PDF".equalsIgnoreCase(cbFormato.getValue())) {
                    generador = new GeneradorPDF();
                } else {
                    generador = new GeneradorCSV();
                }

                byte[] datos = generador.generar(filtradas, TipoReporte.VENTAS_POR_PERIODO,
                        dpDesde.getValue(), dpHasta.getValue());

                String ext = cbFormato.getValue().toLowerCase();
                FileChooser fc = new FileChooser();
                fc.setInitialFileName("mis_compras_" + usuarioActual.getNombre().replace(" ", "_") + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy")) + "." + ext);                fc.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter(cbFormato.getValue(), "*." + ext));
                File archivo = fc.showSaveDialog(null);

                if (archivo != null) {
                    try (FileOutputStream fos = new FileOutputStream(archivo)) {
                        fos.write(datos);
                    }
                    lblEstado.setText("✅ Reporte guardado: " + archivo.getName());
                    lblEstado.setStyle("-fx-text-fill: green;");
                }
            } catch (Exception ex) {
                lblEstado.setText("❌ Error: " + ex.getMessage());
                lblEstado.setStyle("-fx-text-fill: red;");
            }
        });

        Label lblInfo = new Label("Tienes " + GestorSistema.getInstance().compras()
                .porUsuario(usuarioActual.getIdUsuario()).size() + " compras registradas.");
        lblInfo.setStyle("-fx-text-fill: #2a2a5a; -fx-font-size: 12px;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Formato:"), cbFormato);
        grid.addRow(1, new Label("Desde:"), dpDesde);
        grid.addRow(2, new Label("Hasta:"), dpHasta);

        content.getChildren().addAll(
                new Label("📊 Exportar historial de compras"),
                lblInfo,
                new Separator(),
                grid,
                btnExportar,
                lblEstado
        );

        tab.setContent(content);
        return tab;
    }

    // ══════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════

    private IEstrategiaPago resolverEstrategia(String metodo) {
        if (metodo == null) return new PagoEfectivo();
        if (metodo.contains("PSE")) return new PagoPSE(metodo.replace("PSE — ", "").trim());
        if (metodo.contains("Visa")) return new PagoTarjeta("Visa", "****");
        if (metodo.contains("Mastercard")) return new PagoTarjeta("Mastercard", "****");
        return new PagoEfectivo();
    }

    private void alerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }
}