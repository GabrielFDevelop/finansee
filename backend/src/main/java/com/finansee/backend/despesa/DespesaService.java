package com.finansee.backend.despesa;

import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finansee.backend.categoria.Categoria;
import com.finansee.backend.categoria.CategoriaRepository;
import com.finansee.backend.excecao.RecursoNaoEncontradoException;
import com.finansee.backend.excecao.RequisicaoInvalidaException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DespesaService {

  private final DespesaRepository despesaRepository;
  private final CategoriaRepository categoriaRepository;

  @Transactional(readOnly = true)
  public List<DespesaResponse> listarPorMes(Integer ano, Integer mes) {
    YearMonth periodo = resolverPeriodo(ano, mes);
    return despesaRepository
        .buscarPorPeriodo(periodo.atDay(1), periodo.atEndOfMonth())
        .stream()
        .map(DespesaResponse::de)
        .toList();
  }

  @Transactional(readOnly = true)
  public DespesaResponse buscar(Long id) {
    return DespesaResponse.de(buscarEntidade(id));
  }

  @Transactional
  public DespesaResponse criar(DespesaRequest request) {
    Despesa despesa = new Despesa();
    preencher(despesa, request);
    return DespesaResponse.de(despesaRepository.save(despesa));
  }

  @Transactional
  public DespesaResponse atualizar(Long id, DespesaRequest request) {
    Despesa despesa = buscarEntidade(id);
    preencher(despesa, request);
    return DespesaResponse.de(despesa);
  }

  @Transactional
  public void excluir(Long id) {
    despesaRepository.delete(buscarEntidade(id));
  }

  private void preencher(Despesa despesa, DespesaRequest request) {
    Categoria categoria = categoriaRepository.findById(request.categoriaId())
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Categoria não encontrada: " + request.categoriaId()));

    despesa.setDescricao(request.descricao().trim());
    despesa.setValor(request.valor());
    despesa.setData(request.data());
    despesa.setCategoria(categoria);
  }

  private YearMonth resolverPeriodo(Integer ano, Integer mes) {
    if (ano == null && mes == null) {
      return YearMonth.now();
    }
    if (ano == null || mes == null) {
      throw new RequisicaoInvalidaException("Informe ano e mês juntos");
    }
    if (mes < 1 || mes > 12) {
      throw new RequisicaoInvalidaException("O mês deve estar entre 1 e 12");
    }
    return YearMonth.of(ano, mes);
  }

  private Despesa buscarEntidade(Long id) {
    return despesaRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Despesa não encontrada: " + id));
  }
}