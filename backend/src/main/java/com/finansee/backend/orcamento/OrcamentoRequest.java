package com.finansee.backend.orcamento;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrcamentoRequest(
    @NotNull(message = "O limite é obrigatório") @Positive(message = "O limite deve ser maior que zero") @Digits(integer = 10, fraction = 2, message = "O limite deve ter no máximo 10 dígitos inteiros e 2 casas decimais") BigDecimal valorLimite) {
}