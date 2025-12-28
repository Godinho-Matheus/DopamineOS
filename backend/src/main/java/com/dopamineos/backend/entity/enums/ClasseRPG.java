package com.dopamineos.backend.entity.enums;

public enum ClasseRPG {
    GUERREIRO,
    MAGO,
    LADINO;

    public double getMultiplicador(Atributo atributo) {
        return switch (this) {
            case GUERREIRO -> (atributo == Atributo.FORCA || atributo == Atributo.CONSTITUICAO) ? 1.3 : 1.0;
            case MAGO -> (atributo == Atributo.INTELECTO) ? 1.5 : 1.0;
            case LADINO -> (atributo == Atributo.DESTREZA || atributo == Atributo.CARISMA) ? 1.3 : 1.0;
        };
    }
}