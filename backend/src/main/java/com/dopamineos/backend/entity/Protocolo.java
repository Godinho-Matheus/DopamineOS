package com.dopamineos.backend.entity;

import com.dopamineos.backend.entity.enums.Atributo;
import com.dopamineos.backend.entity.enums.Dificuldade;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_protocolos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Protocolo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String icone;
    private String cor;

    private int duracaoMinutos = 30;

    @Enumerated(EnumType.STRING)
    private Atributo atributo;

    @Enumerated(EnumType.STRING)
    private Dificuldade dificuldade;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}