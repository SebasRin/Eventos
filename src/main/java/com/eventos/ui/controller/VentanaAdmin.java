package com.eventos.ui.controller;

import com.eventos.modelo.*;
import com.eventos.patrones.comportamiento.*;
import com.eventos.patrones.creacionales.*;
import com.eventos.patrones.estructurales.PlataformaFacade;
import com.eventos.reporte.GeneradorCSV;
import com.eventos.reporte.GeneradorPDF;
import com.eventos.reporte.IGeneradorReporte;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VentanaAdmin {

    private Stage stage;
    private final PlataformaFacade facade = PlataformaFacade.getInstance();
    private final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void mostrar() {
        stage = new Stage();
        stage.setTitle("Panel de Administración — Plataforma de Eventos");
        stage.setMaximized(true);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: #f5f5ff; -fx-base: #e0e0f0;");

        tabs.getTabs().addAll(
                tabEventos(),
                tabRecintos(),
                tabUsuarios(),
                tabCompras(),
                tabIncidencias(),
                tabDashboard(),
                tabReportes()
        );

        Scene scene = new Scene(tabs, 1200, 800);
        stage.setScene(scene);
        stage.show();
    }

    // ═══════════════════════════════════════════════════════════════════
    // TAB EVENTOS
    // ═══════════════════════════════════════════════════════════════════
    private Tab tabEventos() {
        Tab tab = new Tab("📋 Eventos");
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: #f0f0f5;");

        // Barra de herramientas
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button btnNuevo = new Button("➕ Nuevo Evento");
        Button btnEditar = new Button("✏️ Editar");
        Button btnEliminar = new Button("🗑️ Eliminar");
        Button btnPublicar = new Button("📢 Publicar");
        Button btnPausar = new Button("⏸️ Pausar");
        Button btnCancelar = new Button("❌ Cancelar");
        Button btnFinalizar = new Button("🏁 Finalizar");
        Button btnGestionarZonas = new Button("🏷️ Gestionar Zonas");
        Button btnRefrescar = new Button("🔄 Refrescar");

        String btnStyle = "-fx-cursor:hand;-fx-font-size:12px;";
        btnNuevo.setStyle("-fx-background-color:#2a6a2a;-fx-text-fill:white;" + btnStyle);
        btnEditar.setStyle("-fx-background-color:#3a6a8a;-fx-text-fill:white;" + btnStyle);
        btnEliminar.setStyle("-fx-background-color:#aa3333;-fx-text-fill:white;" + btnStyle);
        btnPublicar.setStyle("-fx-background-color:#2a6a2a;-fx-text-fill:white;" + btnStyle);
        btnPausar.setStyle("-fx-background-color:#cc8800;-fx-text-fill:white;" + btnStyle);
        btnCancelar.setStyle("-fx-background-color:#cc3333;-fx-text-fill:white;" + btnStyle);
        btnFinalizar.setStyle("-fx-background-color:#555555;-fx-text-fill:white;" + btnStyle);
        btnGestionarZonas.setStyle("-fx-background-color:#6a3a8a;-fx-text-fill:white;" + btnStyle);
        btnRefrescar.setStyle("-fx-background-color:#3a3a5a;-fx-text-fill:white;" + btnStyle);

        toolbar.getChildren().addAll(btnNuevo, btnEditar, btnEliminar,
                new Separator(Orientation.VERTICAL),
                btnPublicar, btnPausar, btnCancelar, btnFinalizar,
                new Separator(Orientation.VERTICAL),
                btnGestionarZonas,
                new Separator(Orientation.VERTICAL), btnRefrescar);

        // Tabla de eventos
        TableView<Evento> tablaEventos = new TableView<>();
        tablaEventos.setPrefHeight(350);

        TableColumn<Evento, String> colNombre = new TableColumn<>("Evento");
        colNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        colNombre.setPrefWidth(200);

        TableColumn<Evento, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategoria().toString()));
        colCategoria.setPrefWidth(100);

        TableColumn<Evento, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCiudad()));
        colCiudad.setPrefWidth(120);

        TableColumn<Evento, String> colFecha = new TableColumn<>("Fecha/Hora");
        colFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFechaHora().format(FMT_HORA)));
        colFecha.setPrefWidth(150);

        TableColumn<Evento, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEstado().toString()));
        colEstado.setPrefWidth(100);

        TableColumn<Evento, String> colDisponibilidad = new TableColumn<>("Disponibilidad");
        colDisponibilidad.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDisponibilidadTotal() + "/" + c.getValue().getRecinto().getCapacidadTotal()));
        colDisponibilidad.setPrefWidth(100);

        tablaEventos.getColumns().addAll(colNombre, colCategoria, colCiudad, colFecha, colEstado, colDisponibilidad);
        cargarEventos(tablaEventos);

        // Área de detalle
        TitledPane detallePane = new TitledPane("Detalle del evento seleccionado", null);
        detallePane.setExpanded(true);
        TextArea taDetalle = new TextArea();
        taDetalle.setEditable(false);
        taDetalle.setWrapText(true);
        taDetalle.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
        detallePane.setContent(taDetalle);

        tablaEventos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mostrarDetalleEvento(newVal, taDetalle);
            }
        });

        // Acciones
        btnNuevo.setOnAction(e -> mostrarDialogoNuevoEvento(tablaEventos));
        btnEditar.setOnAction(e -> {
            Evento sel = tablaEventos.getSelectionModel().getSelectedItem();
            if (sel != null) mostrarDialogoEditarEvento(sel, tablaEventos);
            else alerta("Selección requerida", "Seleccione un evento para editar.");
        });
        btnEliminar.setOnAction(e -> {
            Evento sel = tablaEventos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "¿Eliminar '" + sel.getNombre() + "'?", ButtonType.YES, ButtonType.NO);
                if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    GestorSistema.getInstance().eventos().eliminar(sel.getIdEvento());
                    cargarEventos(tablaEventos);
                    alerta("Eliminado", "Evento eliminado correctamente.");
                }
            }
        });
        btnPublicar.setOnAction(e -> {
            Evento sel = tablaEventos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                sel.publicar();
                GestorSistema.getInstance().eventos().guardar(sel);
                cargarEventos(tablaEventos);
                alerta("Evento publicado", sel.getNombre() + " ahora está disponible.");
            }
        });
        btnPausar.setOnAction(e -> {
            Evento sel = tablaEventos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                sel.pausar();
                GestorSistema.getInstance().eventos().guardar(sel);
                cargarEventos(tablaEventos);
                alerta("Evento pausado", sel.getNombre() + " no está visible temporalmente.");
            }
        });
        btnCancelar.setOnAction(e -> {
            Evento sel = tablaEventos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                sel.cancelar();
                GestorSistema.getInstance().eventos().guardar(sel);
                cargarEventos(tablaEventos);
                alerta("Evento cancelado", sel.getNombre() + " ha sido cancelado.");
            }
        });
        btnFinalizar.setOnAction(e -> {
            Evento sel = tablaEventos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                sel.finalizar();
                GestorSistema.getInstance().eventos().guardar(sel);
                cargarEventos(tablaEventos);
                alerta("Evento finalizado", sel.getNombre() + " ha sido finalizado.");
            }
        });
        btnGestionarZonas.setOnAction(e -> {
            Evento sel = tablaEventos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                mostrarDialogoGestionarZonas(sel.getRecinto(), null);
            } else {
                alerta("Selección requerida", "Seleccione un evento para gestionar sus zonas.");
            }
        });
        btnRefrescar.setOnAction(e -> cargarEventos(tablaEventos));

        content.getChildren().addAll(toolbar, tablaEventos, detallePane);
        tab.setContent(content);
        return tab;
    }

    private void cargarEventos(TableView<Evento> tabla) {
        tabla.setItems(FXCollections.observableArrayList(
                GestorSistema.getInstance().eventos().getAll()));
    }

    private void mostrarDetalleEvento(Evento e, TextArea ta) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(e.getIdEvento().substring(0, 8)).append("\n");
        sb.append("Nombre: ").append(e.getNombre()).append("\n");
        sb.append("Categoría: ").append(e.getCategoria()).append("\n");
        sb.append("Estado: ").append(e.getEstado()).append("\n");
        sb.append("Ciudad: ").append(e.getCiudad()).append("\n");
        sb.append("Fecha: ").append(e.getFechaHora().format(FMT_HORA)).append("\n");
        sb.append("Recinto: ").append(e.getRecinto().getNombre()).append("\n");
        sb.append("Dirección: ").append(e.getRecinto().getDireccion()).append("\n");
        sb.append("Descripción: ").append(e.getDescripcion()).append("\n\n");
        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append("ZONAS Y ASIENTOS:\n\n");

        for (Zona z : e.getRecinto().getZonas()) {
            sb.append("┌── Zona: ").append(z.getNombre()).append("\n");
            sb.append("│   Capacidad: ").append(z.getCapacidad()).append("\n");
            sb.append("│   Precio: $").append(String.format("%,.0f", z.getPrecioBase())).append("\n");
            sb.append("│   Disponibles: ").append(z.getDisponibles()).append("\n");
            sb.append("│   Ocupados: ").append(z.getOcupacion()).append("\n");
            sb.append("└──\n");
        }

        sb.append("\n═══════════════════════════════════════════════════════\n");
        sb.append("Políticas:\n");
        sb.append("  Cancelación: ").append(e.getPoliticaCancelacion()).append("\n");
        sb.append("  Reembolso: ").append(e.getPoliticaReembolso()).append("\n");

        ta.setText(sb.toString());
    }

    private void mostrarDialogoNuevoEvento(TableView<Evento> tabla) {
        Stage s = new Stage();
        s.setTitle("Crear nuevo evento");

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f0f0f5;");

        TextField tfNombre = new TextField();
        tfNombre.setPromptText("Nombre del evento");
        tfNombre.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        ComboBox<String> cbTipo = new ComboBox<>(
                FXCollections.observableArrayList("Concierto", "Teatro", "Conferencia"));
        cbTipo.setValue("Concierto");

        ComboBox<Recinto> cbRecinto = new ComboBox<>(
                FXCollections.observableArrayList(GestorSistema.getInstance().recintos().getAll()));
        cbRecinto.setPromptText("Seleccione recinto");

        TextField tfCiudad = new TextField();
        tfCiudad.setPromptText("Ciudad");
        tfCiudad.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        DatePicker dpFecha = new DatePicker(LocalDate.now().plusMonths(1));
        TextField tfHora = new TextField("20:00");
        tfHora.setPromptText("HH:MM");
        tfHora.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        TextArea taDescripcion = new TextArea();
        taDescripcion.setPrefHeight(80);
        taDescripcion.setPromptText("Descripción del evento");
        taDescripcion.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        Button btnCrear = new Button("✅ Crear Evento");
        btnCrear.setStyle("-fx-background-color:#2a6a2a;-fx-text-fill:white;-fx-cursor:hand;");

        Label lblNombre = new Label("Nombre:");
        lblNombre.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblTipo = new Label("Tipo:");
        lblTipo.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblRecinto = new Label("Recinto:");
        lblRecinto.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblCiudad = new Label("Ciudad:");
        lblCiudad.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblFecha = new Label("Fecha:");
        lblFecha.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblHora = new Label("Hora:");
        lblHora.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblDescripcion = new Label("Descripción:");
        lblDescripcion.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");

        btnCrear.setOnAction(e -> {
            if (tfNombre.getText().isBlank() || cbRecinto.getValue() == null) {
                alerta("Error", "Nombre y recinto son obligatorios.");
                return;
            }

            EventoFactory factory;
            CategoriaEvento categoria;
            String tipoSeleccionado = cbTipo.getValue();
            if ("Teatro".equals(tipoSeleccionado)) {
                factory = new TeatroFactory();
                categoria = CategoriaEvento.TEATRO;
            } else if ("Conferencia".equals(tipoSeleccionado)) {
                factory = new ConferenciaFactory();
                categoria = CategoriaEvento.CONFERENCIA;
            } else {
                factory = new ConciertoFactory();
                categoria = CategoriaEvento.CONCIERTO;
            }

            LocalDateTime fechaHora = LocalDateTime.of(dpFecha.getValue(),
                    java.time.LocalTime.parse(tfHora.getText()));

            Evento evento = factory.crearEvento(
                    tfNombre.getText(), taDescripcion.getText(),
                    tfCiudad.getText(), fechaHora, cbRecinto.getValue());

            evento.setCategoria(categoria);
            GestorSistema.getInstance().eventos().guardar(evento);
            cargarEventos(tabla);
            alerta("Evento creado", "Evento '" + evento.getNombre() + "' creado en estado BORRADOR.");
            s.close();
        });

        form.getChildren().addAll(
                lblNombre, tfNombre,
                lblTipo, cbTipo,
                lblRecinto, cbRecinto,
                lblCiudad, tfCiudad,
                lblFecha, dpFecha,
                lblHora, tfHora,
                lblDescripcion, taDescripcion,
                btnCrear
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f0f0f5;");
        s.setScene(new Scene(scroll, 500, 650));
        s.show();
    }

    private void mostrarDialogoEditarEvento(Evento evento, TableView<Evento> tabla) {
        Stage s = new Stage();
        s.setTitle("Editar evento - " + evento.getNombre());

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f0f0f5;");

        TextField tfNombre = new TextField(evento.getNombre());
        tfNombre.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        TextField tfCiudad = new TextField(evento.getCiudad());
        tfCiudad.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        DatePicker dpFecha = new DatePicker(evento.getFechaHora().toLocalDate());

        TextArea taDescripcion = new TextArea(evento.getDescripcion());
        taDescripcion.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        Button btnGuardar = new Button("💾 Guardar cambios");
        btnGuardar.setStyle("-fx-background-color:#3a6a8a;-fx-text-fill:white;-fx-cursor:hand;");

        Label lblNombre = new Label("Nombre:");
        lblNombre.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblCiudad = new Label("Ciudad:");
        lblCiudad.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblFecha = new Label("Fecha:");
        lblFecha.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblDescripcion = new Label("Descripción:");
        lblDescripcion.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");

        btnGuardar.setOnAction(ev -> {
            evento.setNombre(tfNombre.getText());
            evento.setCiudad(tfCiudad.getText());
            evento.setFechaHora(LocalDateTime.of(dpFecha.getValue(), evento.getFechaHora().toLocalTime()));
            evento.setDescripcion(taDescripcion.getText());
            GestorSistema.getInstance().eventos().guardar(evento);
            cargarEventos(tabla);
            alerta("Evento actualizado", "Los cambios fueron guardados.");
            s.close();
        });

        form.getChildren().addAll(
                lblNombre, tfNombre,
                lblCiudad, tfCiudad,
                lblFecha, dpFecha,
                lblDescripcion, taDescripcion,
                btnGuardar
        );

        s.setScene(new Scene(form, 450, 400));
        s.show();
    }

    // ═══════════════════════════════════════════════════════════════════
    // TAB RECINTOS
    // ═══════════════════════════════════════════════════════════════════
    private Tab tabRecintos() {
        Tab tab = new Tab("🏟️ Recintos");
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: #f0f0f5;");

        TableView<Recinto> tablaRecintos = new TableView<>();
        tablaRecintos.setPrefHeight(350);

        TableColumn<Recinto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        colNombre.setPrefWidth(200);

        TableColumn<Recinto, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDireccion()));
        colDireccion.setPrefWidth(300);

        TableColumn<Recinto, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCiudad()));
        colCiudad.setPrefWidth(150);

        TableColumn<Recinto, Integer> colCapacidad = new TableColumn<>("Capacidad Total");
        colCapacidad.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCapacidadTotal()).asObject());
        colCapacidad.setPrefWidth(120);

        TableColumn<Recinto, Integer> colZonas = new TableColumn<>("Zonas");
        colZonas.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getZonas().size()).asObject());
        colZonas.setPrefWidth(80);

        tablaRecintos.getColumns().addAll(colNombre, colDireccion, colCiudad, colCapacidad, colZonas);
        cargarRecintos(tablaRecintos);

        TitledPane detalleZonas = new TitledPane("Zonas del recinto seleccionado", null);
        detalleZonas.setExpanded(true);
        TextArea taZonas = new TextArea();
        taZonas.setEditable(false);
        taZonas.setWrapText(true);
        taZonas.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
        detalleZonas.setContent(taZonas);

        HBox botones = new HBox(10);
        Button btnNuevo = new Button("➕ Nuevo Recinto");
        Button btnEditar = new Button("✏️ Editar");
        Button btnEliminar = new Button("🗑️ Eliminar");
        Button btnGestionarZonas = new Button("🏷️ Gestionar Zonas");
        Button btnRefrescar = new Button("🔄 Refrescar");

        String btnStyle = "-fx-cursor:hand;-fx-font-size:12px;";
        btnNuevo.setStyle("-fx-background-color:#2a6a2a;-fx-text-fill:white;" + btnStyle);
        btnEditar.setStyle("-fx-background-color:#3a6a8a;-fx-text-fill:white;" + btnStyle);
        btnEliminar.setStyle("-fx-background-color:#aa3333;-fx-text-fill:white;" + btnStyle);
        btnGestionarZonas.setStyle("-fx-background-color:#6a3a8a;-fx-text-fill:white;" + btnStyle);
        btnRefrescar.setStyle("-fx-background-color:#3a3a5a;-fx-text-fill:white;" + btnStyle);

        botones.getChildren().addAll(btnNuevo, btnEditar, btnEliminar, btnGestionarZonas, btnRefrescar);

        tablaRecintos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("🏟️ ").append(newVal.getNombre()).append("\n");
                sb.append("📍 ").append(newVal.getDireccion()).append("\n");
                sb.append("🌆 ").append(newVal.getCiudad()).append("\n");
                sb.append("📊 Capacidad total: ").append(newVal.getCapacidadTotal()).append("\n\n");
                sb.append("═══════════════════════════════════════════════════════\n");
                sb.append("📌 ZONAS:\n\n");

                for (Zona z : newVal.getZonas()) {
                    sb.append("┌── Zona: ").append(z.getNombre()).append("\n");
                    sb.append("│   Capacidad: ").append(z.getCapacidad()).append("\n");
                    sb.append("│   Precio base: $").append(String.format("%,.0f", z.getPrecioBase())).append("\n");
                    sb.append("│   Asientos: ").append(z.getAsientos().size()).append("\n");
                    sb.append("│   Disponibles: ").append(z.getDisponibles()).append("\n");
                    sb.append("│   Ocupados: ").append(z.getOcupacion()).append("\n");
                    sb.append("└──\n");
                }
                taZonas.setText(sb.toString());
            }
        });

        btnNuevo.setOnAction(e -> mostrarDialogoNuevoRecinto(tablaRecintos));
        btnEditar.setOnAction(e -> {
            Recinto sel = tablaRecintos.getSelectionModel().getSelectedItem();
            if (sel != null) mostrarDialogoEditarRecinto(sel, tablaRecintos);
            else alerta("Selección requerida", "Seleccione un recinto para editar.");
        });
        btnEliminar.setOnAction(e -> {
            Recinto sel = tablaRecintos.getSelectionModel().getSelectedItem();
            if (sel != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "¿Eliminar el recinto '" + sel.getNombre() + "'?", ButtonType.YES, ButtonType.NO);
                if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    GestorSistema.getInstance().recintos().eliminar(sel.getIdRecinto());
                    cargarRecintos(tablaRecintos);
                    alerta("Eliminado", "Recinto eliminado correctamente.");
                }
            }
        });
        btnGestionarZonas.setOnAction(e -> {
            Recinto sel = tablaRecintos.getSelectionModel().getSelectedItem();
            if (sel != null) mostrarDialogoGestionarZonas(sel, tablaRecintos);
            else alerta("Selección requerida", "Seleccione un recinto para gestionar sus zonas.");
        });
        btnRefrescar.setOnAction(e -> cargarRecintos(tablaRecintos));

        content.getChildren().addAll(
                new Label("🏟️ Gestión de Recintos y Zonas (RF-014, RF-026, RF-027)"),
                new Separator(),
                tablaRecintos,
                detalleZonas,
                botones
        );

        tab.setContent(content);
        return tab;
    }

    private void cargarRecintos(TableView<Recinto> tabla) {
        tabla.setItems(FXCollections.observableArrayList(
                GestorSistema.getInstance().recintos().getAll()));
    }

    private void mostrarDialogoNuevoRecinto(TableView<Recinto> tabla) {
        Stage s = new Stage();
        s.setTitle("Crear nuevo recinto");

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f0f0f5;");

        TextField tfNombre = new TextField();
        tfNombre.setPromptText("Nombre del recinto");
        tfNombre.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        TextField tfDireccion = new TextField();
        tfDireccion.setPromptText("Dirección");
        tfDireccion.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        TextField tfCiudad = new TextField();
        tfCiudad.setPromptText("Ciudad");
        tfCiudad.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        Button btnCrear = new Button("✅ Crear Recinto");
        btnCrear.setStyle("-fx-background-color:#2a6a2a;-fx-text-fill:white;-fx-cursor:hand;");

        Label lblNombre = new Label("Nombre:");
        lblNombre.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblDireccion = new Label("Dirección:");
        lblDireccion.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblCiudad = new Label("Ciudad:");
        lblCiudad.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");

        btnCrear.setOnAction(e -> {
            if (tfNombre.getText().isBlank() || tfDireccion.getText().isBlank() || tfCiudad.getText().isBlank()) {
                alerta("Error", "Todos los campos son obligatorios.");
                return;
            }

            Recinto recinto = new Recinto(tfNombre.getText(), tfDireccion.getText(), tfCiudad.getText());
            GestorSistema.getInstance().recintos().guardar(recinto);
            cargarRecintos(tabla);
            alerta("Recinto creado", "Recinto '" + recinto.getNombre() + "' creado correctamente.");
            s.close();
        });

        form.getChildren().addAll(
                lblNombre, tfNombre,
                lblDireccion, tfDireccion,
                lblCiudad, tfCiudad,
                btnCrear
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f0f0f5;");
        s.setScene(new Scene(scroll, 450, 350));
        s.show();
    }

    private void mostrarDialogoEditarRecinto(Recinto recinto, TableView<Recinto> tabla) {
        Stage s = new Stage();
        s.setTitle("Editar recinto - " + recinto.getNombre());

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f0f0f5;");

        TextField tfNombre = new TextField(recinto.getNombre());
        tfNombre.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        TextField tfDireccion = new TextField(recinto.getDireccion());
        tfDireccion.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        TextField tfCiudad = new TextField(recinto.getCiudad());
        tfCiudad.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");

        Button btnGuardar = new Button("💾 Guardar cambios");
        btnGuardar.setStyle("-fx-background-color:#3a6a8a;-fx-text-fill:white;-fx-cursor:hand;");

        Label lblNombre = new Label("Nombre:");
        lblNombre.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblDireccion = new Label("Dirección:");
        lblDireccion.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblCiudad = new Label("Ciudad:");
        lblCiudad.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");

        btnGuardar.setOnAction(e -> {
            recinto.setNombre(tfNombre.getText());
            recinto.setDireccion(tfDireccion.getText());
            recinto.setCiudad(tfCiudad.getText());
            GestorSistema.getInstance().recintos().guardar(recinto);
            cargarRecintos(tabla);
            alerta("Recinto actualizado", "Los cambios fueron guardados.");
            s.close();
        });

        form.getChildren().addAll(
                lblNombre, tfNombre,
                lblDireccion, tfDireccion,
                lblCiudad, tfCiudad,
                btnGuardar
        );

        s.setScene(new Scene(form, 450, 320));
        s.show();
    }

    private void mostrarDialogoGestionarZonas(Recinto recinto, TableView<Recinto> tablaRecintos) {
        Stage s = new Stage();
        s.setTitle("Gestionar zonas - " + recinto.getNombre());
        s.setMaximized(true);

        VBox main = new VBox(10);
        main.setPadding(new Insets(16));
        main.setStyle("-fx-background-color: #f0f0f5;");

        Label lblInfo = new Label("🏟️ " + recinto.getNombre() + " | Capacidad total: " + recinto.getCapacidadTotal());
        lblInfo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2a2a5a;");

        TableView<Zona> tablaZonas = new TableView<>();
        tablaZonas.setPrefHeight(300);

        TableColumn<Zona, String> colZonaNombre = new TableColumn<>("Zona");
        colZonaNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        colZonaNombre.setPrefWidth(150);

        TableColumn<Zona, Integer> colZonaCapacidad = new TableColumn<>("Capacidad");
        colZonaCapacidad.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCapacidad()).asObject());
        colZonaCapacidad.setPrefWidth(100);

        TableColumn<Zona, Double> colZonaPrecio = new TableColumn<>("Precio Base");
        colZonaPrecio.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrecioBase()).asObject());
        colZonaPrecio.setPrefWidth(120);

        TableColumn<Zona, Integer> colZonaDisponibles = new TableColumn<>("Disponibles");
        colZonaDisponibles.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getDisponibles()).asObject());
        colZonaDisponibles.setPrefWidth(100);

        TableColumn<Zona, Integer> colZonaOcupados = new TableColumn<>("Ocupados");
        colZonaOcupados.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getOcupacion()).asObject());
        colZonaOcupados.setPrefWidth(100);

        tablaZonas.getColumns().addAll(colZonaNombre, colZonaCapacidad, colZonaPrecio, colZonaDisponibles, colZonaOcupados);
        cargarZonas(tablaZonas, recinto);

        TitledPane editPane = new TitledPane("Editar zona seleccionada", null);
        editPane.setExpanded(true);
        GridPane editForm = new GridPane();
        editForm.setHgap(10);
        editForm.setVgap(8);
        editForm.setPadding(new Insets(10));
        editForm.setStyle("-fx-background-color: #ffffff;");

        TextField tfZonaNombre = new TextField();
        tfZonaNombre.setPromptText("Nombre de la zona");
        TextField tfZonaCapacidad = new TextField();
        tfZonaCapacidad.setPromptText("Capacidad");
        TextField tfZonaPrecio = new TextField();
        tfZonaPrecio.setPromptText("Precio base");

        Button btnActualizarZona = new Button("💾 Actualizar zona");
        Button btnEliminarZona = new Button("🗑️ Eliminar zona");
        Button btnNuevaZona = new Button("➕ Nueva zona");

        btnActualizarZona.setStyle("-fx-background-color:#3a6a8a;-fx-text-fill:white;-fx-cursor:hand;");
        btnEliminarZona.setStyle("-fx-background-color:#aa3333;-fx-text-fill:white;-fx-cursor:hand;");
        btnNuevaZona.setStyle("-fx-background-color:#2a6a2a;-fx-text-fill:white;-fx-cursor:hand;");

        editForm.addRow(0, new Label("Nombre:"), tfZonaNombre);
        editForm.addRow(1, new Label("Capacidad:"), tfZonaCapacidad);
        editForm.addRow(2, new Label("Precio base:"), tfZonaPrecio);
        editForm.addRow(3, btnActualizarZona, btnEliminarZona, btnNuevaZona);
        editPane.setContent(editForm);

        TitledPane asientosPane = new TitledPane("Asientos de la zona seleccionada", null);
        asientosPane.setExpanded(false);
        TextArea taAsientos = new TextArea();
        taAsientos.setEditable(false);
        taAsientos.setWrapText(true);
        taAsientos.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
        asientosPane.setContent(taAsientos);

        tablaZonas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tfZonaNombre.setText(newVal.getNombre());
                tfZonaCapacidad.setText(String.valueOf(newVal.getCapacidad()));
                tfZonaPrecio.setText(String.valueOf(newVal.getPrecioBase()));

                StringBuilder sb = new StringBuilder();
                sb.append("📊 Asientos de la zona: ").append(newVal.getNombre()).append("\n");
                sb.append("═══════════════════════════════════════════════════════\n");
                sb.append(String.format("%-6s %-8s %-12s\n", "Fila", "Número", "Estado"));
                sb.append("───────────────────────────────────────────────────────\n");
                for (Asiento a : newVal.getAsientos()) {
                    sb.append(String.format("%-6s %-8d %-12s\n",
                            a.getFila(), a.getNumero(), a.getEstado()));
                }
                taAsientos.setText(sb.toString());
            }
        });

        btnActualizarZona.setOnAction(e -> {
            Zona zona = tablaZonas.getSelectionModel().getSelectedItem();
            if (zona == null) {
                alerta("Error", "Seleccione una zona");
                return;
            }
            try {
                String nuevoNombre = tfZonaNombre.getText();
                int nuevaCapacidad = Integer.parseInt(tfZonaCapacidad.getText());
                double nuevoPrecio = Double.parseDouble(tfZonaPrecio.getText());

                if (nuevaCapacidad < zona.getOcupacion()) {
                    alerta("Error", "No se puede reducir la capacidad por debajo de los asientos ocupados (" + zona.getOcupacion() + ")");
                    return;
                }

                zona.setNombre(nuevoNombre);
                zona.setCapacidad(nuevaCapacidad);
                zona.setPrecioBase(nuevoPrecio);
                cargarZonas(tablaZonas, recinto);
                if (tablaRecintos != null) cargarRecintos(tablaRecintos);
                alerta("Éxito", "Zona actualizada correctamente");
            } catch (NumberFormatException ex) {
                alerta("Error", "Capacidad y precio deben ser números válidos");
            }
        });

        btnEliminarZona.setOnAction(e -> {
            Zona zona = tablaZonas.getSelectionModel().getSelectedItem();
            if (zona == null) {
                alerta("Error", "Seleccione una zona");
                return;
            }
            if (zona.getOcupacion() > 0) {
                alerta("Error", "No se puede eliminar una zona con asientos ocupados");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Eliminar la zona '" + zona.getNombre() + "'?", ButtonType.YES, ButtonType.NO);
            if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                recinto.eliminarZona(zona);
                cargarZonas(tablaZonas, recinto);
                if (tablaRecintos != null) cargarRecintos(tablaRecintos);
                alerta("Éxito", "Zona eliminada correctamente");
            }
        });

        btnNuevaZona.setOnAction(e -> mostrarDialogoNuevaZona(recinto, tablaZonas, tablaRecintos));

        main.getChildren().addAll(lblInfo, new Separator(), tablaZonas, editPane, asientosPane);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f0f0f5;");
        s.setScene(new Scene(scroll, 1000, 700));
        s.show();
    }

    private void cargarZonas(TableView<Zona> tabla, Recinto recinto) {
        tabla.setItems(FXCollections.observableArrayList(recinto.getZonas()));
    }

    private void mostrarDialogoNuevaZona(Recinto recinto, TableView<Zona> tablaZonas, TableView<Recinto> tablaRecintos) {
        Stage s = new Stage();
        s.setTitle("Nueva zona en " + recinto.getNombre());

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f0f0f5;");

        TextField tfNombre = new TextField();
        tfNombre.setPromptText("Nombre de la zona");
        tfNombre.setStyle("-fx-background-color: #ffffff;");

        TextField tfCapacidad = new TextField();
        tfCapacidad.setPromptText("Capacidad");
        tfCapacidad.setStyle("-fx-background-color: #ffffff;");

        TextField tfPrecio = new TextField();
        tfPrecio.setPromptText("Precio base");
        tfPrecio.setStyle("-fx-background-color: #ffffff;");

        Button btnCrear = new Button("✅ Crear zona");
        btnCrear.setStyle("-fx-background-color:#2a6a2a;-fx-text-fill:white;-fx-cursor:hand;");

        Label lblNombre = new Label("Nombre:");
        lblNombre.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblCapacidad = new Label("Capacidad:");
        lblCapacidad.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");
        Label lblPrecio = new Label("Precio base:");
        lblPrecio.setStyle("-fx-text-fill: #2a2a5a; -fx-font-weight: bold;");

        btnCrear.setOnAction(e -> {
            if (tfNombre.getText().isBlank() || tfCapacidad.getText().isBlank() || tfPrecio.getText().isBlank()) {
                alerta("Error", "Todos los campos son obligatorios");
                return;
            }
            try {
                String nombre = tfNombre.getText();
                int capacidad = Integer.parseInt(tfCapacidad.getText());
                double precio = Double.parseDouble(tfPrecio.getText());

                Zona nuevaZona = new Zona(nombre, capacidad, precio);

                int filas = Math.max(1, capacidad / 10);
                int asientosPorFila = capacidad / filas;
                for (int f = 0; f < filas; f++) {
                    String letraFila = String.valueOf((char) ('A' + f));
                    for (int n = 1; n <= asientosPorFila; n++) {
                        nuevaZona.agregarAsiento(new Asiento(letraFila, n));
                    }
                }

                recinto.agregarZona(nuevaZona);
                cargarZonas(tablaZonas, recinto);
                if (tablaRecintos != null) cargarRecintos(tablaRecintos);
                alerta("Éxito", "Zona '" + nombre + "' creada con " + capacidad + " asientos");
                s.close();
            } catch (NumberFormatException ex) {
                alerta("Error", "Capacidad y precio deben ser números válidos");
            }
        });

        form.getChildren().addAll(
                lblNombre, tfNombre,
                lblCapacidad, tfCapacidad,
                lblPrecio, tfPrecio,
                btnCrear
        );

        s.setScene(new Scene(form, 400, 350));
        s.show();
    }

    // ═══════════════════════════════════════════════════════════════════
    // TAB USUARIOS
    // ═══════════════════════════════════════════════════════════════════
    private Tab tabUsuarios() {
        Tab tab = new Tab("👥 Usuarios");
        VBox content = new VBox(10);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: #f0f0f5;");

        TableView<Usuario> tablaUsuarios = new TableView<>();
        tablaUsuarios.setPrefHeight(400);

        TableColumn<Usuario, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        colNombre.setPrefWidth(200);

        TableColumn<Usuario, String> colCorreo = new TableColumn<>("Correo");
        colCorreo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCorreo()));
        colCorreo.setPrefWidth(250);

        TableColumn<Usuario, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTelefono()));
        colTelefono.setPrefWidth(120);

        TableColumn<Usuario, Integer> colCompras = new TableColumn<>("Compras");
        colCompras.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCompras().size()).asObject());
        colCompras.setPrefWidth(80);

        tablaUsuarios.getColumns().addAll(colNombre, colCorreo, colTelefono, colCompras);
        cargarUsuarios(tablaUsuarios);

        HBox botones = new HBox(10);
        Button btnVerCompras = new Button("📋 Ver compras");
        Button btnEliminar = new Button("🗑️ Eliminar usuario");
        Button btnRefrescar = new Button("🔄 Refrescar");

        btnVerCompras.setStyle("-fx-background-color:#3a6a8a;-fx-text-fill:white;-fx-cursor:hand;");
        btnEliminar.setStyle("-fx-background-color:#aa3333;-fx-text-fill:white;-fx-cursor:hand;");
        btnRefrescar.setStyle("-fx-background-color:#3a3a5a;-fx-text-fill:white;-fx-cursor:hand;");

        btnVerCompras.setOnAction(e -> {
            Usuario u = tablaUsuarios.getSelectionModel().getSelectedItem();
            if (u != null) mostrarComprasUsuario(u);
        });

        btnEliminar.setOnAction(e -> {
            Usuario u = tablaUsuarios.getSelectionModel().getSelectedItem();
            if (u != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "¿Eliminar usuario " + u.getNombre() + "?", ButtonType.YES, ButtonType.NO);
                if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    GestorSistema.getInstance().usuarios().eliminar(u.getIdUsuario());
                    cargarUsuarios(tablaUsuarios);
                }
            }
        });

        btnRefrescar.setOnAction(e -> cargarUsuarios(tablaUsuarios));

        botones.getChildren().addAll(btnVerCompras, btnEliminar, btnRefrescar);
        content.getChildren().addAll(tablaUsuarios, botones);
        tab.setContent(content);
        return tab;
    }

    private void cargarUsuarios(TableView<Usuario> tabla) {
        tabla.setItems(FXCollections.observableArrayList(
                GestorSistema.getInstance().usuarios().getAll()));
    }

    private void mostrarComprasUsuario(Usuario usuario) {
        Stage s = new Stage();
        s.setTitle("Compras de " + usuario.getNombre());

        TableView<Compra> tabla = new TableView<>();
        tabla.setPrefHeight(400);

        TableColumn<Compra, String> colEvento = new TableColumn<>("Evento");
        colEvento.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEvento().getNombre()));
        colEvento.setPrefWidth(200);

        TableColumn<Compra, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFechaCreacion().format(FMT)));
        colFecha.setPrefWidth(100);

        TableColumn<Compra, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEstado().toString()));
        colEstado.setPrefWidth(120);

        TableColumn<Compra, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTotal()).asObject());
        colTotal.setPrefWidth(100);

        tabla.getColumns().addAll(colEvento, colFecha, colEstado, colTotal);
        tabla.setItems(FXCollections.observableArrayList(
                GestorSistema.getInstance().compras().porUsuario(usuario.getIdUsuario())));

        VBox root = new VBox(10, tabla);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #f0f0f5;");
        s.setScene(new Scene(root, 700, 500));
        s.show();
    }

    // ═══════════════════════════════════════════════════════════════════
    // TAB COMPRAS
    // ═══════════════════════════════════════════════════════════════════
    private Tab tabCompras() {
        Tab tab = new Tab("🎟️ Compras");
        VBox content = new VBox(10);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: #f0f0f5;");

        TableView<Compra> tablaCompras = new TableView<>();
        tablaCompras.setPrefHeight(450);

        TableColumn<Compra, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getIdCompra().substring(0, 8)));
        colId.setPrefWidth(80);

        TableColumn<Compra, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsuario().getNombre()));
        colUsuario.setPrefWidth(150);

        TableColumn<Compra, String> colEvento = new TableColumn<>("Evento");
        colEvento.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEvento().getNombre()));
        colEvento.setPrefWidth(200);

        TableColumn<Compra, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFechaCreacion().format(FMT)));
        colFecha.setPrefWidth(100);

        TableColumn<Compra, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEstado().toString()));
        colEstado.setPrefWidth(120);

        TableColumn<Compra, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTotal()).asObject());
        colTotal.setPrefWidth(100);

        tablaCompras.getColumns().addAll(colId, colUsuario, colEvento, colFecha, colEstado, colTotal);
        cargarCompras(tablaCompras);

        HBox botones = new HBox(10);
        Button btnCancelar = new Button("❌ Cancelar compra");
        Button btnRefrescar = new Button("🔄 Refrescar");
        btnCancelar.setStyle("-fx-background-color:#aa3333;-fx-text-fill:white;-fx-cursor:hand;");
        btnRefrescar.setStyle("-fx-background-color:#3a3a5a;-fx-text-fill:white;-fx-cursor:hand;");

        btnCancelar.setOnAction(e -> {
            Compra c = tablaCompras.getSelectionModel().getSelectedItem();
            if (c != null) {
                try {
                    facade.cancelarCompra(c);
                    cargarCompras(tablaCompras);
                    alerta("Compra cancelada", "Estado actual: " + c.getEstado());
                } catch (IllegalStateException ex) {
                    alerta("No se puede cancelar", ex.getMessage());
                }
            }
        });

        btnRefrescar.setOnAction(e -> cargarCompras(tablaCompras));
        botones.getChildren().addAll(btnCancelar, btnRefrescar);

        content.getChildren().addAll(tablaCompras, botones);
        tab.setContent(content);
        return tab;
    }

    private void cargarCompras(TableView<Compra> tabla) {
        tabla.setItems(FXCollections.observableArrayList(
                GestorSistema.getInstance().compras().getAll()));
    }

    // ═══════════════════════════════════════════════════════════════════
    // TAB INCIDENCIAS
    // ═══════════════════════════════════════════════════════════════════
    private Tab tabIncidencias() {
        Tab tab = new Tab("⚠️ Incidencias");
        VBox content = new VBox(10);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: #f0f0f5;");

        TableView<Incidencia> tablaIncidencias = new TableView<>();
        tablaIncidencias.setPrefHeight(450);

        TableColumn<Incidencia, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTipo().toString()));
        colTipo.setPrefWidth(150);

        TableColumn<Incidencia, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFecha().format(FMT)));
        colFecha.setPrefWidth(100);

        TableColumn<Incidencia, String> colEntidad = new TableColumn<>("Entidad afectada");
        colEntidad.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEntidadAfectada()));
        colEntidad.setPrefWidth(200);

        TableColumn<Incidencia, String> colDescripcion = new TableColumn<>("Descripción");
        colDescripcion.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescripcion()));
        colDescripcion.setPrefWidth(400);

        tablaIncidencias.getColumns().addAll(colTipo, colFecha, colEntidad, colDescripcion);
        cargarIncidencias(tablaIncidencias);

        Button btnRefrescar = new Button("🔄 Refrescar");
        btnRefrescar.setStyle("-fx-background-color:#3a3a5a;-fx-text-fill:white;-fx-cursor:hand;");
        btnRefrescar.setOnAction(e -> cargarIncidencias(tablaIncidencias));

        content.getChildren().addAll(tablaIncidencias, btnRefrescar);
        tab.setContent(content);
        return tab;
    }

    private void cargarIncidencias(TableView<Incidencia> tabla) {
        tabla.setItems(FXCollections.observableArrayList(
                GestorSistema.getInstance().incidencias().getAll()));
    }

    // ═══════════════════════════════════════════════════════════════════
    // TAB DASHBOARD
    // ═══════════════════════════════════════════════════════════════════
    private Tab tabDashboard() {
        Tab tab = new Tab("📊 Dashboard");
        VBox content = new VBox(15);
        content.setPadding(new Insets(16));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: #f0f0f5;");

        GridPane metricas = new GridPane();
        metricas.setHgap(15);
        metricas.setVgap(10);
        metricas.setAlignment(Pos.CENTER);

        long totalEventos = GestorSistema.getInstance().eventos().getAll().size();
        long totalCompras = GestorSistema.getInstance().compras().getAll().size();
        long totalUsuarios = GestorSistema.getInstance().usuarios().getAll().size();
        double ingresosTotales = calcularIngresosTotales();

        metricas.add(crearTarjeta("🎫 Eventos", String.valueOf(totalEventos)), 0, 0);
        metricas.add(crearTarjeta("🛒 Compras", String.valueOf(totalCompras)), 1, 0);
        metricas.add(crearTarjeta("👥 Usuarios", String.valueOf(totalUsuarios)), 2, 0);
        metricas.add(crearTarjeta("💰 Ingresos", "$" + String.format("%,.0f", ingresosTotales)), 3, 0);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> ventasPorTipo = new BarChart<>(xAxis, yAxis);
        ventasPorTipo.setTitle("Ventas por tipo de evento");
        ventasPorTipo.setPrefHeight(300);
        ventasPorTipo.setStyle("-fx-background-color: #ffffff;");

        XYChart.Series<String, Number> seriesVentas = new XYChart.Series<>();
        seriesVentas.setName("Entradas vendidas");

        long conciertos = contarVentasPorCategoria(CategoriaEvento.CONCIERTO);
        long teatros = contarVentasPorCategoria(CategoriaEvento.TEATRO);
        long conferencias = contarVentasPorCategoria(CategoriaEvento.CONFERENCIA);

        seriesVentas.getData().add(new XYChart.Data<>("Concierto", conciertos));
        seriesVentas.getData().add(new XYChart.Data<>("Teatro", teatros));
        seriesVentas.getData().add(new XYChart.Data<>("Conferencia", conferencias));
        ventasPorTipo.getData().add(seriesVentas);

        PieChart ocupacionZonas = new PieChart();
        ocupacionZonas.setTitle("Top zonas más ocupadas");
        ocupacionZonas.setPrefHeight(300);
        ocupacionZonas.setStyle("-fx-background-color: #ffffff;");

        for (Evento e : GestorSistema.getInstance().eventos().getAll()) {
            for (Zona z : e.getRecinto().getZonas()) {
                int ocu = z.getOcupacion();
                if (ocu > 0) {
                    ocupacionZonas.getData().add(new PieChart.Data(
                            e.getNombre() + " - " + z.getNombre(), ocu));
                }
            }
        }

        long total = totalCompras;
        long canceladas = contarComprasCanceladas();
        double tasa = total > 0 ? (canceladas * 100.0 / total) : 0;

        Label lblTasa = new Label(String.format("📉 Tasa de cancelación: %.1f%% (%d de %d compras)", tasa, canceladas, total));
        lblTasa.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-padding:10px;-fx-text-fill:#cc3333;");

        Button btnRefrescar = new Button("🔄 Refrescar Dashboard");
        btnRefrescar.setStyle("-fx-background-color:#3a3a5a;-fx-text-fill:white;-fx-cursor:hand;");
        btnRefrescar.setOnAction(e -> {
            tab.setContent(null);
            tab.setContent(tabDashboard().getContent());
        });

        content.getChildren().addAll(
                new Label("📊 Panel de Métricas (RF-018, RF-019)"),
                new Separator(),
                metricas,
                ventasPorTipo,
                ocupacionZonas,
                lblTasa,
                btnRefrescar
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f0f0f5;");
        tab.setContent(scroll);
        return tab;
    }

    private VBox crearTarjeta(String titulo, String valor) {
        VBox tarjeta = new VBox(5);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(15));
        tarjeta.setPrefWidth(140);
        tarjeta.setStyle("-fx-background-color:#ffffff;-fx-border-color:#3a3a5a;-fx-border-radius:10;-fx-background-radius:10;-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill:#2a2a5a;-fx-font-size:12px;");
        Label lblValor = new Label(valor);
        lblValor.setStyle("-fx-text-fill:#2a6a2a;-fx-font-size:20px;-fx-font-weight:bold;");
        tarjeta.getChildren().addAll(lblTitulo, lblValor);
        return tarjeta;
    }

    private double calcularIngresosTotales() {
        double total = 0;
        for (Compra c : GestorSistema.getInstance().compras().getAll()) {
            total += c.getTotal();
        }
        return total;
    }

    private long contarVentasPorCategoria(CategoriaEvento categoria) {
        long total = 0;
        for (Compra c : GestorSistema.getInstance().compras().getAll()) {
            if (c.getEvento().getCategoria() == categoria) {
                total += c.getEntradas().size();
            }
        }
        return total;
    }

    private long contarComprasCanceladas() {
        long total = 0;
        for (Compra c : GestorSistema.getInstance().compras().getAll()) {
            if (c.getEstado() == EstadoCompra.CANCELADA || c.getEstado() == EstadoCompra.REEMBOLSADA) {
                total++;
            }
        }
        return total;
    }

    // ═══════════════════════════════════════════════════════════════════
    // TAB REPORTES
    // ═══════════════════════════════════════════════════════════════════
    private Tab tabReportes() {
        Tab tab = new Tab("📄 Reportes");
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: #f0f0f5;");

        ComboBox<TipoReporte> cbTipoReporte = new ComboBox<>(
                FXCollections.observableArrayList(TipoReporte.values()));
        cbTipoReporte.setValue(TipoReporte.VENTAS_POR_PERIODO);

        ComboBox<String> cbFormato = new ComboBox<>(
                FXCollections.observableArrayList("CSV", "PDF"));
        cbFormato.setValue("CSV");

        DatePicker dpDesde = new DatePicker(LocalDate.now().minusMonths(6));
        DatePicker dpHasta = new DatePicker(LocalDate.now());

        Label lblEstado = new Label("");
        lblEstado.setWrapText(true);

        Button btnGenerar = new Button("📥 Generar Reporte");
        btnGenerar.setStyle("-fx-background-color:#3a5a8a;-fx-text-fill:white;-fx-cursor:hand;");

        btnGenerar.setOnAction(e -> {
            try {
                byte[] datos = facade.generarReporte(
                        cbTipoReporte.getValue(),
                        dpDesde.getValue(),
                        dpHasta.getValue(),
                        cbFormato.getValue());

                String ext = cbFormato.getValue().toLowerCase();
                FileChooser fc = new FileChooser();
                fc.setInitialFileName("reporte_" + cbTipoReporte.getValue() + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy")) + "." + ext);                fc.getExtensionFilters().add(
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

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Tipo de reporte:"), cbTipoReporte);
        grid.addRow(1, new Label("Formato:"), cbFormato);
        grid.addRow(2, new Label("Desde:"), dpDesde);
        grid.addRow(3, new Label("Hasta:"), dpHasta);

        content.getChildren().addAll(
                new Label("📄 Generador de Reportes Operativos (RF-046)"),
                new Separator(),
                grid,
                btnGenerar,
                lblEstado
        );

        tab.setContent(content);
        return tab;
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════
    private void alerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }
}