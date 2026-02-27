# Testes de Concorrência — Guia Completo

## Por que concorrência importa neste projeto?

Este sistema tem dois pontos naturais de condição de corrida:

1. **RabbitMQ Consumer**: múltiplas mensagens de importação CSV sendo processadas simultaneamente no mesmo banco.
2. **Máquina de estados da Conta**: dois clientes podem tentar alterar a situação da mesma conta ao mesmo tempo (ex: PAGAR e CANCELAR simultaneamente).

---

## Como os scripts de teste funcionam

### Pré-requisitos
```bash
# Instalar jq (parser JSON para bash)
# Linux/Mac:
brew install jq       # macOS
apt install jq        # Ubuntu/Debian

# Windows (Git Bash já tem curl, instale jq manualmente):
# Baixe jq.exe em https://jqlang.github.io/jq/download/
# e coloque no PATH
```

### Como executar
```bash
# Com a API rodando:
docker-compose up -d

# Dar permissão de execução
chmod +x testes/concorrencia/*.sh

# Rodar testes gerais de concorrência
bash testes/concorrencia/teste_concorrencia.sh

# Rodar testes de concorrência no CSV/Consumer
bash testes/concorrencia/teste_concorrencia_csv.sh
```

---

## Os 4 cenários testados

### Cenário 1 — Uploads CSV simultâneos
**O que faz**: dispara 5 uploads do mesmo CSV em paralelo (`&` em bash).
**O que verifica**: cada upload retorna um `protocoloId` único; após processamento, todos chegam ao status `CONCLUIDO`.
**Risco real**: sem isolamento de protocolo, dois consumers poderiam gravar no mesmo `ImportacaoLog`.

### Cenário 2 — Race condition na máquina de estados
**O que faz**: dois workers tentam simultaneamente PAGAR e CANCELAR a mesma conta.
**O que verifica**: apenas uma transição é aceita (HTTP 200); a outra retorna HTTP 422 com `TransicaoSituacaoInvalidaException`.
**Por que isso acontece**: o JPA usa `@Transactional` com isolamento `READ_COMMITTED` por padrão no PostgreSQL. O segundo a commitar vê o estado já alterado pelo primeiro e lança a exceção de domínio.

### Cenário 3 — Criação massiva simultânea
**O que faz**: 10 workers criam contas ao mesmo tempo.
**O que verifica**: todas as 10 contas são criadas sem duplicação ou perda. O UUID gerado pelo banco (`gen_random_uuid()`) garante unicidade mesmo em paralelo.

### Cenário 4 — Leitura durante escrita (dirty read check)
**O que faz**: 5 leitores leem a mesma conta enquanto um escritor a atualiza.
**O que verifica**: nenhuma leitura retorna `situacao: null` ou estado corrompido. O PostgreSQL garante isso com MVCC (Multi-Version Concurrency Control).

---

## Proteções do sistema contra condições de corrida

| Proteção | Onde está | O que previne |
|---|---|---|
| `@Transactional` nos use cases | Application layer | Operações incompletas visíveis para outros |
| Máquina de estados na entidade | Domain layer | Estado inválido após race condition |
| `gen_random_uuid()` no PostgreSQL | Banco de dados | Colisão de IDs em inserts simultâneos |
| Protocolo UUID único por upload | ImportacaoController | Dois uploads sobrescrevendo o mesmo log |
| `@RabbitListener` com ACK manual | Consumer | Mensagem processada duas vezes após falha |

---

## Limitações desta abordagem e como ir além

### O que os scripts bash NÃO testam bem
- Microsegundos de diferença entre requests (o bash tem latência de processo)
- Carga real com centenas de usuários simultâneos
- Comportamento sob pressão de memória/CPU

### Ferramentas para testes de carga reais

#### k6 (recomendado — moderno e simples)
```bash
# Instalar: https://k6.io/docs/getting-started/installation/
k6 run testes/concorrencia/k6_script.js
```

#### Apache JMeter (GUI, mais visual)
```bash
jmeter -n -t testes/concorrencia/jmeter_plan.jmx -l results.jtl
```

#### wrk (benchmark de HTTP puro)
```bash
# 12 threads, 400 conexões, 30 segundos no endpoint de listagem
wrk -t12 -c400 -d30s -H "Authorization: Bearer $TOKEN" \
    http://localhost:4021/api/contas
```

---

## Exemplo com k6 — race condition na alteração de situação

Crie o arquivo `testes/concorrencia/k6_race_condition.js`:

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

// 50 usuários virtuais, cada um tentando alterar a mesma conta
export const options = {
  vus: 50,
  duration: '10s',
};

const BASE_URL = 'http://localhost:4021';

export function setup() {
  // Login e criação da conta compartilhada
  const loginRes = http.post(`${BASE_URL}/api/auth/login`,
    JSON.stringify({ username: 'admin', password: 'admin123' }),
    { headers: { 'Content-Type': 'application/json' } });
  const token = loginRes.json('accessToken');

  const contaRes = http.post(`${BASE_URL}/api/contas`,
    JSON.stringify({
      dataVencimento: '2025-12-31',
      valor: 100.00,
      descricao: 'Conta k6 race condition',
      fornecedorId: '__FORNECEDOR_ID__'
    }),
    { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` } });

  return { token, contaId: contaRes.json('id') };
}

export default function (data) {
  const payload = JSON.stringify({ situacao: 'PAGO', dataPagamento: '2025-10-01' });
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${data.token}`
    }
  };

  const res = http.patch(`${BASE_URL}/api/contas/${data.contaId}/situacao`, payload, params);

  // Aceita tanto 200 (primeira a ganhar a corrida) quanto 422 (perdeu a corrida)
  check(res, {
    'status é 200 ou 422': (r) => r.status === 200 || r.status === 422,
    'nunca retorna 500': (r) => r.status !== 500,
  });
}
```

**Resultado esperado com k6**:
- ~1 requisição com status 200 (a que ganhou a race condition)
- ~49 requisições com status 422 (perderam a race condition)
- 0 requisições com status 500 (o sistema nunca deve crashar)
