package com.totvs.contaspagar.infrastructure.persistence.jpa;

import com.totvs.contaspagar.infrastructure.persistence.entity.ImportacaoLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImportacaoLogJpaRepository extends JpaRepository<ImportacaoLog, UUID> {
    Optional<ImportacaoLog> findByProtocoloId(UUID protocoloId);

    @Query("SELECT l FROM ImportacaoLog l WHERE l.status = :status AND l.solicitadoEm < :timeout")
    List<ImportacaoLog> findProcessandoComTimeout(
            @Param("status") ImportacaoLog.Status status,
            @Param("timeout") LocalDateTime timeout
    );
}
