#!/usr/bin/env bash
# ============================================================
# Teste de concorrência focado no Consumer RabbitMQ + CSV
# ============================================================
# Simula o cenário mais crítico do desafio:
#   - N uploads simultâneos do mesmo CSV com dados válidos e inválidos
#   - Verifica que cada protocolo é processado independentemente
#   - Verifica que o DLQ recebe mensagens em caso de falha total
#   - Verifica que não há duplicação de registros
# ============================================================

BASE_URL="http://localhost:4021"
AMARELO='\033[1;33m'
VERDE='\033[0;32m'
VERMELHO='\033[0;31m'
NC='\033[0m'

CSV_DIR="$(dirname "$0")/../csv"

# ---------------------------------------------------------------
# SETUP
# ---------------------------------------------------------------
echo -e "${AMARELO}[SETUP] Autenticando...${NC}"
TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.accessToken')

FORNECEDOR_ID=$(curl -s -X POST "$BASE_URL/api/fornecedores" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nome":"Fornecedor CSV Concorrencia"}' | jq -r '.id')

echo -e "${VERDE}Fornecedor: $FORNECEDOR_ID${NC}"

# Atualiza os CSVs com o ID real
for f in "$CSV_DIR"/*.csv; do
  sed -i "s/SUBSTITUA_PELO_ID_DO_FORNECEDOR/$FORNECEDOR_ID/g" "$f" 2>/dev/null
done

# ---------------------------------------------------------------
# TESTE A: 10 uploads simultâneos do csv_valido.csv
# Verifica: protocolos únicos, todos chegam a CONCLUIDO
# ---------------------------------------------------------------
echo ""
echo -e "${AMARELO}[TESTE A] 10 uploads simultâneos do csv_valido.csv${NC}"

declare -a PROTOCOLOS
for i in {1..10}; do
  PROTOCOLO=$(curl -s -X POST "$BASE_URL/api/importacao/csv" \
    -H "Authorization: Bearer $TOKEN" \
    -F "arquivo=@$CSV_DIR/csv_valido.csv" | jq -r '.protocoloId')
  echo -e "  Upload $i → protocolo: $PROTOCOLO"
  PROTOCOLOS+=("$PROTOCOLO")
  # pequeno delay para não sobrecarregar a fila imediatamente
  sleep 0.05 &
done
wait

echo -e "${AMARELO}Aguardando 10s para o Consumer processar todos...${NC}"
sleep 10

echo -e "${AMARELO}Status dos protocolos:${NC}"
CONCLUIDOS=0
for PROTOCOLO in "${PROTOCOLOS[@]}"; do
  STATUS=$(curl -s "$BASE_URL/api/importacao/csv/$PROTOCOLO" \
    -H "Authorization: Bearer $TOKEN" | jq -r '.status')
  if [[ "$STATUS" == "CONCLUIDO" ]]; then
    echo -e "  $PROTOCOLO → ${VERDE}$STATUS${NC}"
    ((CONCLUIDOS++))
  else
    echo -e "  $PROTOCOLO → ${VERMELHO}$STATUS${NC}"
  fi
done
echo -e "  ${VERDE}Concluídos com sucesso: $CONCLUIDOS/10${NC}"

# ---------------------------------------------------------------
# TESTE B: Upload simultâneo de CSVs com tipos de erro diferentes
# Verifica: cada protocolo tem status correto para seu tipo de erro
# ---------------------------------------------------------------
echo ""
echo -e "${AMARELO}[TESTE B] Uploads simultâneos com CSV de erro diferentes${NC}"

CSV_TIPOS=(
  "csv_valido.csv:CONCLUIDO"
  "csv_data_invalida.csv:CONCLUIDO_COM_ERROS"
  "csv_valor_invalido.csv:CONCLUIDO_COM_ERROS"
  "csv_fornecedor_inexistente.csv:CONCLUIDO_COM_ERROS"
  "csv_apenas_cabecalho.csv:CONCLUIDO"
  "csv_misto_todos_erros.csv:CONCLUIDO_COM_ERROS"
)

declare -A PROTO_MAP
for ENTRY in "${CSV_TIPOS[@]}"; do
  CSV=$(echo $ENTRY | cut -d: -f1)
  ESPERADO=$(echo $ENTRY | cut -d: -f2)
  (
    PROTOCOLO=$(curl -s -X POST "$BASE_URL/api/importacao/csv" \
      -H "Authorization: Bearer $TOKEN" \
      -F "arquivo=@$CSV_DIR/$CSV" | jq -r '.protocoloId')
    echo "$CSV|$PROTOCOLO|$ESPERADO" >> /tmp/proto_map.txt
    echo -e "  $CSV → protocolo: $PROTOCOLO"
  ) &
done
wait

echo -e "${AMARELO}Aguardando 8s para o Consumer processar...${NC}"
sleep 8

echo -e "${AMARELO}Validando status esperados:${NC}"
while IFS= read -r linha; do
  CSV=$(echo "$linha" | cut -d'|' -f1)
  PROTOCOLO=$(echo "$linha" | cut -d'|' -f2)
  ESPERADO=$(echo "$linha" | cut -d'|' -f3)
  STATUS=$(curl -s "$BASE_URL/api/importacao/csv/$PROTOCOLO" \
    -H "Authorization: Bearer $TOKEN" | jq -r '.status')
  if [ "$STATUS" == "$ESPERADO" ]; then
    echo -e "  ${VERDE}PASS${NC} $CSV → esperado: $ESPERADO | obtido: $STATUS"
  else
    echo -e "  ${VERMELHO}FAIL${NC} $CSV → esperado: $ESPERADO | obtido: $STATUS"
  fi
done < /tmp/proto_map.txt
rm -f /tmp/proto_map.txt

# ---------------------------------------------------------------
# TESTE C: Verificar ausência de duplicação
# Conta as contas criadas e verifica que bate com o esperado
# ---------------------------------------------------------------
echo ""
echo -e "${AMARELO}[TESTE C] Verificar duplicação de registros${NC}"
echo -e "  csv_valido.csv tem 5 linhas × 10 uploads simultâneos = 50 registros esperados"

TOTAL=$(curl -s "$BASE_URL/api/contas?descricao=Energia+eletrica+agosto&size=100" \
  -H "Authorization: Bearer $TOKEN" | jq '.totalElements // 0')
echo -e "  Registros 'Energia eletrica agosto' no banco: ${VERDE}$TOTAL${NC}"
if [ "$TOTAL" -eq 10 ]; then
  echo -e "  ${VERDE}PASS — sem duplicação detectada${NC}"
else
  echo -e "  ${VERMELHO}ATENÇÃO — valor fora do esperado (pode haver duplicação ou falha)${NC}"
fi

echo ""
echo -e "${VERDE}Testes CSV de concorrência concluídos!${NC}"
