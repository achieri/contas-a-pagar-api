ALTER TABLE usuario
ADD COLUMN atualizado_em TIMESTAMP NOT NULL DEFAULT NOW();

UPDATE usuario SET atualizado_em = criado_em;
