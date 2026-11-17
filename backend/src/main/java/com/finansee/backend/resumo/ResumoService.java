package com.finansee.backend.resumo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finansee.backend.comum.PeriodoMensal;
import com.finansee.backend.despesa.DespesaRepository;
import com.finansee.backend.despesa.TotalPorCategoria;
import com.finansee.backend.orcamento.OrcamentoMensal;
import com.finansee.backend.orcamento.OrcamentoMensalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResumoService {

  private final DespesaRepository despesaRepository;
  private final OrcamentoMensalRepository orcamentoRepository;

  @Transactional(readOnly = true)
  public ResumoMensalResponse resumoDoMes(Integer ano, Integer mes) {
    YearMonth periodo = PeriodoMensal.resolver(ano, mes);
    LocalDate inicio = periodo.atDay(1);
    LocalDate fim = periodo.atEndOfMonth();

    List<TotalPorCategoria> totais = despesaRepository.totalPorCategoria(inicio, fim);

    BigDecimal totalGasto = totais.stream()
        .map(TotalPorCategoria::total)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    List<CategoriaResumo> porCategoria = totais.stream()
        .map(t -> new CategoriaResumo(
            t.categoriaId(),
            t.categoriaNome(),
            t.total(),
            percentual(t.total(), totalGasto)))
        .toList();

    long quantidade = despesaRepository.countByDataBetween(inicio, fim);

    Map<Long, BigDecimal> gastoPorCategoria = totais.stream()
        .collect(Collectors.toMap(TotalPorCategoria::categoriaId, TotalPorCategoria::total));

    List<OrcamentoMensal> orcamentos = orcamentoRepository.buscarPorMes(
        (short) periodo.getYear(), (short) periodo.getMonthValue());

    List<OrcamentoResumo> orcamentosResumo = orcamentos.stream()
        .map(o -> {
          BigDecimal gasto = gastoPorCategoria.getOrDefault(
              o.getCategoria().getId(), BigDecimal.ZERO);
          return new OrcamentoResumo(
              o.getCategoria().getId(),
              o.getCategoria().getNome(),
              o.getValorLimite(),
              gasto,
              percentual(gasto, o.getValorLimite()));
        })
        .toList();

    BigDecimal totalOrcado = orcamentos.stream()
        .map(OrcamentoMensal::getValorLimite)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new ResumoMensalResponse(
        periodo.getYear(), periodo.getMonthValue(),
        totalGasto, quantidade, porCategoria,
        totalOrcado, orcamentosResumo);
  }

  private BigDecimal percentual(BigDecimal parte, BigDecimal total) {
    if (total.signum() == 0) {
      return BigDecimal.ZERO;
    }
    return parte.multiply(BigDecimal.valueOf(100))
        .divide(total, 1, RoundingMode.HALF_UP);
  }
}