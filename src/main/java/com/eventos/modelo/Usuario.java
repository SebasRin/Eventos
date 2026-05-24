package com.eventos.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Usuario {

    private final String idUsuario;
    private String nombre;
    private String correo;
    private String telefono;
    private final List<String> metodosPago = new ArrayList<>();
    private final List<Compra> compras = new ArrayList<>();

    public Usuario(String nombre, String correo, String telefono) {
        this.idUsuario = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
    }

    //gestionar métodos de pago
    public void agregarMetodoPago(String metodo) {
        metodosPago.add(metodo);
    }

    public void eliminarMetodoPago(String metodo) {
        metodosPago.remove(metodo);
    }

    //consultar compras asociadas
    public void agregarCompra(Compra compra) {
        compras.add(compra);
    }




    public String getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<String> getMetodosPago() {
        return metodosPago;
    }

    public List<Compra> getCompras() {
        return compras;
    }

    @Override
    public String toString() {
        return nombre + " <" + correo + ">";
    }
}
