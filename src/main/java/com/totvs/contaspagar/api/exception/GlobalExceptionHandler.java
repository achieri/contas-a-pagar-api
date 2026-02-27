package com.totvs.contaspagar.api.exception;

import com.totvs.contaspagar.domain.exception.ConflictException;
import com.totvs.contaspagar.domain.exception.ContaNaoEncontradaException;
import com.totvs.contaspagar.domain.exception.DomainException;
import com.totvs.contaspagar.domain.exception.FornecedorNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centraliza o tratamento de exceções e os mapeia para RFC 7807 (Problem Details).
 * Garante respostas padronizadas e sem vazamento de stack traces.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** JSON malformado ou tipo incompatível (ex: número com zero à esquerda, string onde se espera número) */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Requisição inválida");
        pd.setDetail("O corpo da requisição contém JSON inválido ou um tipo de dado incompatível.");
        pd.setType(URI.create("urn:contas-pagar:malformed-request"));
        return pd;
    }

    /** Bean Validation — campos inválidos no request body */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erros = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "inválido",
                        (a, b) -> a
                ));
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Dados inválidos");
        pd.setType(URI.create("urn:contas-pagar:validation-error"));
        pd.setProperty("erros", erros);
        return pd;
    }

    /** Recurso não encontrado */
    @ExceptionHandler({ContaNaoEncontradaException.class, FornecedorNaoEncontradoException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Recurso não encontrado");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("urn:contas-pagar:not-found"));
        return pd;
    }

    /** Invariante de domínio violada */
    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle("Regra de negócio violada");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("urn:contas-pagar:domain-rule"));
        return pd;
    }

    /** Propriedade de ordenação inválida em queries de método (ex: sort=string do Swagger UI) */
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handlePropertyReference(PropertyReferenceException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Parâmetro de ordenação inválido");
        pd.setDetail("Propriedade '" + ex.getPropertyName() + "' não existe. Utilize campos válidos para ordenação.");
        pd.setType(URI.create("urn:contas-pagar:invalid-sort"));
        return pd;
    }

    /** Propriedade de ordenação inválida em queries JPQL customizadas */
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ProblemDetail handleInvalidDataAccess(InvalidDataAccessApiUsageException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Parâmetro de consulta inválido");
        pd.setDetail("Parâmetro de ordenação ou filtro inválido. Verifique os campos informados.");
        pd.setType(URI.create("urn:contas-pagar:invalid-sort"));
        return pd;
    }

    /** Conflito de unicidade detectado na camada de aplicação (ex: nome duplicado) */
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Conflito de dados");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("urn:contas-pagar:conflict"));
        return pd;
    }

    /** Violação de constraint única no banco (fallback de race condition) */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Conflito de dados");
        pd.setDetail("Já existe um registro com os dados informados.");
        pd.setType(URI.create("urn:contas-pagar:conflict"));
        return pd;
    }

    /** Credenciais inválidas */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Credenciais inválidas");
        pd.setDetail("Usuário ou senha incorretos.");
        return pd;
    }

    /** Erro interno não tratado */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("Erro interno não tratado", ex);
        var pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Erro interno");
        pd.setDetail("Um erro inesperado ocorreu. Por favor, tente novamente.");
        return pd;
    }
}
