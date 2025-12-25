import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { GameService } from '../../services/game.service';

@Component({
  selector: 'app-setup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './setup.html'
})
export class SetupComponent {

  step = 1; // Para controlar os passos (1: Nome, 2: Classe, 3: Tarefas)
  nome: string = '';
  classeSelecionada: string = '';
  
  // Lista de Missões Selecionadas pelo usuário
  selecionadas: any[] = [];

  // Dados das Classes
  classes = [
    { id: 'GUERREIRO', nome: 'Guerreiro', icone: '⚔️', desc: 'Foco em Força e Constituição.', cor: 'border-orange-500 text-orange-500 bg-orange-900/20' },
    { id: 'MAGO', nome: 'Mago', icone: '🔮', desc: 'Foco em Intelecto e Mana.', cor: 'border-blue-500 text-blue-500 bg-blue-900/20' },
    { id: 'LADINO', nome: 'Ladino', icone: '🗡️', desc: 'Foco em Destreza e Carisma.', cor: 'border-green-500 text-green-500 bg-green-900/20' }
  ];

  // --- O CARDÁPIO DE TAREFAS GENÉRICAS ---
  tarefasDisponiveis = [
    { nome: 'Beber Água (2L)', icone: '💧', atributo: 'CONSTITUICAO', dificuldade: 'EASY', duracaoMinutos: 1, selecionada: false },
    { nome: 'Leitura Técnica', icone: '📚', atributo: 'INTELECTO', dificuldade: 'MEDIUM', duracaoMinutos: 30, selecionada: false },
    { nome: 'Treino de Força', icone: '🏋️', atributo: 'FORCA', dificuldade: 'HARD', duracaoMinutos: 60, selecionada: false },
    { nome: 'Cardio / Corrida', icone: '🏃', atributo: 'DESTREZA', dificuldade: 'MEDIUM', duracaoMinutos: 45, selecionada: false },
    { nome: 'Meditação', icone: '🧘', atributo: 'INTELECTO', dificuldade: 'EASY', duracaoMinutos: 15, selecionada: false },
    { nome: 'Networking / Social', icone: '🤝', atributo: 'CARISMA', dificuldade: 'MEDIUM', duracaoMinutos: 30, selecionada: false },
    { nome: 'Dormir 8h', icone: '💤', atributo: 'CONSTITUICAO', dificuldade: 'HARD', duracaoMinutos: 480, selecionada: false },
    { nome: 'Cozinhar Refeição', icone: '🍳', atributo: 'CONSTITUICAO', dificuldade: 'EASY', duracaoMinutos: 40, selecionada: false },
    { nome: 'Estudo Profundo (Deep Work)', icone: '🧠', atributo: 'INTELECTO', dificuldade: 'HARD', duracaoMinutos: 90, selecionada: false },
    { nome: 'Arrumar o Quarto', icone: '🧹', atributo: 'DESTREZA', dificuldade: 'EASY', duracaoMinutos: 20, selecionada: false },
  ];

  constructor(private gameService: GameService, private router: Router) {}

  selecionarClasse(id: string) {
    this.classeSelecionada = id;
    this.step = 2; // Avança visualmente ou libera o próximo passo
  }

  toggleTarefa(tarefa: any) {
    tarefa.selecionada = !tarefa.selecionada;
  }

  confirmar() {
    if (!this.nome || !this.classeSelecionada) return;

    // Filtra apenas as que o usuário marcou como "selecionada"
    const tarefasFinais = this.tarefasDisponiveis
      .filter(t => t.selecionada)
      .map(t => ({
        nome: t.nome,
        icone: t.icone,
        descricao: 'Hábito Inicial',
        atributo: t.atributo,
        dificuldade: t.dificuldade,
        duracaoMinutos: t.duracaoMinutos // Envia o tempo editado pelo usuário
      }));

    this.gameService.setup({ 
      nome: this.nome, 
      classe: this.classeSelecionada,
      tarefasIniciais: tarefasFinais // Envia a lista para o Java
    }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => alert('Erro ao criar personagem.')
    });
  }
}