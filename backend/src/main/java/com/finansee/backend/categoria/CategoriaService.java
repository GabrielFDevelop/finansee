package com.finansee.backend.categoria;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finansee.backend.excecao.ConflitoException;
import com.finansee.backend.excecao.RecursoNaoEncontradoException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

  private final CategoriaRepository repository;

  @Transactional(readOnly = true)
  public List<CategoriaResponse> listar() {
    return repository.findAll(Sort.by("nome"))
        .stream()
        .map(CategoriaResponse::de)
        .toList();
  }

  @Transactional(readOnly = true)
  public CategoriaResponse buscar(Long id) {
    return CategoriaResponse.de(buscarEntidade(id));
  }

  @Transactional
  public CategoriaResponse criar(CategoriaRequest request) {
    String nome = request.nome().trim();

    if (repository.existsByNomeIgnoreCase(nome)) {
      throw new ConflitoException("Já existe uma categoria com o nome '" + nome + "'");
    }

    Categoria categoria = new Categoria();
    categoria.setNome(nome);
    return CategoriaResponse.de(repository.save(categoria));
  }

  @Transactional
  public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
    Categoria categoria = buscarEntidade(id);
    String nome = request.nome().trim();

    if (repository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
      throw new ConflitoException("Já existe uma categoria com o nome '" + nome + "'");
    }

    categoria.setNome(nome);
    return CategoriaResponse.de(categoria);
  }

  @Transactional
  public void excluir(Long id) {
    Categoria categoria = buscarEntidade(id);
    repository.delete(categoria);
    repository.flush();
  }

  private Categoria buscarEntidade(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Categoria não encontrada: " + id));
  }
}