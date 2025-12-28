package com.dopamineos.backend.repository;

import com.dopamineos.backend.entity.LogAtividade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogAtividadeRepository extends JpaRepository<LogAtividade, Long> {
    List<LogAtividade> findByOrderByDataHoraDesc();
}