package com.finansee.backend.resumo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finansee.backend.comum.PeriodoMensal;
import com.finansee.backend.despesa.DespesaRepository;
import com.finansee.backend.despesa.TotalPorCategoria;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResumoService {

  private final DespesaRepository despesaRepository;

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

    return new ResumoMensalResponse(
        periodo.getYear(), periodo.getMonthValue(),
        totalGasto, quantidade, porCategoria);
  }

  private BigDecimal percentual(BigDecimal parte, BigDecimal total) {
    if (total.signum() == 0) {
      return BigDecimal.ZERO;
    }
    return parte.multiply(BigDecimal.valueOf(100))
        .divide(total, 1, RoundingMode.HALF_UP);
  }
}