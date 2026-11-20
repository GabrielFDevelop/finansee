package com.finansee.backend.categoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finansee.backend.excecao.ConflitoException;
import com.finansee.backend.excecao.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

  @Mock
  private CategoriaRepository repository;

  @InjectMocks
  private CategoriaService service;

  @Test
  @DisplayName("criar remove os espaços das pontas do nome antes de salvar")
  void criar_removeEspacosDoNome() {
    when(repository.existsByNomeIgnoreCase("Pets")).thenReturn(false);
    when(repository.save(any(Categoria.class))).thenAnswer(chamada -> chamada.getArgument(0));

    CategoriaResponse resposta = service.criar(new CategoriaRequest("  Pets  "));

    ArgumentCaptor<Categoria> capturado = ArgumentCaptor.forClass(Categoria.class);
    verify(repository).save(capturado.capture());
    assertThat(capturado.getValue().getNome()).isEqualTo("Pets");
    assertThat(resposta.nome()).isEqualTo("Pets");
  }

  @Test
  @DisplayName("criar com nome que já existe lança conflito e não salva nada")
  void criar_nomeDuplicado_lancaConflito() {
    when(repository.existsByNomeIgnoreCase("Lazer")).thenReturn(true);

    assertThatThrownBy(() -> service.criar(new CategoriaRequest("Lazer")))
        .isInstanceOf(ConflitoException.class)
        .hasMessageContaining("Lazer");

    verify(repository, never()).save(any());
  }

  @Test
  @DisplayName("buscar uma categoria inexistente lança recurso não encontrado")
  void buscar_inexistente_lancaNaoEncontrado() {
    when(repository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.buscar(99L))
        .isInstanceOf(RecursoNaoEncontradoException.class);
  }

  @Test
  @DisplayName("atualizar mantendo o próprio nome não é conflito")
  void atualizar_mantendoOProprioNome_naoDaConflito() {
    Categoria existente = categoria(1L, "Lazer");
    when(repository.findById(1L)).thenReturn(Optional.of(existente));
    when(repository.existsByNomeIgnoreCaseAndIdNot("Lazer", 1L)).thenReturn(false);

    CategoriaResponse resposta = service.atualizar(1L, new CategoriaRequest("Lazer"));

    assertThat(resposta.nome()).isEqualTo("Lazer");
  }

  @Test
  @DisplayName("atualizar para o nome de outra categoria lança conflito")
  void atualizar_nomeDeOutraCategoria_lancaConflito() {
    Categoria existente = categoria(1L, "Lazer");
    when(repository.findById(1L)).thenReturn(Optional.of(existente));
    when(repository.existsByNomeIgnoreCaseAndIdNot("Saúde", 1L)).thenReturn(true);

    assertThatThrownBy(() -> service.atualizar(1L, new CategoriaRequest("Saúde")))
        .isInstanceOf(ConflitoException.class);

    assertThat(existente.getNome()).isEqualTo("Lazer");
  }

  private static Categoria categoria(long id, String nome) {
    Categoria categoria = new Categoria();
    categoria.setId(id);
    categoria.setNome(nome);
    return categoria;
  }
}