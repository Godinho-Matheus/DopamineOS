package com.dopamineos.backend.entity;

import com.dopamineos.backend.entity.enums.ClasseRPG;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tb_usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Enumerated(EnumType.STRING)
    private ClasseRPG classe;

    private int nivel = 1;
    private int xpAtual = 0;
    private int xpParaProximoNivel = 100;
    private int moedas = 0;

    // Atributos
    private int forca;
    private int destreza;
    private int intelecto;
    private int carisma;
    private int constituicao;

    // Status Dinâmicos
    private int hpAtual;
    private int maxHp;
    private int mpAtual;
    private int maxMp;

    // --- MÉTODOS DE LÓGICA DE NEGÓCIO

    public void ganharXp(int xp) {
        this.xpAtual += xp;
        if (this.xpAtual >= this.xpParaProximoNivel) {
            subirNivel();
        }
    }

    private void subirNivel() {
        this.nivel++;
        this.xpAtual -= this.xpParaProximoNivel;
        this.xpParaProximoNivel = (int) (this.xpParaProximoNivel * 1.5);
        
        // Cura ao upar
        this.hpAtual = this.maxHp;
        this.mpAtual = this.maxMp;
    }

    public void resetarPersonagem(String nome, ClasseRPG classe) {
        this.nome = nome;
        this.classe = classe;
        this.nivel = 1;
        this.xpAtual = 0;
        this.xpParaProximoNivel = 100;
        this.moedas = 0;

        // Define atributos baseados na classe
        switch (classe) {
            case GUERREIRO -> {
                this.forca = 10; this.destreza = 5; this.intelecto = 3; this.carisma = 5; this.constituicao = 10;
            }
            case MAGO -> {
                this.forca = 3; this.destreza = 5; this.intelecto = 10; this.carisma = 6; this.constituicao = 4;
            }
            case LADINO -> {
                this.forca = 5; this.destreza = 10; this.intelecto = 5; this.carisma = 8; this.constituicao = 5;
            }
        }
        
        // Calcula Status derivados
        this.maxHp = this.constituicao * 10;
        this.hpAtual = this.maxHp;
        this.maxMp = this.intelecto * 10;
        this.mpAtual = this.maxMp;
    }
}