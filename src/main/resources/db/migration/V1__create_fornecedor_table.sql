CREATE TABLE fornecedor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fornecedor_nome ON fornecedor (nome);
