package com.finansee.backend.orcamento;

import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finansee.backend.categoria.Categoria;
import com.finansee.backend.categoria.CategoriaRepository;
import com.finansee.backend.comum.PeriodoMensal;
import com.finansee.backend.excecao.RecursoNaoEncontradoException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

  private final OrcamentoMensalRepository repository;
  private final CategoriaRepository categoriaRepository;

  @Transactional(readOnly = true)
  public List<OrcamentoResponse> listar(Integer ano, Integer mes) {
    YearMonth periodo = PeriodoMensal.resolver(ano, mes);
    return repository
        .buscarPorMes((short) periodo.getYear(), (short) periodo.getMonthValue())
        .stream()
        .map(OrcamentoResponse::de)
        .toList();
  }

  @Transactional
  public OrcamentoResponse definir(int ano, int mes, Long categoriaId, OrcamentoRequest request) {
    YearMonth periodo = PeriodoMensal.resolver(ano, mes);
    short anoRef = (short) periodo.getYear();
    short mesRef = (short) periodo.getMonthValue();

    Categoria categoria = categoriaRepository.findById(categoriaId)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Categoria não encontrada: " + categoriaId));

    OrcamentoMensal orcamento = repository
        .findByAnoAndMesAndCategoriaId(anoRef, mesRef, categoriaId)
        .orElseGet(() -> {
          OrcamentoMensal novo = new OrcamentoMensal();
          novo.setAno(anoRef);
          novo.setMes(mesRef);
          novo.setCategoria(categoria);
          return novo;
        });

    orcamento.setValorLimite(request.valorLimite());
    return OrcamentoResponse.de(repository.save(orcamento));
  }

  @Transactional
  public void remover(int ano, int mes, Long categoriaId) {
    YearMonth periodo = PeriodoMensal.resolver(ano, mes);

    OrcamentoMensal orcamento = repository
        .findByAnoAndMesAndCategoriaId(
            (short) periodo.getYear(), (short) periodo.getMonthValue(), categoriaId)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Não há orçamento definido para essa categoria neste mês"));

    repository.delete(orcamento);
  }
}