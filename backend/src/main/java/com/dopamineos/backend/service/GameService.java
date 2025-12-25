package com.dopamineos.backend.service;

import com.dopamineos.backend.dto.ProtocoloDTO;
import com.dopamineos.backend.dto.UsuarioDTO;
import com.dopamineos.backend.entity.LogAtividade;
import com.dopamineos.backend.entity.Protocolo;
import com.dopamineos.backend.entity.Usuario;
import com.dopamineos.backend.entity.enums.ClasseRPG;
import com.dopamineos.backend.repository.LogAtividadeRepository;
import com.dopamineos.backend.repository.ProtocoloRepository;
import com.dopamineos.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProtocoloRepository protocoloRepository;

    @Autowired
    private LogAtividadeRepository logAtividadeRepository;

    /**
     * Retorna o jogador principal (singleton pattern)
     */
    public Usuario pegarJogador() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        if (usuarios.isEmpty()) {
            return criarJogadorPadrao();
        }
        return usuarios.get(0);
    }

    /**
     * Cria o jogador padrão com stats iniciais
     */
    private Usuario criarJogadorPadrao() {
        Usuario novo = new Usuario();
        novo.setNome("Viajante");
        novo.setNivel(1);
        novo.setXpAtual(0);
        novo.setXpParaProximoNivel(100);
        novo.setForca(5);
        novo.setDestreza(5);
        novo.setIntelecto(5);
        novo.setCarisma(5);
        novo.setConstituicao(5);
        novo.setMaxHp(50);
        novo.setHpAtual(50);
        novo.setMaxMp(50);
        novo.setMpAtual(50);
        novo.setMoedas(0);
        return usuarioRepository.save(novo);
    }

    /**
     * Retorna o perfil do jogador como DTO
     */
    public UsuarioDTO obterPerfil() {
        return converterParaDTO(pegarJogador());
    }

    /**
     * Retorna lista de protocolos disponíveis
     */
    public List<ProtocoloDTO> listarProtocolos() {
        return protocoloRepository.findAll()
                .stream()
                .map(this::converterProtocoloParaDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cria um novo protocolo
     */
    public ProtocoloDTO criarProtocolo(ProtocoloDTO dto) {
        Protocolo protocolo = converterDTOParaProtocolo(dto);
        Protocolo salvo = protocoloRepository.save(protocolo);
        return converterProtocoloParaDTO(salvo);
    }

    /**
     * Realiza check-in de um protocolo (completa missão)
     */
    public UsuarioDTO realizarCheckin(Long protocoloId) {
        Usuario jogador = pegarJogador();
        Protocolo protocolo = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new RuntimeException("Protocolo não encontrado"));

        // Calcula XP e Gold
        double multiplicador = jogador.getClasse() != null 
                ? jogador.getClasse().getMultiplicador(protocolo.getAtributo()) 
                : 1.0;
        int xpFinal = (int) (protocolo.getDificuldade().getXpBase() * multiplicador);
        int goldFinal = protocolo.getDificuldade().getGoldBase();

        // Calcula ganho de atributos
        int statGain = switch (protocolo.getDificuldade()) {
            case EASY -> 1;
            case MEDIUM -> 2;
            case HARD -> 3;
            case EPIC -> 5;
        };

        // Aplica ganhos
        jogador.setXpAtual(jogador.getXpAtual() + xpFinal);
        jogador.setMoedas(jogador.getMoedas() + goldFinal);

        // Aumenta atributos específicos
        switch (protocolo.getAtributo()) {
            case FORCA -> jogador.setForca(jogador.getForca() + statGain);
            case DESTREZA -> jogador.setDestreza(jogador.getDestreza() + statGain);
            case INTELECTO -> {
                jogador.setIntelecto(jogador.getIntelecto() + statGain);
                jogador.setMaxMp(jogador.getIntelecto() * 10);
                jogador.setMpAtual(jogador.getMpAtual() + (statGain * 10));
            }
            case CARISMA -> jogador.setCarisma(jogador.getCarisma() + statGain);
            case CONSTITUICAO -> {
                jogador.setConstituicao(jogador.getConstituicao() + statGain);
                jogador.setMaxHp(jogador.getConstituicao() * 10);
                jogador.setHpAtual(jogador.getHpAtual() + (statGain * 10));
            }
        }

        // Verifica level up
        if (jogador.getXpAtual() >= jogador.getXpParaProximoNivel()) {
            jogador.setXpAtual(jogador.getXpAtual() - jogador.getXpParaProximoNivel());
            jogador.setNivel(jogador.getNivel() + 1);
            jogador.setHpAtual(jogador.getMaxHp());
            jogador.setMpAtual(jogador.getMaxMp());
            int novaMeta = (int) (jogador.getXpParaProximoNivel() * 1.2);
            jogador.setXpParaProximoNivel(novaMeta);
        }

        // Registra log de atividade
        LogAtividade log = new LogAtividade();
        log.setUsuario(jogador);
        log.setProtocolo(protocolo);
        log.setDataHora(LocalDateTime.now());
        log.setXpGanho(xpFinal);
        log.setGoldGanho(goldFinal);
        logAtividadeRepository.saveAndFlush(log);

        Usuario atualizado = usuarioRepository.save(jogador);
        return converterParaDTO(atualizado);
    }

    /**
     * Configura o personagem (setup inicial)
     */
    public UsuarioDTO configurarPersonagem(String nome, ClasseRPG classe) {
        Usuario jogador = pegarJogador();
        
        // Limpa dados anteriores
        logAtividadeRepository.deleteAll();
        protocoloRepository.deleteAll();

        // Define novo personagem
        jogador.setNome(nome);
        jogador.setClasse(classe);
        jogador.setNivel(1);
        jogador.setXpAtual(0);
        jogador.setXpParaProximoNivel(100);
        jogador.setMoedas(0);
        jogador.setForca(5);
        jogador.setDestreza(5);
        jogador.setIntelecto(5);
        jogador.setCarisma(5);
        jogador.setConstituicao(5);

        // Aplica bônus de classe
        if (classe != null) {
            switch (classe) {
                case GUERREIRO -> {
                    jogador.setForca(12);
                    jogador.setConstituicao(12);
                }
                case MAGO -> jogador.setIntelecto(15);
                case LADINO -> {
                    jogador.setDestreza(12);
                    jogador.setCarisma(10);
                }
            }
        }

        // Recalcula stats
        jogador.setMaxHp(jogador.getConstituicao() * 10);
        jogador.setHpAtual(jogador.getMaxHp());
        jogador.setMaxMp(jogador.getIntelecto() * 10);
        jogador.setMpAtual(jogador.getMaxMp());

        Usuario atualizado = usuarioRepository.save(jogador);
        return converterParaDTO(atualizado);
    }

    /**
     * Converte Usuario para DTO
     */
    private UsuarioDTO converterParaDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getClasse(),
                usuario.getNivel(),
                usuario.getXpAtual(),
                usuario.getXpParaProximoNivel(),
                usuario.getMoedas(),
                usuario.getForca(),
                usuario.getDestreza(),
                usuario.getIntelecto(),
                usuario.getCarisma(),
                usuario.getConstituicao(),
                usuario.getHpAtual(),
                usuario.getMaxHp(),
                usuario.getMpAtual(),
                usuario.getMaxMp()
        );
    }

    /**
     * Converte Protocolo para DTO
     */
    private ProtocoloDTO converterProtocoloParaDTO(Protocolo protocolo) {
        return new ProtocoloDTO(
                protocolo.getId(),
                protocolo.getNome(),
                protocolo.getIcone(),
                protocolo.getCor(),
                protocolo.getDuracaoMinutos(),
                protocolo.getAtributo(),
                protocolo.getDificuldade()
        );
    }

    /**
     * Converte DTO para Protocolo
     */
    private Protocolo converterDTOParaProtocolo(ProtocoloDTO dto) {
        Protocolo protocolo = new Protocolo();
        protocolo.setNome(dto.getNome());
        protocolo.setIcone(dto.getIcone());
        protocolo.setCor(dto.getCor());
        protocolo.setDuracaoMinutos(dto.getDuracaoMinutos());
        protocolo.setAtributo(dto.getAtributo());
        protocolo.setDificuldade(dto.getDificuldade());
        return protocolo;
    }
}
