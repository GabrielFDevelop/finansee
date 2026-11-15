package com.finansee.backend.excecao;

public class RequisicaoInvalidaException extends RuntimeException {

  public RequisicaoInvalidaException(String mensagem) {
    super(mensagem);
  }
}