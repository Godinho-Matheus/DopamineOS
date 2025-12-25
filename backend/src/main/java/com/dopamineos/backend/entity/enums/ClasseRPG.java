package com.dopamineos.backend.entity.enums;

public enum ClasseRPG {
    GUERREIRO,
    MAGO,
    LADINO;

    // Método que define o bônus de XP baseado no atributo da tarefa
    public double getMultiplicador(Atributo atributo) {
        switch (this) {
            case GUERREIRO:
                if (atributo == Atributo.FORCA || atributo == Atributo.CONSTITUICAO) return 1.3;
                break;
            case MAGO:
                if (atributo == Atributo.INTELECTO) return 1.5;
                break;
            case LADINO:
                if (atributo == Atributo.DESTREZA || atributo == Atributo.CARISMA) return 1.3;
                break;
        }
        return 1.0;
    }
}