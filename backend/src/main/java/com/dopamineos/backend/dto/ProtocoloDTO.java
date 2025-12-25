package com.dopamineos.backend.dto;

import com.dopamineos.backend.entity.enums.Atributo;
import com.dopamineos.backend.entity.enums.Dificuldade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProtocoloDTO {
    private Long id;
    private String nome;
    private String icone;
    private String cor;
    private int duracaoMinutos = 30;
    private Atributo atributo;
    private Dificuldade dificuldade;
}
