package com.dopamineos.backend.dto;

import com.dopamineos.backend.entity.enums.ClasseRPG;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String nome;
    private ClasseRPG classe;
    private int nivel;
    private int xpAtual;
    private int xpParaProximoNivel;
    private int moedas;
    private int forca;
    private int destreza;
    private int intelecto;
    private int carisma;
    private int constituicao;
    private int hpAtual;
    private int maxHp;
    private int mpAtual;
    private int maxMp;
}
