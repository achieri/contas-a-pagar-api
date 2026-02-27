-- Fornecedor padrão para testes de importação CSV
-- UUID fixo para que os arquivos em testes/csv/ funcionem sem substituição manual
INSERT INTO fornecedor (id, nome, criado_em, atualizado_em)
VALUES (
    '586443e5-446b-4a0e-88a3-0e6393a550be',
    'Fornecedor Padrão Testes',
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;
