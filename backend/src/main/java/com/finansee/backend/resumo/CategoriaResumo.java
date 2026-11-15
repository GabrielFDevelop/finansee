package com.finansee.backend.resumo;

import java.math.BigDecimal;

public record CategoriaResumo(
    Long categoriaId,
    String categoriaNome,
    BigDecimal total,
    BigDecimal percentual) {
}