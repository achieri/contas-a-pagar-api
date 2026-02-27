-- Garante unicidade case-insensitive no nome do fornecedor
CREATE UNIQUE INDEX idx_fornecedor_nome_unique ON fornecedor (LOWER(nome));
