package com.dopamineos.backend.entity;

import com.dopamineos.backend.entity.enums.Atributo;
import com.dopamineos.backend.entity.enums.ClasseRPG;
import com.dopamineos.backend.entity.enums.Dificuldade;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_usuarios")
@Getter @Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;
    
    @Enumerated(EnumType.STRING)
    private ClasseRPG classe;

    private int nivel = 1;
    private int xpAtual = 0;
    private int xpParaProximoNivel = 100;
    private int moedas = 0;

    // Atributos Base
    private int forca = 5;
    private int destreza = 5;
    private int intelecto = 5;
    private int carisma = 5;
    private int constituicao = 5;

    // Status Calculados
    private int hpAtual;
    private int maxHp;
    private int mpAtual;
    private int maxMp;


    // 1. Calcula o bônus da classe
    public double calcularMultiplicadorClasse(Protocolo protocolo) {
        if (this.classe == null) return 1.0;
        return this.classe.getMultiplicador(protocolo.getAtributo());
    }

    // 2. Aplica XP e Ouro
    public void receberRecompensas(int xp, int gold) {
        this.moedas += gold;
        this.adicionarXp(xp);
    }

    // 3. Aplica ganho de atributos baseado na dificuldade
public void aplicarGanhoDeAtributos(Atributo atributo, Dificuldade dificuldade) {
        int ganho = dificuldade.getPontosAtributo();
        switch (atributo) {
            case FORCA -> this.forca += ganho;
            case DESTREZA -> this.destreza += ganho;
            case INTELECTO -> {
                this.intelecto += ganho;
                this.maxMp = this.intelecto * 10;
                this.mpAtual += (ganho * 10);
            }
            case CARISMA -> this.carisma += ganho;
            case CONSTITUICAO -> {
                this.constituicao += ganho;
                this.maxHp = this.constituicao * 10;
                this.hpAtual += (ganho * 10);
            }
        }
    }

    // 4. Reseta ou Cria Personagem
    public void resetarPersonagem(String nome, ClasseRPG classe) {
        this.nome = nome;
        this.classe = classe;
        this.nivel = 1;
        this.xpAtual = 0;
        this.xpParaProximoNivel = 100;
        this.moedas = 0;
        
        // Reset atributos base
        this.forca = 5;
        this.destreza = 5;
        this.intelecto = 5;
        this.carisma = 5;
        this.constituicao = 5;

        // Aplica bônus iniciais da classe
        if (classe != null) {
            switch (classe) {
                case GUERREIRO -> { this.forca = 12; this.constituicao = 12; }
                case MAGO -> { this.intelecto = 15; }
                case LADINO -> { this.destreza = 12; this.carisma = 10; }
            }
        }
        
        recuperarStatusTotal();
    }

    // --- MÉTODOS PRIVADOS AUXILIARES ---

    private void adicionarXp(int xp) {
        this.xpAtual += xp;
        while (this.xpAtual >= this.xpParaProximoNivel) {
            subirDeNivel();
        }
    }

    private void subirDeNivel() {
        this.xpAtual -= this.xpParaProximoNivel;
        this.nivel++;
        this.xpParaProximoNivel = (int) (this.xpParaProximoNivel * 1.2);
        recuperarStatusTotal();
    }

    private void recuperarStatusTotal() {
        this.maxHp = this.constituicao * 10;
        this.hpAtual = this.maxHp;
        this.maxMp = this.intelecto * 10;
        this.mpAtual = this.maxMp;
    }
}