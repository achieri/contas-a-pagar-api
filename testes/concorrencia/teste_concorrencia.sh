#!/usr/bin/env bash
# ============================================================
# Script de Testes de Concorrência — API Contas a Pagar
# ============================================================
# Pré-requisitos: curl, jq
# Uso: bash teste_concorrencia.sh
#
# O script executa 4 cenários de concorrência:
#   1. Múltiplos uploads CSV simultâneos
#   2. Race condition na alteração de situação (PAGO x CANCELADO)
#   3. Criação massiva e simultânea de contas
#   4. Leitura simultânea com escrita (dirty read check)
# ============================================================

BASE_URL="http://localhost:4021"
VERDE='\033[0;32m'
VERMELHO='\033[0;31m'
AMARELO='\033[1;33m'
NC='\033[0m'

# ---------------------------------------------------------------
# SETUP — obter token e criar fornecedor
# ---------------------------------------------------------------
echo -e "${AMARELO}[SETUP] Autenticando...${NC}"
TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.accessToken')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
  echo -e "${VERMELHO}[ERRO] Falha na autenticação. Verifique se a API está rodando.${NC}"
  exit 1
fi
echo -e "${VERDE}[OK] Token obtido.${NC}"

echo -e "${AMARELO}[SETUP] Criando fornecedor base...${NC}"
FORNECEDOR_ID=$(curl -s -X POST "$BASE_URL/api/fornecedores" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nome":"Fornecedor Concorrencia Teste"}' \
  | jq -r '.id')

if [ -z "$FORNECEDOR_ID" ] || [ "$FORNECEDOR_ID" == "null" ]; then
  echo -e "${VERMELHO}[ERRO] Não foi possível criar o fornecedor.${NC}"
  exit 1
fi
echo -e "${VERDE}[OK] Fornecedor criado: $FORNECEDOR_ID${NC}"

# substitui o placeholder nos CSVs
CSV_DIR="$(dirname "$0")/../csv"
for f in "$CSV_DIR"/*.csv; do
  sed -i "s/SUBSTITUA_PELO_ID_DO_FORNECEDOR/$FORNECEDOR_ID/g" "$f" 2>/dev/null
done
echo -e "${VERDE}[OK] CSVs atualizados com fornecedor real.${NC}"

# ---------------------------------------------------------------
# CENÁRIO 1: Múltiplos uploads CSV simultâneos
# Objetivo: garantir que cada upload recebe um protocoloId único
# e que o Consumer processa todos sem conflito de dados.
# ---------------------------------------------------------------
echo ""
echo -e "${AMARELO}======================================================${NC}"
echo -e "${AMARELO}CENÁRIO 1 — Uploads CSV simultâneos (5 paralelos)${NC}"
echo -e "${AMARELO}======================================================${NC}"

PROTOCOLOS=()
for i in {1..5}; do
  (
    RESP=$(curl -s -o /tmp/resp_csv_$i.json -w "%{http_code}" \
      -X POST "$BASE_URL/api/importacao/csv" \
      -H "Authorization: Bearer $TOKEN" \
      -F "arquivo=@$CSV_DIR/csv_valido.csv")
    PROTOCOLO=$(jq -r '.protocoloId' /tmp/resp_csv_$i.json)
    echo -e "  Worker $i → HTTP $RESP | protocolo: $PROTOCOLO"
  ) &
done
wait
echo -e "${VERDE}[OK] Todos os workers concluídos. Aguardando processamento...${NC}"
sleep 5

echo -e "${AMARELO}Verificando status dos protocolos:${NC}"
for i in {1..5}; do
  PROTOCOLO=$(jq -r '.protocoloId' /tmp/resp_csv_$i.json 2>/dev/null)
  if [ -n "$PROTOCOLO" ] && [ "$PROTOCOLO" != "null" ]; then
    STATUS=$(curl -s "$BASE_URL/api/importacao/csv/$PROTOCOLO" \
      -H "Authorization: Bearer $TOKEN" | jq -r '.status')
    echo -e "  Protocolo $i ($PROTOCOLO): ${VERDE}$STATUS${NC}"
  fi
done

# ---------------------------------------------------------------
# CENÁRIO 2: Race condition — alterar situação simultaneamente
# Objetivo: dois workers tentam simultaneamente PAGAR e CANCELAR
# a mesma conta. Apenas uma transição é válida; a outra deve
# falhar com 422 (conta já não está PENDENTE).
# ---------------------------------------------------------------
echo ""
echo -e "${AMARELO}======================================================${NC}"
echo -e "${AMARELO}CENÁRIO 2 — Race condition: PAGAR vs CANCELAR (mesma conta)${NC}"
echo -e "${AMARELO}======================================================${NC}"

CONTA_ID=$(curl -s -X POST "$BASE_URL/api/contas" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"dataVencimento\":\"2025-12-31\",\"valor\":999.00,\"descricao\":\"Conta race condition\",\"fornecedorId\":\"$FORNECEDOR_ID\"}" \
  | jq -r '.id')

echo -e "  Conta criada: $CONTA_ID"
echo -e "  Disparando PAGAR e CANCELAR simultaneamente..."

(
  HTTP=$(curl -s -o /tmp/race_pagar.json -w "%{http_code}" \
    -X PATCH "$BASE_URL/api/contas/$CONTA_ID/situacao" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"situacao":"PAGO","dataPagamento":"2025-10-01"}')
  SITUACAO=$(jq -r '.situacao // .title' /tmp/race_pagar.json)
  echo -e "  Worker PAGAR  → HTTP $HTTP | resultado: $SITUACAO"
) &

(
  HTTP=$(curl -s -o /tmp/race_cancelar.json -w "%{http_code}" \
    -X PATCH "$BASE_URL/api/contas/$CONTA_ID/situacao" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"situacao":"CANCELADO"}')
  SITUACAO=$(jq -r '.situacao // .title' /tmp/race_cancelar.json)
  echo -e "  Worker CANCELAR → HTTP $HTTP | resultado: $SITUACAO"
) &

wait
SITUACAO_FINAL=$(curl -s "$BASE_URL/api/contas/$CONTA_ID" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.situacao')
echo -e "  ${VERDE}Situação final da conta: $SITUACAO_FINAL${NC}"
echo -e "  ${AMARELO}(Uma requisição deve ter sido 200, a outra 422)${NC}"

# ---------------------------------------------------------------
# CENÁRIO 3: Criação massiva simultânea (10 contas em paralelo)
# Objetivo: garantir que todas as contas são criadas corretamente
# sem perda de dados ou duplicação devido a condições de corrida.
# ---------------------------------------------------------------
echo ""
echo -e "${AMARELO}======================================================${NC}"
echo -e "${AMARELO}CENÁRIO 3 — Criação simultânea de 10 contas${NC}"
echo -e "${AMARELO}======================================================${NC}"

CRIADAS=0
for i in {1..10}; do
  (
    HTTP=$(curl -s -o /tmp/conta_$i.json -w "%{http_code}" \
      -X POST "$BASE_URL/api/contas" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "{\"dataVencimento\":\"2025-0${((i%9+1))}-15\",\"valor\":$((i*100)).00,\"descricao\":\"Conta paralela $i\",\"fornecedorId\":\"$FORNECEDOR_ID\"}")
    ID=$(jq -r '.id' /tmp/conta_$i.json)
    echo -e "  Worker $i → HTTP $HTTP | id: $ID"
  ) &
done
wait

echo -e "${AMARELO}Verificando contas criadas:${NC}"
TOTAL=$(curl -s "$BASE_URL/api/contas?descricao=Conta+paralela&size=20" \
  -H "Authorization: Bearer $TOKEN" | jq '.totalElements')
echo -e "  ${VERDE}Total de contas 'Conta paralela' encontradas: $TOTAL (esperado: 10)${NC}"

# ---------------------------------------------------------------
# CENÁRIO 4: Leitura simultânea durante escrita
# Objetivo: verificar consistência de leituras enquanto updates ocorrem.
# Nenhuma leitura deve retornar estado inconsistente (ex: situacao null).
# ---------------------------------------------------------------
echo ""
echo -e "${AMARELO}======================================================${NC}"
echo -e "${AMARELO}CENÁRIO 4 — Leitura simultânea durante atualização${NC}"
echo -e "${AMARELO}======================================================${NC}"

CONTA_LEITURA=$(curl -s -X POST "$BASE_URL/api/contas" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"dataVencimento\":\"2025-12-01\",\"valor\":700.00,\"descricao\":\"Conta leitura concorrente\",\"fornecedorId\":\"$FORNECEDOR_ID\"}" \
  | jq -r '.id')

echo -e "  Conta criada: $CONTA_LEITURA"
echo -e "  Disparando 5 leitores e 1 escritor simultaneamente..."

# Escritor: atualiza descrição
(
  sleep 0.1
  curl -s -X PUT "$BASE_URL/api/contas/$CONTA_LEITURA" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"dataVencimento\":\"2025-12-01\",\"valor\":700.00,\"descricao\":\"Descricao atualizada\",\"fornecedorId\":\"$FORNECEDOR_ID\"}" \
    > /dev/null
  echo -e "  Escritor → atualização concluída"
) &

# Leitores: leem enquanto o escritor age
LEITURAS_OK=0
for i in {1..5}; do
  (
    SITUACAO=$(curl -s "$BASE_URL/api/contas/$CONTA_LEITURA" \
      -H "Authorization: Bearer $TOKEN" | jq -r '.situacao')
    if [ "$SITUACAO" != "null" ] && [ -n "$SITUACAO" ]; then
      echo -e "  Leitor $i → situacao: ${VERDE}$SITUACAO${NC} (consistente)"
    else
      echo -e "  Leitor $i → ${VERMELHO}situacao inconsistente: '$SITUACAO'${NC}"
    fi
  ) &
done
wait

echo ""
echo -e "${VERDE}============================================${NC}"
echo -e "${VERDE}  Testes de concorrência concluídos!${NC}"
echo -e "${VERDE}============================================${NC}"
echo ""
echo -e "Arquivos de resultado em /tmp/race_*.json e /tmp/conta_*.json"
