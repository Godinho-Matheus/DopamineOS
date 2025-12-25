package com.dopamineos.backend.entity.enums;

public enum Dificuldade {
    EASY(10, 2),
    MEDIUM(30, 5),
    HARD(60, 15),
    EPIC(100, 50);

    private final int xpBase;
    private final int goldBase;

    Dificuldade(int xpBase, int goldBase) {
        this.xpBase = xpBase;
        this.goldBase = goldBase;
    }

    public int getXpBase() { return xpBase; }
    public int getGoldBase() { return goldBase; }
}