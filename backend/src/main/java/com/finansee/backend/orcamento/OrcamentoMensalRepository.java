package com.finansee.backend.orcamento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrcamentoMensalRepository extends JpaRepository<OrcamentoMensal, Long> {

  List<OrcamentoMensal> findByAnoAndMes(Short ano, Short mes);
}