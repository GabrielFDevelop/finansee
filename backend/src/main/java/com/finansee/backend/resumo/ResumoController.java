package com.finansee.backend.resumo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resumo")
@RequiredArgsConstructor
public class ResumoController {

  private final ResumoService service;

  @GetMapping
  public ResumoMensalResponse resumoDoMes(@RequestParam(required = false) Integer ano,
      @RequestParam(required = false) Integer mes) {
    return service.resumoDoMes(ano, mes);
  }
}