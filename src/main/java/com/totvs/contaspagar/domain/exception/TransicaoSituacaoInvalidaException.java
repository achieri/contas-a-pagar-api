package com.totvs.contaspagar.domain.exception;

import com.totvs.contaspagar.domain.model.SituacaoConta;

public class TransicaoSituacaoInvalidaException extends DomainException {

    public TransicaoSituacaoInvalidaException(SituacaoConta de, SituacaoConta para) {
        super(String.format("Transição de situação inválida: %s → %s", de, para));
    }
}
