package com.dopamineos.backend.controller;

import com.dopamineos.backend.dto.LogAtividadeDTO;
import com.dopamineos.backend.dto.ProtocoloDTO;
import com.dopamineos.backend.dto.SetupRequest;
import com.dopamineos.backend.dto.UsuarioDTO;
import com.dopamineos.backend.entity.Protocolo;
import com.dopamineos.backend.entity.Usuario;
import com.dopamineos.backend.mapper.GameMapper;
import com.dopamineos.backend.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioDTO> getPerfil() {
        // getPerfil() lança ResourceNotFoundException se não houver usuário → 404
        return ResponseEntity.ok(gameMapper.toUsuarioDTO(gameService.getPerfil()));
    }

    @GetMapping("/protocolos")
    public ResponseEntity<List<ProtocoloDTO>> getProtocolos() {
        List<Protocolo> protocolos = gameService.getProtocolos();
        return ResponseEntity.ok(protocolos.stream().map(gameMapper::toProtocoloDTO).toList());
    }

    /**
     * Cria o personagem e, opcionalmente, salva as missões escolhidas no setup.
     * Se nenhuma missão for enviada, cria o deck padrão.
     */
    @PostMapping("/setup")
    public ResponseEntity<UsuarioDTO> setup(@RequestBody @Valid SetupRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setClasse(request.classe());

        Usuario criado = gameService.setup(usuario);

        // Salva as missões escolhidas pelo usuário (se houver) ou cria deck padrão
        if (request.missoes() != null && !request.missoes().isEmpty()) {
            List<Protocolo> entidades = request.missoes().stream()
                    .map(dto -> {
                        Protocolo p = gameMapper.toProtocoloEntity(dto);
                        p.setUsuario(criado);
                        return p;
                    })
                    .toList();
            gameService.salvarMissoes(entidades);
        } else {
            gameService.criarMissoesPadrao(criado);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(gameMapper.toUsuarioDTO(criado));
    }

    /** Cria múltiplas missões de uma vez. */
    @PostMapping("/protocolos")
    public ResponseEntity<List<ProtocoloDTO>> criarProtocolos(@RequestBody @Valid List<ProtocoloDTO> dtos) {
        Usuario usuario = gameService.getPerfil();
        List<Protocolo> entidades = dtos.stream()
                .map(dto -> {
                    Protocolo p = gameMapper.toProtocoloEntity(dto);
                    p.setUsuario(usuario);
                    return p;
                })
                .toList();
        List<Protocolo> salvos = gameService.salvarMissoes(entidades);
        return ResponseEntity.ok(salvos.stream().map(gameMapper::toProtocoloDTO).toList());
    }

    /** Cria uma missão avulsa. */
    @PostMapping("/protocolo")
    public ResponseEntity<ProtocoloDTO> criarProtocolo(@RequestBody @Valid ProtocoloDTO dto) {
        Usuario usuario = gameService.getPerfil();
        Protocolo entidade = gameMapper.toProtocoloEntity(dto);
        entidade.setUsuario(usuario);
        Protocolo salva = gameService.salvarMissaoAvulsa(entidade);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameMapper.toProtocoloDTO(salva));
    }

    @DeleteMapping("/protocolo/{id}")
    public ResponseEntity<Void> deletarProtocolo(@PathVariable Long id) {
        gameService.deletarProtocolo(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkin/{id}")
    public ResponseEntity<UsuarioDTO> checkin(@PathVariable Long id) {
        return ResponseEntity.ok(gameMapper.toUsuarioDTO(gameService.fazerCheckin(id)));
    }

    /** Retorna o histórico como DTOs (nunca expõe entidades JPA diretamente). */
    @GetMapping("/historico")
    public ResponseEntity<List<LogAtividadeDTO>> getHistorico() {
        return ResponseEntity.ok(
                gameService.getHistoricoCompleto().stream()
                        .map(gameMapper::toLogAtividadeDTO)
                        .toList()
        );
    }
}