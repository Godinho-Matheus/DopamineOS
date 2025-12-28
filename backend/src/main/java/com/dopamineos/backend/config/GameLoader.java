package com.dopamineos.backend.config;

import com.dopamineos.backend.entity.Protocolo;
import com.dopamineos.backend.entity.Usuario;
import com.dopamineos.backend.entity.enums.Atributo;
import com.dopamineos.backend.entity.enums.ClasseRPG;
import com.dopamineos.backend.entity.enums.Dificuldade;
import com.dopamineos.backend.repository.ProtocoloRepository;
import com.dopamineos.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GameLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProtocoloRepository protocoloRepository;

    @Override
    public void run(String... args) throws Exception {
        verificarECriarJogador();
        verificarECriarMissoes();
    }

    private void verificarECriarJogador() {
        if (usuarioRepository.count() > 0) {
            return;
        }

        log.info("🎮 Criando Player 'Alex, o Paladino'...");

        Usuario u = new Usuario();
        
        u.resetarPersonagem("Alex, o Paladino", ClasseRPG.GUERREIRO);
        
        u.setForca(10);
        u.setDestreza(5);
        u.setIntelecto(10);
        u.setCarisma(5);
        u.setConstituicao(10);
        u.setMaxHp(u.getConstituicao() * 10);
        u.setHpAtual(u.getMaxHp());
        u.setMaxMp(u.getIntelecto() * 10);
        u.setMpAtual(u.getMaxMp());

        usuarioRepository.save(u);
        log.info("✅ Player criado com sucesso!");
    }

    private void verificarECriarMissoes() {
        if (protocoloRepository.count() > 0) {
            return;
        }

        log.info("🃏 Criando Deck de Rotina...");

        List<Protocolo> seeds = List.of(
            criar("⚔️ Preparar Batalha", "🧴", Atributo.CONSTITUICAO, Dificuldade.EASY, 10),
            criar("🏋️ Templo de Ferro", "💪", Atributo.FORCA, Dificuldade.HARD, 60),
            criar("🧠 Deep Work", "💻", Atributo.INTELECTO, Dificuldade.EPIC, 90),
            criar("🍖 Banquete (Almoço)", "🍗", Atributo.CONSTITUICAO, Dificuldade.MEDIUM, 30),
            criar("🚴 Cardio + Anime", "🚲", Atributo.DESTREZA, Dificuldade.MEDIUM, 45),
            criar("📚 Grimoire (Estudo)", "📖", Atributo.INTELECTO, Dificuldade.HARD, 60),
            criar("🛌 Long Rest", "💤", Atributo.CONSTITUICAO, Dificuldade.EASY, 480)
        );

        protocoloRepository.saveAll(seeds);
        log.info("✅ {} cartas de missão adicionadas ao Grimório!", seeds.size());
    }

    private Protocolo criar(String nome, String icone, Atributo atr, Dificuldade dif, int minutos) {
        Protocolo p = new Protocolo();
        p.setNome(nome);
        p.setIcone(icone);
        p.setAtributo(atr);
        p.setDificuldade(dif);
        p.setDuracaoMinutos(minutos);
        // p.setDescricao("Hábito da Rotina"); 
        return p;
    }
}