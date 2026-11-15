package com.finansee.backend.despesa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "despesa")
@Getter
@Setter
@NoArgsConstructor
public class Despesa {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String descricao;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal valor;

  @Column(nullable = false)
  private LocalDate data;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "categoria_id", nullable = false)
  private Categoria categoria;

  @CreationTimestamp
  @Column(name = "criada_em", nullable = false, updatable = false)
  private LocalDateTime criadaEm;
}