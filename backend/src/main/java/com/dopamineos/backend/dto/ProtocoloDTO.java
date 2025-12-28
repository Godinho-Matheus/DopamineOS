package com.dopamineos.backend.dto;

import com.dopamineos.backend.entity.enums.Atributo;
import com.dopamineos.backend.entity.enums.Dificuldade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProtocoloDTO(
    Long id,
    
    @NotBlank(message = "O nome é obrigatório")
    String nome,
    
    String icone,
    String cor,
    String descricao,
    
    @NotNull(message = "A duração é obrigatória")
    Integer duracaoMinutos,
    
    @NotNull(message = "O atributo é obrigatório")
    Atributo atributo,
    
    @NotNull(message = "A dificuldade é obrigatória")
    Dificuldade dificuldade
) {}