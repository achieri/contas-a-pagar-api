-- Usuário admin padrão para facilitar testes iniciais
-- Senha: admin123 (bcrypt)
INSERT INTO usuario (id, username, password, role, ativo, criado_em)
VALUES (
    gen_random_uuid(),
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyNv68p2i',
    'ROLE_ADMIN',
    TRUE,
    NOW()
) ON CONFLICT (username) DO NOTHING;
