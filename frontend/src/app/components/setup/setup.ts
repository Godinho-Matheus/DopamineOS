import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { switchMap, tap, catchError } from 'rxjs/operators';

import { GameService, ClasseRPG, Atributo, Dificuldade, Protocolo } from '../../services/game.service';

interface TarefaSelecionavel extends Protocolo {
  selecionada: boolean;
}

@Component({
  selector: 'app-setup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './setup.html',
  styleUrls: ['./setup.css']
})
export class SetupComponent {

  step = 1;
  nome: string = '';
  loading = false;
  
  EClasse = ClasseRPG;

  classeSelecionada: ClasseRPG | null = null;

  // Dados estáticos visuais
  classes = [
    {
      id: ClasseRPG.GUERREIRO,
      nome: 'Guerreiro',
      icone: '⚔️',
      desc: 'Mestre da Força e Constituição.',
      cor: 'border-orange-500 text-orange-500 bg-orange-900/20'
    },
    {
      id: ClasseRPG.MAGO,
      nome: 'Mago',
      icone: '🔮',
      desc: 'Sábio do Intelecto e Mana.',
      cor: 'border-blue-500 text-blue-500 bg-blue-900/20'
    },
    {
      id: ClasseRPG.LADINO,
      nome: 'Ladino',
      icone: '🗡️',
      desc: 'Ágil em Destreza e Carisma.',
      cor: 'border-green-500 text-green-500 bg-green-900/20'
    }
  ];

  // Lista de sugestões iniciais
  tarefasDisponiveis: TarefaSelecionavel[] = [
    { nome: 'Beber Água (2L)', icone: '💧', atributo: Atributo.CONSTITUICAO, dificuldade: Dificuldade.EASY, duracaoMinutos: 1, selecionada: false },
    { nome: 'Leitura Técnica', icone: '📚', atributo: Atributo.INTELECTO, dificuldade: Dificuldade.MEDIUM, duracaoMinutos: 30, selecionada: false },
    { nome: 'Treino de Força', icone: '🏋️', atributo: Atributo.FORCA, dificuldade: Dificuldade.HARD, duracaoMinutos: 60, selecionada: false },
    { nome: 'Corrida', icone: '🏃', atributo: Atributo.DESTREZA, dificuldade: Dificuldade.MEDIUM, duracaoMinutos: 45, selecionada: false },
    { nome: 'Meditação', icone: '🧘', atributo: Atributo.INTELECTO, dificuldade: Dificuldade.EASY, duracaoMinutos: 15, selecionada: false },
    { nome: 'Networking', icone: '🤝', atributo: Atributo.CARISMA, dificuldade: Dificuldade.MEDIUM, duracaoMinutos: 30, selecionada: false },
    { nome: 'Dormir 8h', icone: '💤', atributo: Atributo.CONSTITUICAO, dificuldade: Dificuldade.HARD, duracaoMinutos: 480, selecionada: false },
    { nome: 'Cozinhar', icone: '🍳', atributo: Atributo.DESTREZA, dificuldade: Dificuldade.EASY, duracaoMinutos: 40, selecionada: false },
    { nome: 'Deep Work', icone: '🧠', atributo: Atributo.INTELECTO, dificuldade: Dificuldade.HARD, duracaoMinutos: 90, selecionada: false },
    { nome: 'Limpeza', icone: '🧹', atributo: Atributo.CONSTITUICAO, dificuldade: Dificuldade.EASY, duracaoMinutos: 20, selecionada: false },
  ];

  constructor(private gameService: GameService, private router: Router) {}

  avancarStep() {
    if (this.step === 1 && !this.nome) return;
    if (this.step === 2 && !this.classeSelecionada) return;
    this.step++;
  }

  voltarStep() {
    if (this.step > 1) this.step--;
  }

  selecionarClasse(classe: ClasseRPG) {
    this.classeSelecionada = classe;
  }

  toggleTarefa(tarefa: TarefaSelecionavel) {
    tarefa.selecionada = !tarefa.selecionada;
  }

  finalizarSetup() {
    if (!this.nome || !this.classeSelecionada) return;
    
    this.loading = true;

    // Cria o Personagem
    this.gameService.setup(this.nome, this.classeSelecionada)
      .pipe(
        switchMap((usuarioCriado) => {
          console.log('✅ Personagem criado:', usuarioCriado);

          const tarefasParaSalvar = this.tarefasDisponiveis.filter(t => t.selecionada);

          if (tarefasParaSalvar.length === 0) {
            return of([]); // Nenhuma tarefa para salvar
          }

          const requisicoes = tarefasParaSalvar.map(tarefa => {
            const { selecionada, ...protocoloLimpo } = tarefa;
            return this.gameService.criarProtocolo(protocoloLimpo);
          });

          return forkJoin(requisicoes);
        }),
        tap(() => this.loading = false),
        catchError(err => {
          this.loading = false;
          console.error('❌ Erro no setup:', err);
          alert('Erro ao configurar: ' + (err.error?.message || 'Erro desconhecido'));
          return of(null); // Retorna nulo para não quebrar a chain
        })
      )
      .subscribe((resultado) => {
        if (resultado !== null) {
          console.log('✅ Missões criadas:', resultado);
           this.router.navigate(['/dashboard']); // Redireciona para o jogo
        }
      });
  }
}