package com.dopamineos.backend.config;

import com.dopamineos.backend.entity.Usuario;
import com.dopamineos.backend.entity.enums.ClasseRPG;
import com.dopamineos.backend.repository.ProtocoloRepository;
import com.dopamineos.backend.repository.UsuarioRepository;
import com.dopamineos.backend.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GameLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProtocoloRepository protocoloRepository;
    private final GameService gameService;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("🎮 Personagem já existe. Seed ignorado.");
            return;
        }

        log.info("🎮 Criando personagem padrão 'Alex, o Paladino'...");

        Usuario u = new Usuario();
        u.resetarPersonagem("Alex, o Paladino", ClasseRPG.GUERREIRO);
        Usuario salvo = usuarioRepository.save(u);

        if (protocoloRepository.count() == 0) {
            gameService.criarMissoesPadrao(salvo);
        }

        log.info("✅ Seed inicial concluído!");
    }
}
