package com.dopamineos.backend.entity.enums;

import lombok.Getter;

@Getter // Lombok evita boilerplate de getters manuais
public enum Dificuldade {
    EASY(10, 2, 1),
    MEDIUM(30, 5, 2),
    HARD(60, 15, 3),
    EPIC(100, 50, 5);

    private final int xpBase;
    private final int goldBase;
    private final int pontosAtributo; // Novo campo!

    Dificuldade(int xpBase, int goldBase, int pontosAtributo) {
        this.xpBase = xpBase;
        this.goldBase = goldBase;
        this.pontosAtributo = pontosAtributo;
    }
}