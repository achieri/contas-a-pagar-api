import http from 'k6/http';
import { check } from 'k6';

// 50 usuários virtuais tentando alterar a mesma conta simultaneamente
export const options = {
  vus: 50,
  duration: '10s',
};

const BASE_URL = 'http://localhost:4021';

export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ username: 'admin', password: 'admin123' }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  const token = loginRes.json('accessToken');

  const fornecedorRes = http.post(
    `${BASE_URL}/api/fornecedores`,
    JSON.stringify({ nome: 'Fornecedor k6 race condition' }),
    { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` } }
  );
  const fornecedorId = fornecedorRes.json('id');

  const contaRes = http.post(
    `${BASE_URL}/api/contas`,
    JSON.stringify({
      dataVencimento: '2025-12-31',
      valor: 100.00,
      descricao: 'Conta k6 race condition',
      fornecedorId: fornecedorId,
    }),
    { headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` } }
  );

  return { token, contaId: contaRes.json('id') };
}

export default function (data) {
  const res = http.patch(
    `${BASE_URL}/api/contas/${data.contaId}/situacao`,
    JSON.stringify({ situacao: 'PAGO', dataPagamento: '2025-10-01' }),
    {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${data.token}`,
      },
    }
  );

  // Resultado esperado:
  //   ~1 request com 200 (ganhou a race condition — conta transitou para PAGO)
  //  ~49 requests com 422 (perderam — TransicaoSituacaoInvalidaException)
  //   0  requests com 500 (o sistema nunca deve crashar)
  check(res, {
    'status é 200 ou 422': (r) => r.status === 200 || r.status === 422,
    'nunca retorna 500': (r) => r.status !== 500,
  });
}
