package com.finansee.backend.resumo;

import java.math.BigDecimal;

public record OrcamentoResumo(
    Long categoriaId,
    String categoriaNome,
    BigDecimal limite,
    BigDecimal gasto,
    BigDecimal percentualUsado) {
}