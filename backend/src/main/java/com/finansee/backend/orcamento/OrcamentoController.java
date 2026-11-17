package com.finansee.backend.orcamento;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orcamentos")
@RequiredArgsConstructor
public class OrcamentoController {

  private final OrcamentoService service;

  @GetMapping
  public List<OrcamentoResponse> listar(@RequestParam(required = false) Integer ano,
      @RequestParam(required = false) Integer mes) {
    return service.listar(ano, mes);
  }

  @PutMapping("/{ano}/{mes}/{categoriaId}")
  public OrcamentoResponse definir(@PathVariable int ano,
      @PathVariable int mes,
      @PathVariable Long categoriaId,
      @Valid @RequestBody OrcamentoRequest request) {
    return service.definir(ano, mes, categoriaId, request);
  }

  @DeleteMapping("/{ano}/{mes}/{categoriaId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remover(@PathVariable int ano,
      @PathVariable int mes,
      @PathVariable Long categoriaId) {
    service.remover(ano, mes, categoriaId);
  }
}