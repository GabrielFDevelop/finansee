package com.finansee.backend.orcamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.finansee.backend.excecao.RecursoNaoEncontradoException;

@WebMvcTest(OrcamentoController.class)
class OrcamentoControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private OrcamentoService service;

  @Test
  @DisplayName("PUT válido devolve 200 e entrega ano, mês, categoria e limite ao service")
  void definir_dadosValidos_devolve200() throws Exception {
    when(service.definir(eq(2026), eq(11), eq(1L), any(OrcamentoRequest.class)))
        .thenReturn(new OrcamentoResponse(1L, "Alimentação", 2026, 11, new BigDecimal("900.00")));

    mockMvc.perform(put("/api/orcamentos/2026/11/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "valorLimite": 900 }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.categoriaId").value(1))
        .andExpect(jsonPath("$.categoriaNome").value("Alimentação"))
        .andExpect(jsonPath("$.ano").value(2026))
        .andExpect(jsonPath("$.mes").value(11))
        .andExpect(jsonPath("$.valorLimite").value(900.0));

    ArgumentCaptor<OrcamentoRequest> recebido = ArgumentCaptor.forClass(OrcamentoRequest.class);
    verify(service).definir(eq(2026), eq(11), eq(1L), recebido.capture());
    assertThat(recebido.getValue().valorLimite()).isEqualByComparingTo("900");
  }

  @Test
  @DisplayName("PUT com limite zero devolve 400")
  void definir_limiteZero_devolve400() throws Exception {
    mockMvc.perform(put("/api/orcamentos/2026/11/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "valorLimite": 0 }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erros.valorLimite").value("O limite deve ser maior que zero"));

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("PUT sem o campo valorLimite devolve 400")
  void definir_semLimite_devolve400() throws Exception {
    mockMvc.perform(put("/api/orcamentos/2026/11/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erros.valorLimite").value("O limite é obrigatório"));

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("PUT com ano que não é número devolve 400")
  void definir_anoNaoNumerico_devolve400() throws Exception {
    mockMvc.perform(put("/api/orcamentos/abc/11/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            { "valorLimite": 900 }
            """))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("DELETE devolve 204 e chama o service com ano, mês e categoria da URL")
  void remover_devolve204() throws Exception {
    mockMvc.perform(delete("/api/orcamentos/2026/11/1"))
        .andExpect(status().isNoContent());

    verify(service).remover(2026, 11, 1L);
  }

  @Test
  @DisplayName("DELETE de orçamento que não existe devolve 404")
  void remover_inexistente_devolve404() throws Exception {
    doThrow(new RecursoNaoEncontradoException("Não há orçamento definido para essa categoria neste mês"))
        .when(service).remover(2026, 11, 7L);

    mockMvc.perform(delete("/api/orcamentos/2026/11/7"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail")
            .value("Não há orçamento definido para essa categoria neste mês"));
  }
}