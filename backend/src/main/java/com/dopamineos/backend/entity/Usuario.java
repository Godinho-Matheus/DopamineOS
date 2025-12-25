package com.dopamineos.backend.entity;

import com.dopamineos.backend.entity.enums.ClasseRPG;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    
    @Enumerated(EnumType.STRING)
    private ClasseRPG classe;

    private int nivel;
    private int xpAtual;
    private int xpParaProximoNivel = 100;
    private int moedas;

    // Atributos
    private int forca;
    private int destreza;
    private int intelecto;
    private int carisma;
    private int constituicao;

    // Stats
    private int hpAtual;
    private int maxHp;
    private int mpAtual;
    private int maxMp;
}