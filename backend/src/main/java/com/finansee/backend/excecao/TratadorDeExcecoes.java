package com.finansee.backend.excecao;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeExcecoes {

  @ExceptionHandler(RecursoNaoEncontradoException.class)
  public ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(ConflitoException.class)
  public ProblemDetail conflito(ConflitoException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail integridade(DataIntegrityViolationException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
        "Operação não permitida: o registro está em uso ou viola uma regra de integridade.");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail validacao(MethodArgumentNotValidException ex) {
    Map<String, String> erros = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(erro -> erros.put(erro.getField(), erro.getDefaultMessage()));

    ProblemDetail problema = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Dados inválidos");
    problema.setProperty("erros", erros);
    return problema;
  }
}