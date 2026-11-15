package com.finansee.backend.despesa;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaResponse(
    Long id,
    String descricao,
    BigDecimal valor,
    LocalDate data,
    Long categoriaId,
    String categoriaNome) {

  public static DespesaResponse de(Despesa despesa) {
    return new DespesaResponse(
        despesa.getId(),
        despesa.getDescricao(),
        despesa.getValor(),
        despesa.getData(),
        despesa.getCategoria().getId(),
        despesa.getCategoria().getNome());
  }
}