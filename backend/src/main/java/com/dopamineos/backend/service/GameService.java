package com.dopamineos.backend.service;

import com.dopamineos.backend.dto.ProtocoloDTO;
import com.dopamineos.backend.dto.UsuarioDTO;
import com.dopamineos.backend.entity.LogAtividade;
import com.dopamineos.backend.entity.Protocolo;
import com.dopamineos.backend.entity.Usuario;
import com.dopamineos.backend.entity.enums.ClasseRPG;
import com.dopamineos.backend.mapper.GameMapper;
import com.dopamineos.backend.exception.ResourceNotFoundException;
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
    private final GameMapper gameMapper;


    private Usuario getJogadorAtual() {
        return usuarioRepository.findAll().stream().findFirst()
                .orElseGet(this::criarJogadorPadrao);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obterPerfil() {
        return gameMapper.toUsuarioDTO(getJogadorAtual());
    }

    @Transactional(readOnly = true)
    public List<ProtocoloDTO> listarProtocolos() {
        return protocoloRepository.findAll().stream()
                .map(gameMapper::toProtocoloDTO)
                .toList();
    }

    @Transactional
    public ProtocoloDTO criarProtocolo(ProtocoloDTO dto) {
        Protocolo protocolo = gameMapper.toProtocoloEntity(dto);
        Protocolo salvo = protocoloRepository.save(protocolo);
        return gameMapper.toProtocoloDTO(salvo);
    }

    @Transactional
    public UsuarioDTO realizarCheckin(Long protocoloId) {
        Usuario jogador = getJogadorAtual();
        
        Protocolo protocolo = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new ResourceNotFoundException("Protocolo não encontrado com ID: " + protocoloId));
        
        double multiplicador = jogador.calcularMultiplicadorClasse(protocolo);
        int xpGanho = (int) (protocolo.getDificuldade().getXpBase() * multiplicador);
        int goldGanho = protocolo.getDificuldade().getGoldBase();

        jogador.receberRecompensas(xpGanho, goldGanho);
        jogador.aplicarGanhoDeAtributos(protocolo.getAtributo(), protocolo.getDificuldade());

        usuarioRepository.save(jogador);
        registrarLog(jogador, protocolo, xpGanho, goldGanho);

        log.info("Checkin realizado: User {} | XP+ {} | Gold+ {}", jogador.getNome(), xpGanho, goldGanho);
        
        return gameMapper.toUsuarioDTO(jogador);
    }

    @Transactional
    public UsuarioDTO configurarPersonagem(String nome, ClasseRPG classe) {
        Usuario jogador = getJogadorAtual();

        // Limpeza de histórico.
        logAtividadeRepository.deleteAll();
        
        jogador.resetarPersonagem(nome, classe);
        
        Usuario atualizado = usuarioRepository.save(jogador);
        return gameMapper.toUsuarioDTO(atualizado);
    }

    private void registrarLog(Usuario jogador, Protocolo protocolo, int xp, int gold) {
        LogAtividade log = new LogAtividade();
        log.setUsuario(jogador);
        log.setProtocolo(protocolo);
        log.setDataHora(LocalDateTime.now());
        log.setXpGanho(xp);
        log.setGoldGanho(gold);
        logAtividadeRepository.save(log);
    }

    private Usuario criarJogadorPadrao() {
        log.info("Criando jogador padrão...");
        Usuario novo = new Usuario();
        novo.resetarPersonagem("Viajante", null);
        return usuarioRepository.save(novo);
    }
}