package com.finansee.backend.resumo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finansee.backend.categoria.Categoria;
import com.finansee.backend.despesa.DespesaRepository;
import com.finansee.backend.despesa.TotalPorCategoria;
import com.finansee.backend.orcamento.OrcamentoMensal;
import com.finansee.backend.orcamento.OrcamentoMensalRepository;

@ExtendWith(MockitoExtension.class)
class ResumoServiceTest {

  @Mock
  private DespesaRepository despesaRepository;

  @Mock
  private OrcamentoMensalRepository orcamentoRepository;

  @InjectMocks
  private ResumoService service;

  @Test
  @DisplayName("soma o total do mês e calcula o percentual de cada categoria")
  void calculaTotalEPercentuais() {
    // Arrange
    when(despesaRepository.totalPorCategoria(any(), any())).thenReturn(List.of(
        new TotalPorCategoria(1L, "Alimentação", new BigDecimal("300.00")),
        new TotalPorCategoria(2L, "Transporte", new BigDecimal("100.00"))));
    when(despesaRepository.countByDataBetween(any(), any())).thenReturn(5L);
    when(orcamentoRepository.buscarPorMes(any(), any())).thenReturn(List.of());

    // Act
    ResumoMensalResponse resumo = service.resumoDoMes(2026, 11);

    // Assert
    assertThat(resumo.ano()).isEqualTo(2026);
    assertThat(resumo.mes()).isEqualTo(11);
    assertThat(resumo.totalGasto()).isEqualByComparingTo("400.00");
    assertThat(resumo.quantidadeDespesas()).isEqualTo(5);
    assertThat(resumo.porCategoria()).hasSize(2);
    assertThat(resumo.porCategoria().get(0).percentual()).isEqualByComparingTo("75.0");
    assertThat(resumo.porCategoria().get(1).percentual()).isEqualByComparingTo("25.0");
  }

  @Test
  @DisplayName("mês sem despesas devolve zeros, sem erro de divisão por zero")
  void mesSemDespesas_devolveZeros() {
    when(despesaRepository.totalPorCategoria(any(), any())).thenReturn(List.of());
    when(orcamentoRepository.buscarPorMes(any(), any())).thenReturn(List.of());

    ResumoMensalResponse resumo = service.resumoDoMes(2026, 11);

    assertThat(resumo.totalGasto()).isEqualByComparingTo("0");
    assertThat(resumo.quantidadeDespesas()).isZero();
    assertThat(resumo.porCategoria()).isEmpty();
    assertThat(resumo.orcamentos()).isEmpty();
    assertThat(resumo.totalOrcado()).isEqualByComparingTo("0");
  }

  @Test
  @DisplayName("categoria com orçamento e sem despesas aparece com gasto zero")
  void orcamentoSemGasto_aparecComGastoZero() {
    when(despesaRepository.totalPorCategoria(any(), any())).thenReturn(List.of(
        new TotalPorCategoria(1L, "Alimentação", new BigDecimal("300.00"))));
    when(orcamentoRepository.buscarPorMes(any(), any())).thenReturn(List.of(
        orcamento(1L, "Alimentação", "900.00"),
        orcamento(2L, "Transporte", "200.00")));

    ResumoMensalResponse resumo = service.resumoDoMes(2026, 11);

    assertThat(resumo.totalOrcado()).isEqualByComparingTo("1100.00");
    assertThat(resumo.orcamentos()).hasSize(2);

    OrcamentoResumo alimentacao = resumo.orcamentos().get(0);
    assertThat(alimentacao.gasto()).isEqualByComparingTo("300.00");
    assertThat(alimentacao.percentualUsado()).isEqualByComparingTo("33.3");

    OrcamentoResumo transporte = resumo.orcamentos().get(1);
    assertThat(transporte.gasto()).isEqualByComparingTo("0");
    assertThat(transporte.percentualUsado()).isEqualByComparingTo("0");
  }

  @Test
  @DisplayName("o percentual usado pode passar de 100 quando o limite é estourado")
  void gastoAcimaDoLimite_passaDeCem() {
    when(despesaRepository.totalPorCategoria(any(), any())).thenReturn(List.of(
        new TotalPorCategoria(1L, "Lazer", new BigDecimal("150.00"))));
    when(orcamentoRepository.buscarPorMes(any(), any())).thenReturn(List.of(
        orcamento(1L, "Lazer", "100.00")));

    ResumoMensalResponse resumo = service.resumoDoMes(2026, 11);

    assertThat(resumo.orcamentos().get(0).percentualUsado()).isEqualByComparingTo("150.0");
  }

  @Test
  @DisplayName("o percentual é arredondado para uma casa decimal")
  void percentual_arredondaParaUmaCasa() {
    when(despesaRepository.totalPorCategoria(any(), any())).thenReturn(List.of(
        new TotalPorCategoria(1L, "Saúde", new BigDecimal("200.00"))));
    when(orcamentoRepository.buscarPorMes(any(), any())).thenReturn(List.of(
        orcamento(1L, "Saúde", "300.00")));

    ResumoMensalResponse resumo = service.resumoDoMes(2026, 11);

    // 200 / 300 = 66,666...% e deve virar 66,7
    assertThat(resumo.orcamentos().get(0).percentualUsado()).isEqualByComparingTo("66.7");
  }

  private static OrcamentoMensal orcamento(long categoriaId, String nomeCategoria, String limite) {
    Categoria categoria = new Categoria();
    categoria.setId(categoriaId);
    categoria.setNome(nomeCategoria);

    OrcamentoMensal orcamento = new OrcamentoMensal();
    orcamento.setAno((short) 2026);
    orcamento.setMes((short) 11);
    orcamento.setValorLimite(new BigDecimal(limite));
    orcamento.setCategoria(categoria);
    return orcamento;
  }
}