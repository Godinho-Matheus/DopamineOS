package com.dopamineos.backend.service;

import com.dopamineos.backend.entity.LogAtividade;
import com.dopamineos.backend.entity.Protocolo;
import com.dopamineos.backend.entity.Usuario;
import com.dopamineos.backend.entity.enums.Atributo;
import com.dopamineos.backend.entity.enums.Dificuldade;
import com.dopamineos.backend.repository.LogAtividadeRepository;
import com.dopamineos.backend.repository.ProtocoloRepository;
import com.dopamineos.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final UsuarioRepository usuarioRepository;
    private final ProtocoloRepository protocoloRepository;
    private final LogAtividadeRepository logAtividadeRepository;

    public Usuario getPerfil() {
        return usuarioRepository.findAll().stream().findFirst().orElse(null);
    }

    public List<Protocolo> getProtocolos() {
        return protocoloRepository.findAll();
    }

    public List<LogAtividade> getHistoricoCompleto() {
        return logAtividadeRepository.findByOrderByDataHoraDesc();
    }

    @Transactional
    public Usuario setup(Usuario usuario, List<Protocolo> tarefasIniciais) {
        log.info("🚀 Iniciando Setup do Personagem...");

        // 1. LIMPEZA TOTAL
        logAtividadeRepository.deleteAll(); 
        log.info("🧹 Histórico antigo apagado.");
        
        usuarioRepository.deleteAll(); 
        log.info("🧹 Usuário antigo apagado.");
        
        protocoloRepository.deleteAll();
        log.info("🧹 Missões antigas apagadas.");

        // 2. CRIAÇÃO DO USUÁRIO
        // Recalcula HP/MP baseado na classe escolhida
        usuario.resetarPersonagem(usuario.getNome(), usuario.getClasse());
        Usuario salvo = usuarioRepository.save(usuario);
        log.info("✨ Usuário criado: {} (Classe: {})", salvo.getNome(), salvo.getClasse());

        // 3. CRIAÇÃO DAS MISSÕES
        if (tarefasIniciais != null && !tarefasIniciais.isEmpty()) {
            // Cenário A: Usuário escolheu missões no Frontend
            protocoloRepository.saveAll(tarefasIniciais);
            log.info("🃏 {} missões personalizadas salvas.", tarefasIniciais.size());
        } else {
            // Cenário B: Usuário não escolheu nada (Fallback)
            log.info("🃏 Nenhuma missão selecionada. Criando Deck Padrão...");
            criarMissoesPadrao();
        }

        return salvo;
    }

    @Transactional
    public Usuario fazerCheckin(Long protocoloId) {
        Usuario user = getPerfil();
        
        // Busca a missão ou lança erro
        Protocolo task = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new RuntimeException("Missão não encontrada com ID: " + protocoloId));

        // 1. Lógica de RPG (Calcular recompensas)
        int xpGanho = calcularXp(task.getDificuldade());
        int goldGanho = calcularGold(task.getDificuldade());
        
        // Aplica no usuário
        user.ganharXp(xpGanho);
        user.setMoedas(user.getMoedas() + goldGanho);

        // 2. Salva no Histórico (Log)
        LogAtividade logEntry = new LogAtividade();
        logEntry.setNomeMissao(task.getNome());
        logEntry.setIcone(task.getIcone());
        logEntry.setXpGanho(xpGanho);
        logEntry.setDuracaoMinutos(task.getDuracaoMinutos());
        logEntry.setDataHora(LocalDateTime.now());
        logEntry.setUsuario(user);
        
        logAtividadeRepository.save(logEntry);

        log.info("✅ Checkin: {} (+{} XP)", task.getNome(), xpGanho);

        // 3. Salva e retorna usuário atualizado
        return usuarioRepository.save(user);
    }

    @Transactional
    public Protocolo salvarMissaoAvulsa(Protocolo p) {
        return protocoloRepository.save(p);
    }

    @Transactional
    public void deletarProtocolo(Long id) {
        protocoloRepository.deleteById(id);
    }

    @Transactional
    public List<Protocolo> salvarMissoes(List<Protocolo> protocolos) {
        return protocoloRepository.saveAll(protocolos);
    }

    private void criarMissoesPadrao() {
        // Criação manual das missões padrão se o usuário não selecionar nada
        List<Protocolo> padroes = List.of(
            criar("Beber Água", "💧", Atributo.CONSTITUICAO, Dificuldade.EASY, 5),
            criar("Alongamento", "🧘", Atributo.DESTREZA, Dificuldade.EASY, 10),
            criar("Leitura Focada", "📚", Atributo.INTELECTO, Dificuldade.MEDIUM, 30),
            criar("Treino Físico", "💪", Atributo.FORCA, Dificuldade.HARD, 60),
            criar("Networking", "🤝", Atributo.CARISMA, Dificuldade.MEDIUM, 20),
            criar("Sono Reparador", "💤", Atributo.CONSTITUICAO, Dificuldade.EPIC, 480)
        );
        
        protocoloRepository.saveAll(padroes);
    }

    // Helper para instanciar Protocolo rapidamente
    private Protocolo criar(String nome, String icone, Atributo atr, Dificuldade dif, int minutos) {
        Protocolo p = new Protocolo();
        p.setNome(nome);
        p.setIcone(icone);
        p.setAtributo(atr);
        p.setDificuldade(dif);
        p.setDuracaoMinutos(minutos);
        p.setDescricao("Rotina Inicial");
        return p;
    }

    private int calcularXp(Dificuldade d) {
        if (d == null) return 10;
        return switch (d) {
            case EASY -> 15;
            case MEDIUM -> 30;
            case HARD -> 50;
            case EPIC -> 100;
        };
    }

    private int calcularGold(Dificuldade d) {
        if (d == null) return 5;
        return switch (d) {
            case EASY -> 5;
            case MEDIUM -> 10;
            case HARD -> 20;
            case EPIC -> 50;
        };
    }
}