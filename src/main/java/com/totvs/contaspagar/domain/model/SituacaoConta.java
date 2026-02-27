package com.totvs.contaspagar.domain.model;

/**
 * Enum que representa os estados possíveis de uma Conta a Pagar.
 * A máquina de estados é validada pelo próprio domínio na entidade Conta.
 */
public enum SituacaoConta {
    PENDENTE,
    PAGO,
    CANCELADO
}
