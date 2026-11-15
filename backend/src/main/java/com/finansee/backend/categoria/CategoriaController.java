package com.finansee.backend.categoria;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

  private final CategoriaService service;

  @GetMapping
  public List<CategoriaResponse> listar() {
    return service.listar();
  }

  @GetMapping("/{id}")
  public CategoriaResponse buscar(@PathVariable Long id) {
    return service.buscar(id);
  }

  @PostMapping
  public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
    CategoriaResponse criada = service.criar(request);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(criada.id())
        .toUri();
    return ResponseEntity.created(location).body(criada);
  }

  @PutMapping("/{id}")
  public CategoriaResponse atualizar(@PathVariable Long id,
      @Valid @RequestBody CategoriaRequest request) {
    return service.atualizar(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void excluir(@PathVariable Long id) {
    service.excluir(id);
  }
}