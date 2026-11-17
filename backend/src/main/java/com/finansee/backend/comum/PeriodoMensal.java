package com.finansee.backend.comum;

import java.time.YearMonth;

import com.finansee.backend.excecao.RequisicaoInvalidaException;

public final class PeriodoMensal {

  private PeriodoMensal() {
  }

  public static YearMonth resolver(Integer ano, Integer mes) {
    if (ano == null && mes == null) {
      return YearMonth.now();
    }
    if (ano == null || mes == null) {
      throw new RequisicaoInvalidaException("Informe ano e mês juntos");
    }
    if (mes < 1 || mes > 12) {
      throw new RequisicaoInvalidaException("O mês deve estar entre 1 e 12");
    }
    if (ano < 2000 || ano > 2100) {
      throw new RequisicaoInvalidaException("O ano deve estar entre 2000 e 2100");
    }
    return YearMonth.of(ano, mes);
  }
}