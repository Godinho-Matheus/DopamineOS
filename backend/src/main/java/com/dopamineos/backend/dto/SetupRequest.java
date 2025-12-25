package com.dopamineos.backend.dto;

import com.dopamineos.backend.entity.enums.ClasseRPG;
import java.util.List;

public class SetupRequest {
    private String nome;
    private ClasseRPG classe;
    private List<ProtocoloDTO> tarefasIniciais;

    public SetupRequest() {}

    public SetupRequest(String nome, ClasseRPG classe, List<ProtocoloDTO> tarefasIniciais) {
        this.nome = nome;
        this.classe = classe;
        this.tarefasIniciais = tarefasIniciais;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public ClasseRPG getClasse() { return classe; }
    public void setClasse(ClasseRPG classe) { this.classe = classe; }

    public List<ProtocoloDTO> getTarefasIniciais() { return tarefasIniciais; }
    public void setTarefasIniciais(List<ProtocoloDTO> tarefasIniciais) { this.tarefasIniciais = tarefasIniciais; }
}
