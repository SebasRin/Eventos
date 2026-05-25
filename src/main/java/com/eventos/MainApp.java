package com.eventos;

import com.eventos.ui.controller.VentanaAdmin;
import com.eventos.ui.controller.VentanaUsuario;
import com.eventos.util.DatosPrueba;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * ══════════════════════════════════════════════════════════
 *  PUNTO DE ENTRADA — MainApp — RF-045
 * ══════════════════════════════════════════════════════════
 *
 * Flujo de arranque:
 *   1. init()  → carga todos los datos de prueba en memoria
 *   2. start() → muestra la pantalla de selección de perfil
 *
 * La pantalla principal ofrece dos accesos:
 *   👤 Usuario   → VentanaUsuario  (explorar, comprar, mis compras)
 *   ⚙  Admin     → VentanaAdmin    (gestión completa + métricas)
 */
public class MainApp extends Application {

    // ── Ciclo de vida JavaFX ──────────────────────────────────────────

    /**
     * init() se ejecuta ANTES de start(), en hilo de fondo.
     * Es el lugar correcto para cargar datos sin bloquear la UI.
     */
    @Override
    public void init() {
        DatosPrueba.inicializar();
    }

    /**
     * start() construye y muestra la ventana principal.
     * Se ejecuta en el hilo de JavaFX (Application Thread).
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Plataforma de Gestión de Eventos");

        // ── Layout raíz ──────────────────────────────────────────────
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(48));
        root.setStyle("-fx-background-color: #1a1a2e;");

        // ── Encabezado ───────────────────────────────────────────────
        Label lblTitulo = new Label("🎟  Plataforma de Eventos");
        lblTitulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");

        Label lblSubtitulo = new Label("Seleccione su perfil para continuar");
        lblSubtitulo.setStyle("-fx-font-size: 13px; -fx-text-fill: #8888aa;");

        // ── Botones de perfil ─────────────────────────────────────────
        Button btnUsuario = crearBoton("👤   Acceder como Usuario", "#3a3a7a", "#5555aa");
        Button btnAdmin   = crearBoton("⚙    Panel de Administrador", "#5a2a2a", "#8b3333");

        btnUsuario.setOnAction(e -> new VentanaUsuario().mostrar());
        btnAdmin.setOnAction(e -> new VentanaAdmin().mostrar());

        // ── Información de datos de prueba ────────────────────────────
        Label lblInfo = new Label(
                "Correos de prueba:  ana@mail.com  |  carlos@mail.com  |  maria@mail.com");
        lblInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #555577;");

        // ── Versión ───────────────────────────────────────────────────
        Label lblVersion = new Label("PGII — Programación II · Uniquindío · 2026-1");
        lblVersion.setStyle("-fx-font-size: 10px; -fx-text-fill: #444466;");

        root.getChildren().addAll(
                lblTitulo, lblSubtitulo,
                new Separator(),
                btnUsuario, btnAdmin,
                new Separator(),
                lblInfo, lblVersion);

        // ── Escena ────────────────────────────────────────────────────
        Scene scene = new Scene(root, 480, 400);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // ── Helper: crea botón estilizado con efecto hover ────────────────
    private Button crearBoton(String texto, String colorBase, String colorHover) {
        Button btn = new Button(texto);
        btn.setPrefWidth(320);
        btn.setPrefHeight(52);

        String estiloBase  = String.format(
                "-fx-background-color:%s;-fx-text-fill:white;" +
                        "-fx-font-size:14px;-fx-background-radius:8;-fx-cursor:hand;", colorBase);
        String estiloHover = String.format(
                "-fx-background-color:%s;-fx-text-fill:white;" +
                        "-fx-font-size:14px;-fx-background-radius:8;-fx-cursor:hand;", colorHover);

        btn.setStyle(estiloBase);
        btn.setOnMouseEntered(e -> btn.setStyle(estiloHover));
        btn.setOnMouseExited(e  -> btn.setStyle(estiloBase));
        return btn;
    }

    // ── Punto de entrada de la JVM ────────────────────────────────────
    public static void main(String[] args) {
        launch(args);
    }
}
