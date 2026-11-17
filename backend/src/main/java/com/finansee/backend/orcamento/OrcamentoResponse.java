package com.finansee.backend.orcamento;

import java.math.BigDecimal;

public record OrcamentoResponse(
    Long categoriaId,
    String categoriaNome,
    int ano,
    int mes,
    BigDecimal valorLimite) {

  public static OrcamentoResponse de(OrcamentoMensal orcamento) {
    return new OrcamentoResponse(
        orcamento.getCategoria().getId(),
        orcamento.getCategoria().getNome(),
        orcamento.getAno(),
        orcamento.getMes(),
        orcamento.getValorLimite());
  }
}