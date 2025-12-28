package com.dopamineos.backend.controller;

import com.dopamineos.backend.dto.ProtocoloDTO;
import com.dopamineos.backend.dto.SetupRequest;
import com.dopamineos.backend.dto.UsuarioDTO;
import com.dopamineos.backend.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class GameController {

    private final GameService gameService;

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDTO> getPerfil() {
        return ResponseEntity.ok(gameService.obterPerfil());
    }

    @GetMapping("/protocolos")
    public ResponseEntity<List<ProtocoloDTO>> listarProtocolos() {
        List<ProtocoloDTO> protocolos = gameService.listarProtocolos();
        return ResponseEntity.ok(protocolos);
    }

    @PostMapping("/protocolos")
    public ResponseEntity<ProtocoloDTO> criarProtocolo(@RequestBody @Valid ProtocoloDTO dto) {
        ProtocoloDTO novoProtocolo = gameService.criarProtocolo(dto);
                URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(novoProtocolo.id())
                .toUri();
        return ResponseEntity.created(location).body(novoProtocolo);
    }

    @PostMapping("/checkin/{protocoloId}")
    public ResponseEntity<UsuarioDTO> realizarAtividade(@PathVariable Long protocoloId) {
        return ResponseEntity.ok(gameService.realizarCheckin(protocoloId));
    }

    @PostMapping("/setup")
    public ResponseEntity<UsuarioDTO> configurarPersonagem(@RequestBody @Valid SetupRequest request) {
        UsuarioDTO usuario = gameService.configurarPersonagem(request.getNome(), request.getClasse());
        return ResponseEntity.ok(usuario);
    }
}