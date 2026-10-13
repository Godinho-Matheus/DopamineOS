package com.lumeos.backend.dto;

import com.lumeos.backend.entity.enums.ClasseRPG;

public record UsuarioDTO(
        Long id,
        String nome,
        ClasseRPG classe,
        int nivel,
        int xpAtual,
        int xpParaProximoNivel,
        int moedas,
        int forca,
        int destreza,
        int intelecto,
        int carisma,
        int constituicao,
        int hpAtual,
        int maxHp,
        int mpAtual,
        int maxMp
) {}
