package com.dopamineos.backend.controller;

import com.dopamineos.backend.dto.ProtocoloDTO;
import com.dopamineos.backend.dto.SetupRequest;
import com.dopamineos.backend.dto.UsuarioDTO;
import com.dopamineos.backend.entity.LogAtividade;
import com.dopamineos.backend.entity.Protocolo;
import com.dopamineos.backend.entity.Usuario;
import com.dopamineos.backend.mapper.GameMapper;
import com.dopamineos.backend.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDTO> getPerfil() {
        Usuario usuario = gameService.getPerfil();
        return ResponseEntity.ok(gameMapper.toUsuarioDTO(usuario));
    }

    @GetMapping("/protocolos")
    public ResponseEntity<List<ProtocoloDTO>> getProtocolos() {
        List<Protocolo> protocolos = gameService.getProtocolos();
        return ResponseEntity.ok(protocolos.stream().map(gameMapper::toProtocoloDTO).toList());
    }

    @PostMapping("/setup")
    public ResponseEntity<UsuarioDTO> setup(@RequestBody @Valid SetupRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setClasse(request.classe());
        
        Usuario criado = gameService.setup(usuario, null);
        return ResponseEntity.ok(gameMapper.toUsuarioDTO(criado));
    }

    // Endpoint para criar múltiplas missões
    @PostMapping("/protocolos")
    public ResponseEntity<List<ProtocoloDTO>> criarProtocolos(@RequestBody List<ProtocoloDTO> dtos) {
        Usuario usuario = gameService.getPerfil();
        if (usuario == null) {
            return ResponseEntity.badRequest().build(); // Segurança extra
        }
        List<Protocolo> entidades = dtos.stream()
                .map(dto -> {
                    Protocolo entidade = gameMapper.toProtocoloEntity(dto);
                    entidade.setUsuario(usuario);
                    return entidade;
                })
                .toList();
        List<Protocolo> salvos = gameService.salvarMissoes(entidades);
        return ResponseEntity.ok(salvos.stream()
                .map(gameMapper::toProtocoloDTO)
                .toList());
    }
    
    // Endpoint para criar UMA missão
    @PostMapping("/protocolo")
    public ResponseEntity<ProtocoloDTO> criarProtocolo(@RequestBody @Valid ProtocoloDTO dto) {
        Usuario usuario = gameService.getPerfil();
        Protocolo entidade = gameMapper.toProtocoloEntity(dto);
        entidade.setUsuario(usuario);
        Protocolo salva = gameService.salvarMissaoAvulsa(entidade);
        return ResponseEntity.ok(gameMapper.toProtocoloDTO(salva));
    }

    @DeleteMapping("/protocolo/{id}")
    public ResponseEntity<Void> deletarProtocolo(@PathVariable Long id) {
        gameService.deletarProtocolo(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkin/{id}")
    public ResponseEntity<UsuarioDTO> checkin(@PathVariable Long id) {
        Usuario usuarioAtualizado = gameService.fazerCheckin(id);
        return ResponseEntity.ok(gameMapper.toUsuarioDTO(usuarioAtualizado));
    }
    
    @GetMapping("/historico")
    public ResponseEntity<List<LogAtividade>> getHistorico() {
        return ResponseEntity.ok(gameService.getHistoricoCompleto());
    }
}