package com.finansee.backend.categoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.finansee.backend.excecao.ConflitoException;
import com.finansee.backend.excecao.RecursoNaoEncontradoException;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CategoriaService service;

  @Test
  @DisplayName("GET /api/categorias devolve 200 e a lista em JSON")
  void listar_devolveALista() throws Exception {
    when(service.listar()).thenReturn(List.of(
        new CategoriaResponse(1L, "Alimentação"),
        new CategoriaResponse(2L, "Transporte")));

    mockMvc.perform(get("/api/categorias"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].nome").value("Alimentação"));
  }

  @Test
  @DisplayName("POST válido devolve 201, o cabeçalho Location e a categoria criada")
  void criar_dadosValidos_devolve201() throws Exception {
    when(service.criar(any(CategoriaRequest.class))).thenReturn(new CategoriaResponse(8L, "Pets"));

    mockMvc.perform(post("/api/categorias")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "nome": "Pets" }
            """))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/api/categorias/8"))
        .andExpect(jsonPath("$.id").value(8))
        .andExpect(jsonPath("$.nome").value("Pets"));

    ArgumentCaptor<CategoriaRequest> recebido = ArgumentCaptor.forClass(CategoriaRequest.class);
    verify(service).criar(recebido.capture());
    assertThat(recebido.getValue().nome()).isEqualTo("Pets");
  }

  @Test
  @DisplayName("POST com nome em branco devolve 400 com o erro do campo e não chama o service")
  void criar_nomeEmBranco_devolve400() throws Exception {
    mockMvc.perform(post("/api/categorias")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "nome": "   " }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Dados inválidos"))
        .andExpect(jsonPath("$.erros.nome").value("O nome é obrigatório"));

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("POST com nome de 81 caracteres devolve 400")
  void criar_nomeMuitoLongo_devolve400() throws Exception {
    String json = "{ \"nome\": \"" + "x".repeat(81) + "\" }";

    mockMvc.perform(post("/api/categorias")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erros.nome").value("O nome deve ter no máximo 80 caracteres"));

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("POST quando o service acusa nome duplicado devolve 409 com a mensagem")
  void criar_nomeDuplicado_devolve409() throws Exception {
    when(service.criar(any(CategoriaRequest.class)))
        .thenThrow(new ConflitoException("Já existe uma categoria com o nome 'Lazer'"));

    mockMvc.perform(post("/api/categorias")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "nome": "Lazer" }
            """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Já existe uma categoria com o nome 'Lazer'"));
  }

  @Test
  @DisplayName("GET de uma categoria inexistente devolve 404")
  void buscar_inexistente_devolve404() throws Exception {
    when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Categoria não encontrada: 99"));

    mockMvc.perform(get("/api/categorias/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Categoria não encontrada: 99"));
  }

  @Test
  @DisplayName("DELETE devolve 204 e chama o service com o id da URL")
  void excluir_devolve204() throws Exception {
    mockMvc.perform(delete("/api/categorias/3"))
        .andExpect(status().isNoContent());

    verify(service).excluir(3L);
  }

  @Test
  @DisplayName("DELETE de categoria em uso (violação de integridade) devolve 409")
  void excluir_emUso_devolve409() throws Exception {
    doThrow(new DataIntegrityViolationException("chave estrangeira"))
        .when(service).excluir(1L);

    mockMvc.perform(delete("/api/categorias/1"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail")
            .value("Operação não permitida: o registro está em uso ou viola uma regra de integridade."));
  }

  @Test
  @DisplayName("POST com JSON malformado devolve 400")
  void criar_jsonMalformado_devolve400() throws Exception {
    mockMvc.perform(post("/api/categorias")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ nome: "))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(service);
  }
}