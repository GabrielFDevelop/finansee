package com.finansee.backend.despesa;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {

  List<Despesa> findByDataBetweenOrderByDataDesc(LocalDate inicio, LocalDate fim);
}