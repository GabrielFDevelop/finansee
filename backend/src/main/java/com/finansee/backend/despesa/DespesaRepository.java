package com.finansee.backend.despesa;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {

  @Query("""
      select d from Despesa d
      join fetch d.categoria
      where d.data between :inicio and :fim
      order by d.data desc, d.id desc
      """)
  List<Despesa> buscarPorPeriodo(@Param("inicio") LocalDate inicio,
      @Param("fim") LocalDate fim);
}