package com.totvs.contaspagar.infrastructure.persistence.jpa;

import com.totvs.contaspagar.infrastructure.persistence.entity.ImportacaoLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ImportacaoLogJpaRepository extends JpaRepository<ImportacaoLog, UUID> {
    Optional<ImportacaoLog> findByProtocoloId(UUID protocoloId);
}
