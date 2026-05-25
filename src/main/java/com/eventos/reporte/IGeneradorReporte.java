package com.eventos.reporte;

import com.eventos.modelo.Compra;
import com.eventos.modelo.TipoReporte;

import java.time.LocalDate;
import java.util.List;

public interface IGeneradorReporte {
    byte[] generar(List<Compra> compras, TipoReporte tipo,
                   LocalDate desde, LocalDate hasta) throws Exception;
}