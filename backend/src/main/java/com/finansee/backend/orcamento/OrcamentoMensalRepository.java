package com.finansee.backend.orcamento;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrcamentoMensalRepository extends JpaRepository<OrcamentoMensal, Long> {

  @Query("""
      select o from OrcamentoMensal o
      join fetch o.categoria c
      where o.ano = :ano and o.mes = :mes
      order by c.nome
      """)
  List<OrcamentoMensal> buscarPorMes(@Param("ano") Short ano, @Param("mes") Short mes);

  Optional<OrcamentoMensal> findByAnoAndMesAndCategoriaId(Short ano, Short mes, Long categoriaId);
}