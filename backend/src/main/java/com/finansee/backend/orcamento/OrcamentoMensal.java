package com.finansee.backend.orcamento;

import java.math.BigDecimal;

import com.finansee.backend.categoria.Categoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orcamento_mensal", uniqueConstraints = @UniqueConstraint(columnNames = { "ano", "mes", "categoria_id" }))
@Getter
@Setter
@NoArgsConstructor
public class OrcamentoMensal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Short ano;

  @Column(nullable = false)
  private Short mes;

  @Column(name = "valor_limite", nullable = false, precision = 12, scale = 2)
  private BigDecimal valorLimite;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "categoria_id", nullable = false)
  private Categoria categoria;
}