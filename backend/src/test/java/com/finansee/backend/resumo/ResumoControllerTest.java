package com.finansee.backend.resumo;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.finansee.backend.excecao.RequisicaoInvalidaException;

@WebMvcTest(ResumoController.class)
class ResumoControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ResumoService service;

  @Test
  @DisplayName("o JSON do resumo tem exatamente os campos que o Angular consome")
  void resumo_respeitaOContratoComOFrontEnd() throws Exception {
    ResumoMensalResponse resumo = new ResumoMensalResponse(
        2026, 11, new BigDecimal("400.00"), 5L,
        List.of(
            new CategoriaResumo(1L, "Alimentação", new BigDecimal("300.00"), new BigDecimal("75.0")),
            new CategoriaResumo(2L, "Transporte", new BigDecimal("100.00"), new BigDecimal("25.0"))),
        new BigDecimal("900.00"),
        List.of(new OrcamentoResumo(1L, "Alimentação", new BigDecimal("900.00"),
            new BigDecimal("300.00"), new BigDecimal("33.3"))));

    when(service.resumoDoMes(2026, 11)).thenReturn(resumo);

    mockMvc.perform(get("/api/resumo").param("ano", "2026").param("mes", "11"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ano").value(2026))
        .andExpect(jsonPath("$.mes").value(11))
        .andExpect(jsonPath("$.totalGasto").value(400.0))
        .andExpect(jsonPath("$.quantidadeDespesas").value(5))
        .andExpect(jsonPath("$.porCategoria[0].categoriaId").value(1))
        .andExpect(jsonPath("$.porCategoria[0].categoriaNome").value("Alimentação"))
        .andExpect(jsonPath("$.porCategoria[0].total").value(300.0))
        .andExpect(jsonPath("$.porCategoria[0].percentual").value(75.0))
        .andExpect(jsonPath("$.totalOrcado").value(900.0))
        .andExpect(jsonPath("$.orcamentos[0].categoriaNome").value("Alimentação"))
        .andExpect(jsonPath("$.orcamentos[0].limite").value(900.0))
        .andExpect(jsonPath("$.orcamentos[0].gasto").value(300.0))
        .andExpect(jsonPath("$.orcamentos[0].percentualUsado").value(33.3));

    verify(service).resumoDoMes(2026, 11);
  }

  @Test
  @DisplayName("mês inválido devolve 400")
  void resumo_mesInvalido_devolve400() throws Exception {
    when(service.resumoDoMes(2026, 13))
        .thenThrow(new RequisicaoInvalidaException("O mês deve estar entre 1 e 12"));

    mockMvc.perform(get("/api/resumo").param("ano", "2026").param("mes", "13"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("O mês deve estar entre 1 e 12"));
  }
}