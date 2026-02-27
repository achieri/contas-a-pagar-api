CREATE TYPE situacao_conta AS ENUM ('PENDENTE', 'PAGO', 'CANCELADO');

CREATE TABLE conta (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    data_vencimento DATE NOT NULL,
    data_pagamento DATE,
    valor NUMERIC(19, 2) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    situacao situacao_conta NOT NULL DEFAULT 'PENDENTE',
    fornecedor_id UUID NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_conta_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedor(id),
    CONSTRAINT chk_valor_positivo CHECK (valor > 0),
    CONSTRAINT chk_data_pagamento CHECK (
        situacao != 'PAGO' OR data_pagamento IS NOT NULL
    )
);

CREATE INDEX idx_conta_data_vencimento ON conta (data_vencimento);
CREATE INDEX idx_conta_descricao ON conta USING gin (to_tsvector('portuguese', descricao));
CREATE INDEX idx_conta_situacao ON conta (situacao);
CREATE INDEX idx_conta_fornecedor ON conta (fornecedor_id);
