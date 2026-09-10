package com.dopamineos.backend.dto;

import java.time.LocalDateTime;

public record LogAtividadeDTO(
        Long id,
        String nomeMissao,
        String icone,
        int xpGanho,
        int duracaoMinutos,
        LocalDateTime dataHora
) {}
