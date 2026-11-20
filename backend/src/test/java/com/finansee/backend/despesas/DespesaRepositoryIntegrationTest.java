package com.finansee.backend.despesas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.finansee.backend.TestcontainersConfiguration;
import com.finansee.backend.categoria.Categoria;
import com.finansee.backend.categoria.CategoriaRepository;
import com.finansee.backend.despesa.Despesa;
import com.finansee.backend.despesa.DespesaRepository;
import com.finansee.backend.despesa.TotalPorCategoria;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class DespesaRepositoryIntegrationTest {

  @Autowired
  private DespesaRepository despesaRepository;

  @Autowired
  private CategoriaRepository categoriaRepository;

  @Test
  @DisplayName("buscarPorPeriodo inclui o primeiro e o último dia, exclui o resto e ordena da mais recente")
  void buscarPorPeriodo_respeitaOsLimitesEAOrdem() {
    Categoria categoria = salvarCategoria("Categoria de teste");
    salvarDespesa("1º de novembro", "10.00", LocalDate.of(2026, 11, 1), categoria);
    salvarDespesa("30 de novembro", "20.00", LocalDate.of(2026, 11, 30), categoria);
    salvarDespesa("31 de outubro", "30.00", LocalDate.of(2026, 10, 31), categoria);
    salvarDespesa("1º de dezembro", "40.00", LocalDate.of(2026, 12, 1), categoria);

    List<Despesa> resultado = despesaRepository.buscarPorPeriodo(
        LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

    assertThat(resultado)
        .extracting(Despesa::getDescricao)
        .containsExactly("30 de novembro", "1º de novembro");
  }

  @Test
  @DisplayName("totalPorCategoria soma por categoria e ordena do maior para o menor")
  void totalPorCategoria_somaEOrdena() {
    Categoria a = salvarCategoria("Categoria A");
    Categoria b = salvarCategoria("Categoria B");
    LocalDate dia = LocalDate.of(2026, 11, 10);
    salvarDespesa("a1", "10.50", dia, a);
    salvarDespesa("a2", "20.25", dia, a);
    salvarDespesa("b1", "100.00", dia, b);

    List<TotalPorCategoria> totais = despesaRepository.totalPorCategoria(
        LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

    assertThat(totais).hasSize(2);
    assertThat(totais.get(0).categoriaNome()).isEqualTo("Categoria B");
    assertThat(totais.get(0).total()).isEqualByComparingTo("100.00");
    assertThat(totais.get(1).categoriaNome()).isEqualTo("Categoria A");
    assertThat(totais.get(1).total()).isEqualByComparingTo("30.75");
  }

  @Test
  @DisplayName("countByDataBetween conta só as despesas do período")
  void countByDataBetween_contaSoOPeriodo() {
    Categoria categoria = salvarCategoria("Categoria de contagem");
    salvarDespesa("dentro 1", "1.00", LocalDate.of(2026, 11, 5), categoria);
    salvarDespesa("dentro 2", "1.00", LocalDate.of(2026, 11, 20), categoria);
    salvarDespesa("fora", "1.00", LocalDate.of(2026, 12, 5), categoria);

    long total = despesaRepository.countByDataBetween(
        LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

    assertThat(total).isEqualTo(2);
  }

  private Categoria salvarCategoria(String nome) {
    Categoria categoria = new Categoria();
    categoria.setNome(nome);
    return categoriaRepository.save(categoria);
  }

  private void salvarDespesa(String descricao, String valor, LocalDate data, Categoria categoria) {
    Despesa despesa = new Despesa();
    despesa.setDescricao(descricao);
    despesa.setValor(new BigDecimal(valor));
    despesa.setData(data);
    despesa.setCategoria(categoria);
    despesaRepository.save(despesa);
  }
}