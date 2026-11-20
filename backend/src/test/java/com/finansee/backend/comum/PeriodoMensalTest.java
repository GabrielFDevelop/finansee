package com.finansee.backend.comum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.YearMonth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.finansee.backend.excecao.RequisicaoInvalidaException;

class PeriodoMensalTest {

  @Test
  @DisplayName("sem ano e sem mês, usa o mês atual")
  void semAnoEMes_usaMesAtual() {
    YearMonth periodo = PeriodoMensal.resolver(null, null);

    assertThat(periodo).isEqualTo(YearMonth.now());
  }

  @Test
  @DisplayName("com ano e mês válidos, devolve exatamente esse período")
  void anoEMesValidos_devolveOPeriodo() {
    YearMonth periodo = PeriodoMensal.resolver(2026, 11);

    assertThat(periodo).isEqualTo(YearMonth.of(2026, 11));
  }

  @Test
  @DisplayName("informar só o ano é erro")
  void soAno_lancaExcecao() {
    assertThatThrownBy(() -> PeriodoMensal.resolver(2026, null))
        .isInstanceOf(RequisicaoInvalidaException.class)
        .hasMessage("Informe ano e mês juntos");
  }

  @Test
  @DisplayName("informar só o mês é erro")
  void soMes_lancaExcecao() {
    assertThatThrownBy(() -> PeriodoMensal.resolver(null, 11))
        .isInstanceOf(RequisicaoInvalidaException.class)
        .hasMessage("Informe ano e mês juntos");
  }

  @ParameterizedTest(name = "mês {0} é inválido")
  @ValueSource(ints = { 0, 13, -1 })
  @DisplayName("mês fora de 1 a 12 é erro")
  void mesForaDoIntervalo_lancaExcecao(int mes) {
    assertThatThrownBy(() -> PeriodoMensal.resolver(2026, mes))
        .isInstanceOf(RequisicaoInvalidaException.class)
        .hasMessage("O mês deve estar entre 1 e 12");
  }

  @ParameterizedTest(name = "ano {0} é inválido")
  @ValueSource(ints = { 1999, 2101 })
  @DisplayName("ano fora de 2000 a 2100 é erro")
  void anoForaDoIntervalo_lancaExcecao(int ano) {
    assertThatThrownBy(() -> PeriodoMensal.resolver(ano, 6))
        .isInstanceOf(RequisicaoInvalidaException.class)
        .hasMessage("O ano deve estar entre 2000 e 2100");
  }

  @ParameterizedTest(name = "mês {0} é aceito")
  @ValueSource(ints = { 1, 12 })
  @DisplayName("os limites do intervalo de meses são aceitos")
  void limitesDoMes_saoAceitos(int mes) {
    assertThat(PeriodoMensal.resolver(2026, mes)).isEqualTo(YearMonth.of(2026, mes));
  }
}