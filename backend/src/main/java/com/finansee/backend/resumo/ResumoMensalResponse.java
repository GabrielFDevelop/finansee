package com.finansee.backend.resumo;

import java.math.BigDecimal;
import java.util.List;

public record ResumoMensalResponse(
    int ano,
    int mes,
    BigDecimal totalGasto,
    long quantidadeDespesas,
    List<CategoriaResumo> porCategoria) {
}