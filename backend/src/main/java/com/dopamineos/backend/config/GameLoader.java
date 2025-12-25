package com.dopamineos.backend.config;

import com.dopamineos.backend.entity.*;
import com.dopamineos.backend.entity.enums.*;
import com.dopamineos.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameLoader {

    @Bean
    CommandLineRunner initGame(UsuarioRepository userRepo, ProtocoloRepository protoRepo) {
        return args -> {
            // 1. Cria o Player se não existir
            if (userRepo.count() == 0) {
                Usuario u = new Usuario();
                u.setNome("Alex, o Paladino");
                u.setClasse(ClasseRPG.GUERREIRO);
                u.setNivel(1);
                u.setXpAtual(0);
                u.setMoedas(0);
                // Stats iniciais
                u.setForca(10);
                u.setDestreza(5);
                u.setIntelecto(10);
                u.setCarisma(5);
                u.setConstituicao(10);
                userRepo.save(u);
                System.out.println("🎮 Player criado!");

                // 2. Cria os Botões de Rotina
                criarProtocolo(protoRepo, "⚔️ Preparar Batalha", "🧴", Atributo.CONSTITUICAO, Dificuldade.EASY);
                criarProtocolo(protoRepo, "🏋️ Templo de Ferro", "💪", Atributo.FORCA, Dificuldade.HARD);
                criarProtocolo(protoRepo, "🧠 Deep Work", "💻", Atributo.INTELECTO, Dificuldade.EPIC);
                criarProtocolo(protoRepo, "🍖 Banquete (Almoço)", "🍗", Atributo.CONSTITUICAO, Dificuldade.MEDIUM);
                criarProtocolo(protoRepo, "🚴 Cardio + Anime", "🚲", Atributo.DESTREZA, Dificuldade.MEDIUM);
                criarProtocolo(protoRepo, "📚 Grimoire (Estudo)", "📖", Atributo.INTELECTO, Dificuldade.HARD);
                criarProtocolo(protoRepo, "🛌 Long Rest", "💤", Atributo.CONSTITUICAO, Dificuldade.EASY);

                System.out.println("🃏 Deck de Rotina criado!");
            }
            // Se não houver protocolos (por exemplo: foram apagados em setup), garante seeds
            if (protoRepo.count() == 0) {
                criarProtocolo(protoRepo, "⚔️ Preparar Batalha", "🧴", Atributo.CONSTITUICAO, Dificuldade.EASY);
                criarProtocolo(protoRepo, "🏋️ Templo de Ferro", "💪", Atributo.FORCA, Dificuldade.HARD);
                criarProtocolo(protoRepo, "🧠 Deep Work", "💻", Atributo.INTELECTO, Dificuldade.EPIC);
                criarProtocolo(protoRepo, "🍖 Banquete (Almoço)", "🍗", Atributo.CONSTITUICAO, Dificuldade.MEDIUM);
                criarProtocolo(protoRepo, "🚴 Cardio + Anime", "🚲", Atributo.DESTREZA, Dificuldade.MEDIUM);
                criarProtocolo(protoRepo, "📚 Grimoire (Estudo)", "📖", Atributo.INTELECTO, Dificuldade.HARD);
                criarProtocolo(protoRepo, "🛌 Long Rest", "💤", Atributo.CONSTITUICAO, Dificuldade.EASY);
                System.out.println("🃏 Deck de Rotina criado (verificação adicional)!");
            }
        };
    }

    private void criarProtocolo(ProtocoloRepository repo, String nome, String icone, Atributo atr, Dificuldade dif) {
        Protocolo p = new Protocolo();
        p.setNome(nome);
        p.setIcone(icone);
        p.setAtributo(atr);
        p.setDificuldade(dif);
        repo.save(p);
    }
}