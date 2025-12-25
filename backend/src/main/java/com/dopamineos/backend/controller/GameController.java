package com.dopamineos.backend.controller;

import com.dopamineos.backend.dto.ProtocoloDTO;
import com.dopamineos.backend.dto.SetupRequest;
import com.dopamineos.backend.dto.UsuarioDTO;
import com.dopamineos.backend.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;

    /**
     * 1. Obtém o perfil do jogador
     */
    @GetMapping("/perfil")
    public UsuarioDTO getPerfil() {
        return gameService.obterPerfil();
    }

    /**
     * 2. Lista todos os protocolos (missões)
     */
    @GetMapping("/protocolos")
    public List<ProtocoloDTO> listarProtocolos() {
        return gameService.listarProtocolos();
    }

    /**
     * 3. Cria um novo protocolo (missão)
     */
    @PostMapping("/protocolos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProtocoloDTO criarProtocolo(@RequestBody ProtocoloDTO protocolo) {
        return gameService.criarProtocolo(protocolo);
    }

    /**
     * 4. Realiza check-in (completa uma missão)
     */
    @PostMapping("/checkin/{protocoloId}")
    public UsuarioDTO realizarAtividade(@PathVariable Long protocoloId) {
        return gameService.realizarCheckin(protocoloId);
    }

    /**
     * 5. Configura o personagem (setup inicial)
     */
    @PostMapping("/setup")
    public UsuarioDTO escolherClasse(@RequestBody SetupRequest request) {
        return gameService.configurarPersonagem(request.getNome(), request.getClasse());
    }
}