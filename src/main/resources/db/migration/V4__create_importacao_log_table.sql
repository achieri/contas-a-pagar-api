CREATE TYPE status_importacao AS ENUM ('AGUARDANDO', 'PROCESSANDO', 'CONCLUIDO', 'CONCLUIDO_COM_ERROS', 'FALHA');

CREATE TABLE importacao_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    protocolo_id UUID NOT NULL UNIQUE,
    status status_importacao NOT NULL DEFAULT 'AGUARDANDO',
    total_linhas INTEGER,
    linhas_processadas INTEGER DEFAULT 0,
    linhas_com_erro INTEGER DEFAULT 0,
    mensagem_erro TEXT,
    solicitado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    processado_em TIMESTAMP
);

CREATE INDEX idx_importacao_log_protocolo ON importacao_log (protocolo_id);
