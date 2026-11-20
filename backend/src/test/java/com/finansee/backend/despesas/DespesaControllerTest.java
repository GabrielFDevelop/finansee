package com.finansee.backend.despesas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.finansee.backend.despesa.DespesaController;
import com.finansee.backend.despesa.DespesaRequest;
import com.finansee.backend.despesa.DespesaResponse;
import com.finansee.backend.despesa.DespesaService;
import com.finansee.backend.excecao.RecursoNaoEncontradoException;
import com.finansee.backend.excecao.RequisicaoInvalidaException;

@WebMvcTest(DespesaController.class)
class DespesaControllerTest {

  private static final String DESPESA_VALIDA = """
      {
        "descricao": "Almoço",
        "valor": 32.50,
        "data": "2026-11-15",
        "categoriaId": 1
      }
      """;

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private DespesaService service;

  @Test
  @DisplayName("GET com ano e mês repassa os parâmetros ao service e devolve o JSON esperado")
  void listar_comAnoEMes() throws Exception {
    when(service.listarPorMes(2026, 11)).thenReturn(List.of(respostaPadrao()));

    mockMvc.perform(get("/api/despesas").param("ano", "2026").param("mes", "11"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(10))
        .andExpect(jsonPath("$[0].descricao").value("Almoço"))
        .andExpect(jsonPath("$[0].valor").value(32.5))
        .andExpect(jsonPath("$[0].data").value("2026-11-15"))
        .andExpect(jsonPath("$[0].categoriaId").value(1))
        .andExpect(jsonPath("$[0].categoriaNome").value("Alimentação"));

    verify(service).listarPorMes(2026, 11);
  }

  @Test
  @DisplayName("GET sem parâmetros chama o service com nulos (que usa o mês atual)")
  void listar_semParametros() throws Exception {
    when(service.listarPorMes(null, null)).thenReturn(List.of());

    mockMvc.perform(get("/api/despesas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    verify(service).listarPorMes(null, null);
  }

  @Test
  @DisplayName("GET com mês inválido devolve 400 com a mensagem do erro")
  void listar_mesInvalido_devolve400() throws Exception {
    when(service.listarPorMes(2026, 13))
        .thenThrow(new RequisicaoInvalidaException("O mês deve estar entre 1 e 12"));

    mockMvc.perform(get("/api/despesas").param("ano", "2026").param("mes", "13"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("O mês deve estar entre 1 e 12"));
  }

  @Test
  @DisplayName("POST válido devolve 201 e entrega ao service os dados convertidos do JSON")
  void criar_dadosValidos_devolve201() throws Exception {
    when(service.criar(any(DespesaRequest.class))).thenReturn(respostaPadrao());

    mockMvc.perform(post("/api/despesas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(DESPESA_VALIDA))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/api/despesas/10"))
        .andExpect(jsonPath("$.id").value(10));

    ArgumentCaptor<DespesaRequest> recebido = ArgumentCaptor.forClass(DespesaRequest.class);
    verify(service).criar(recebido.capture());
    assertThat(recebido.getValue().descricao()).isEqualTo("Almoço");
    assertThat(recebido.getValue().valor()).isEqualByComparingTo("32.50");
    assertThat(recebido.getValue().data()).isEqualTo(LocalDate.of(2026, 11, 15));
    assertThat(recebido.getValue().categoriaId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("POST sem nenhum campo devolve 400 listando o erro de cada campo")
  void criar_semCampos_devolveUmErroPorCampo() throws Exception {
    mockMvc.perform(post("/api/despesas")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erros.descricao").value("A descrição é obrigatória"))
        .andExpect(jsonPath("$.erros.valor").value("O valor é obrigatório"))
        .andExpect(jsonPath("$.erros.data").value("A data é obrigatória"))
        .andExpect(jsonPath("$.erros.categoriaId").value("A categoria é obrigatória"));

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("POST com valor negativo devolve 400")
  void criar_valorNegativo_devolve400() throws Exception {
    mockMvc.perform(post("/api/despesas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(DESPESA_VALIDA.replace("32.50", "-5")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erros.valor").value("O valor deve ser maior que zero"));

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("POST com mais de duas casas decimais devolve 400")
  void criar_tresCasasDecimais_devolve400() throws Exception {
    mockMvc.perform(post("/api/despesas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(DESPESA_VALIDA.replace("32.50", "10.999")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erros.valor")
            .value("O valor deve ter no máximo 10 dígitos inteiros e 2 casas decimais"));

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("POST com data no formato brasileiro (dd/MM/aaaa) devolve 400")
  void criar_dataEmFormatoBrasileiro_devolve400() throws Exception {
    mockMvc.perform(post("/api/despesas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(DESPESA_VALIDA.replace("2026-11-15", "15/11/2026")))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("POST com categoria inexistente devolve 404")
  void criar_categoriaInexistente_devolve404() throws Exception {
    when(service.criar(any(DespesaRequest.class)))
        .thenThrow(new RecursoNaoEncontradoException("Categoria não encontrada: 1"));

    mockMvc.perform(post("/api/despesas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(DESPESA_VALIDA))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Categoria não encontrada: 1"));
  }

  @Test
  @DisplayName("PUT válido devolve 200 e usa o id da URL")
  void atualizar_dadosValidos_devolve200() throws Exception {
    when(service.atualizar(eq(10L), any(DespesaRequest.class))).thenReturn(respostaPadrao());

    mockMvc.perform(put("/api/despesas/10")
        .contentType(MediaType.APPLICATION_JSON)
        .content(DESPESA_VALIDA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.descricao").value("Almoço"));
  }

  @Test
  @DisplayName("DELETE devolve 204 e chama o service com o id da URL")
  void excluir_devolve204() throws Exception {
    mockMvc.perform(delete("/api/despesas/5"))
        .andExpect(status().isNoContent());

    verify(service).excluir(5L);
  }

  @Test
  @DisplayName("DELETE de despesa inexistente devolve 404")
  void excluir_inexistente_devolve404() throws Exception {
    doThrow(new RecursoNaoEncontradoException("Despesa não encontrada: 99"))
        .when(service).excluir(99L);

    mockMvc.perform(delete("/api/despesas/99"))
        .andExpect(status().isNotFound());
  }

  private static DespesaResponse respostaPadrao() {
    return new DespesaResponse(10L, "Almoço", new BigDecimal("32.50"),
        LocalDate.of(2026, 11, 15), 1L, "Alimentação");
  }
}