package com.finansee.backend.despesa;

import java.math.BigDecimal;

public record TotalPorCategoria(Long categoriaId, String categoriaNome, BigDecimal total) {
}