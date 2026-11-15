package com.finansee.backend.despesa;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/despesas")
@RequiredArgsConstructor
public class DespesaController {

  private final DespesaService service;

  @GetMapping
  public List<DespesaResponse> listar(@RequestParam(required = false) Integer ano,
      @RequestParam(required = false) Integer mes) {
    return service.listarPorMes(ano, mes);
  }

  @GetMapping("/{id}")
  public DespesaResponse buscar(@PathVariable Long id) {
    return service.buscar(id);
  }

  @PostMapping
  public ResponseEntity<DespesaResponse> criar(@Valid @RequestBody DespesaRequest request) {
    DespesaResponse criada = service.criar(request);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(criada.id())
        .toUri();
    return ResponseEntity.created(location).body(criada);
  }

  @PutMapping("/{id}")
  public DespesaResponse atualizar(@PathVariable Long id,
      @Valid @RequestBody DespesaRequest request) {
    return service.atualizar(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void excluir(@PathVariable Long id) {
    service.excluir(id);
  }
}