package com.lumeos.backend.service;

import com.lumeos.backend.entity.LogAtividade;
import com.lumeos.backend.entity.Protocolo;
import com.lumeos.backend.entity.Usuario;
import com.lumeos.backend.entity.enums.Atributo;
import com.lumeos.backend.entity.enums.Dificuldade;
import com.lumeos.backend.exception.ResourceNotFoundException;
import com.lumeos.backend.repository.LogAtividadeRepository;
import com.lumeos.backend.repository.ProtocoloRepository;
import com.lumeos.backend.repository.UsuarioRepository;
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

    /**
     * Retorna o único jogador existente ou lança 404.
     */
    public Usuario getPerfil() {
        return usuarioRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum personagem encontrado. Faça o Setup primeiro."));
    }

    public List<Protocolo> getProtocolos() {
        return protocoloRepository.findAll();
    }

    public List<LogAtividade> getHistoricoCompleto() {
        return logAtividadeRepository.findByOrderByDataHoraDesc();
    }

    /**
     * Cria um novo personagem, apagando todos os dados anteriores.
     * Ordem de delete respeitando FK: logs → protocolos → usuário.
     */
    @Transactional
    public Usuario setup(Usuario usuario) {
        log.info("🚀 Iniciando Setup do Personagem...");

        // 1. LIMPEZA na ordem correta (respeita FK: log → protocolo → usuario)
        logAtividadeRepository.deleteAll();
        log.info("🧹 Histórico antigo apagado.");

        protocoloRepository.deleteAll();
        log.info("🧹 Missões antigas apagadas.");

        usuarioRepository.deleteAll();
        log.info("🧹 Usuário antigo apagado.");

        // 2. CRIAÇÃO DO USUÁRIO (resetarPersonagem calcula HP/MP/atributos da classe)
        usuario.resetarPersonagem(usuario.getNome(), usuario.getClasse());
        Usuario salvo = usuarioRepository.save(usuario);
        log.info("✨ Usuário criado: {} (Classe: {})", salvo.getNome(), salvo.getClasse());

        return salvo;
    }

    @Transactional
    public Usuario fazerCheckin(Long protocoloId) {
        // getPerfil() já lança ResourceNotFoundException se não houver usuário
        Usuario user = getPerfil();

        Protocolo task = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada com ID: " + protocoloId));

        // Usa os valores definidos no próprio enum Dificuldade (fonte única da verdade)
        int xpGanho = task.getDificuldade() != null ? task.getDificuldade().getXpBase() : 10;
        int goldGanho = task.getDificuldade() != null ? task.getDificuldade().getGoldBase() : 2;

        user.ganharXp(xpGanho);
        user.setMoedas(user.getMoedas() + goldGanho);

        // Registra no histórico
        LogAtividade logEntry = new LogAtividade();
        logEntry.setNomeMissao(task.getNome());
        logEntry.setIcone(task.getIcone());
        logEntry.setXpGanho(xpGanho);
        logEntry.setDuracaoMinutos(task.getDuracaoMinutos());
        logEntry.setDataHora(LocalDateTime.now());
        logEntry.setUsuario(user);

        logAtividadeRepository.save(logEntry);
        log.info("✅ Checkin: {} (+{} XP, +{} gold)", task.getNome(), xpGanho, goldGanho);

        return usuarioRepository.save(user);
    }

    @Transactional
    public Protocolo salvarMissaoAvulsa(Protocolo p) {
        return protocoloRepository.save(p);
    }

    @Transactional
    public void deletarProtocolo(Long id) {
        if (!protocoloRepository.existsById(id)) {
            throw new ResourceNotFoundException("Missão não encontrada com ID: " + id);
        }
        protocoloRepository.deleteById(id);
    }

    @Transactional
    public List<Protocolo> salvarMissoes(List<Protocolo> protocolos) {
        return protocoloRepository.saveAll(protocolos);
    }

    /**
     * Cria o deck padrão de missões associadas ao usuário informado.
     */
    public void criarMissoesPadrao(Usuario usuario) {
        List<Protocolo> padroes = List.of(
            criar("Beber Água",     "💧", Atributo.CONSTITUICAO, Dificuldade.EASY,   5,   usuario),
            criar("Alongamento",   "🧘", Atributo.DESTREZA,     Dificuldade.EASY,   10,  usuario),
            criar("Leitura Focada","📚", Atributo.INTELECTO,    Dificuldade.MEDIUM, 30,  usuario),
            criar("Treino Físico", "💪", Atributo.FORCA,        Dificuldade.HARD,   60,  usuario),
            criar("Networking",    "🤝", Atributo.CARISMA,      Dificuldade.MEDIUM, 20,  usuario),
            criar("Sono Reparador","💤", Atributo.CONSTITUICAO, Dificuldade.EPIC,   480, usuario)
        );
        protocoloRepository.saveAll(padroes);
        log.info("🃏 Deck padrão criado: {} missões.", padroes.size());
    }

    private Protocolo criar(String nome, String icone, Atributo atr, Dificuldade dif, int minutos, Usuario usuario) {
        Protocolo p = new Protocolo();
        p.setNome(nome);
        p.setIcone(icone);
        p.setAtributo(atr);
        p.setDificuldade(dif);
        p.setDuracaoMinutos(minutos);
        p.setDescricao("Rotina Inicial");
        p.setUsuario(usuario);
        return p;
    }
}