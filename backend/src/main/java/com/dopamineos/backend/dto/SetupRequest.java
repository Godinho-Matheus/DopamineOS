package com.dopamineos.backend.dto;

import com.dopamineos.backend.entity.enums.ClasseRPG;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SetupRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotNull(message = "A classe é obrigatória")
        ClasseRPG classe,

        /** Missões escolhidas pelo usuário no setup. Opcional — se vazio, cria deck padrão. */
        List<ProtocoloDTO> missoes
) {}
