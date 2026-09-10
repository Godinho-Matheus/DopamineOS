import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { GameService, ClasseRPG, Protocolo, Atributo, Dificuldade } from '../../services/game.service';

@Component({
  selector: 'app-setup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './setup.html'
})
export class SetupComponent {
  step = 1;
  loading = false;
  erro = '';

  nome = '';
  classeSelecionada: ClasseRPG | null = null;
  missoesSelecionadas: Protocolo[] = [];

  classes = [
    { 
      id: ClasseRPG.GUERREIRO,
      nome: 'Guerreiro',
      icone: '⚔️',
      desc: 'Força bruta e resistência. Ideal para quem quer focar em exercícios físicos e disciplina.',
      cor: 'border-red-500 shadow-red-500/20'
    },
    { 
      id: ClasseRPG.MAGO,
      nome: 'Mago',
      icone: '🔮',
      desc: 'Intelecto e sabedoria. Ideal para quem foca em estudos, leitura e aprendizado.',
      cor: 'border-blue-500 shadow-blue-500/20'
    },
    { 
      id: ClasseRPG.LADINO,
      nome: 'Ladino',
      icone: '🗡️',
      desc: 'Agilidade e astúcia. Ideal para quem busca flexibilidade, finanças e soft skills.',
      cor: 'border-green-500 shadow-green-500/20'
    }
  ];

  tarefasDisponiveis: Protocolo[] = [
    { nome: 'Beber 2L de Água', icone: '💧', atributo: Atributo.CONSTITUICAO, dificuldade: Dificuldade.EASY, duracaoMinutos: 5 },
    { nome: 'Ler 10 Páginas', icone: '📚', atributo: Atributo.INTELECTO, dificuldade: Dificuldade.MEDIUM, duracaoMinutos: 30 },
    { nome: 'Treino Hipertrofia', icone: '💪', atributo: Atributo.FORCA, dificuldade: Dificuldade.HARD, duracaoMinutos: 60 },
    { nome: 'Caminhada ao Sol', icone: '☀️', atributo: Atributo.CONSTITUICAO, dificuldade: Dificuldade.EASY, duracaoMinutos: 20 },
    { nome: 'Meditação', icone: '🧘', atributo: Atributo.INTELECTO, dificuldade: Dificuldade.EASY, duracaoMinutos: 15 },
    { nome: 'Codar Projeto Pessoal', icone: '💻', atributo: Atributo.INTELECTO, dificuldade: Dificuldade.EPIC, duracaoMinutos: 120 },
    { nome: 'Dormir 8h', icone: '💤', atributo: Atributo.CONSTITUICAO, dificuldade: Dificuldade.EPIC, duracaoMinutos: 480 },
    { nome: 'Arrumar a Casa', icone: '🧹', atributo: Atributo.DESTREZA, dificuldade: Dificuldade.MEDIUM, duracaoMinutos: 45 }
  ];

  constructor(
    private gameService: GameService,
    private router: Router
  ) {}

  selecionarClasse(classe: ClasseRPG) {
    this.classeSelecionada = classe;
  }

  toggleTarefa(missao: Protocolo) {
    const index = this.missoesSelecionadas.indexOf(missao);
    if (index === -1) {
      this.missoesSelecionadas.push(missao);
    } else {
      this.missoesSelecionadas.splice(index, 1);
    }
  }

  estaSelecionada(missao: Protocolo): boolean {
    return this.missoesSelecionadas.includes(missao);
  }

  avancarStep() {
    if (this.step === 1 && this.nome && this.classeSelecionada) {
      this.step = 2;
    }
  }

  voltarStep() {
    this.step = 1;
  }

  finalizarSetup() {
    if (!this.nome || !this.classeSelecionada) return;

    this.loading = true;
    this.erro = '';

    // Envia nome, classe e missões selecionadas num único POST /setup
    this.gameService.setup(this.nome, this.classeSelecionada, this.missoesSelecionadas)
      .subscribe({
        next: () => {
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          console.error(err);
          this.erro = 'Erro ao criar personagem. Tente novamente.';
          this.loading = false;
        }
      });
  }
}