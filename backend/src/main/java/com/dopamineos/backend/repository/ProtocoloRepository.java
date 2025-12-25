package com.dopamineos.backend.repository;

import com.dopamineos.backend.entity.Protocolo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProtocoloRepository extends JpaRepository<Protocolo, Long> {
}