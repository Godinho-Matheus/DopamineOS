package com.dopamineos.backend.entity;

import com.dopamineos.backend.entity.enums.Atributo;
import com.dopamineos.backend.entity.enums.Dificuldade;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tb_protocolos")
public class Protocolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String icone;
    private String cor;
    
    private String descricao;

    @Enumerated(EnumType.STRING)
    private Atributo atributo;

    @Enumerated(EnumType.STRING)
    private Dificuldade dificuldade;
    
    private int duracaoMinutos;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}