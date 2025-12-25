import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { GameService, Usuario, Protocolo } from '../../services/game.service';
import { NgApexchartsModule, ChartComponent, ApexAxisChartSeries, ApexChart, ApexXAxis, ApexStroke, ApexFill, ApexMarkers, ApexYAxis, ApexTheme } from "ng-apexcharts";
import { SidebarComponent } from '../sidebar/sidebar';

export type ChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  xaxis: ApexXAxis;
  stroke: ApexStroke;
  fill: ApexFill;
  markers: ApexMarkers;
  yaxis: ApexYAxis;
  theme: ApexTheme;
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NgApexchartsModule, RouterLink, SidebarComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {
  @ViewChild("chart") chart: ChartComponent | undefined;
  public chartOptions: Partial<ChartOptions> | any;
  
  usuario?: Usuario;
  protocolos: Protocolo[] = [];
  viewMode: 'rotina' | 'status' = 'rotina';

  historico: any[] = [];
  dataSelecionada: string = '';
  totalMinutosHoje: number = 0;

  constructor(private gameService: GameService) {
    this.dataSelecionada = this.getDataHojeLocal();
    this.chartOptions = {
      series: [{ name: 'Seus Atributos', data: [0, 0, 0, 0, 0] }],
      chart: { height: 350, type: 'radar', toolbar: { show: false }, background: 'transparent' },
      xaxis: {
        categories: ['Força', 'Destreza', 'Intelecto', 'Carisma', 'Const'], 
        labels: { style: { colors: Array(5).fill('#9ca3af') } } 
      },
      stroke: { width: 2, colors: ['#3b82f6'] },
      fill: { opacity: 0.2, colors: ['#3b82f6'] },
      markers: { size: 4, colors: ['#3b82f6'] },
      yaxis: { show: false, min: 0 },
      theme: { mode: 'dark' }
    };
  }

  private getDataHojeLocal(): string {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = String(hoje.getMonth() + 1).padStart(2, '0');
    const dia = String(hoje.getDate()).padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
  }

  ngOnInit(): void {
    this.carregarDados();
    this.carregarHistorico();
  }

  carregarDados() {
    this.gameService.getPerfil().subscribe({
      next: (dados) => {
        this.usuario = dados;
        this.atualizarGrafico(dados);
      },
      error: (err) => console.error(err)
    });

    this.gameService.getProtocolos().subscribe(dados => {
      this.protocolos = dados;
    });
  }

  carregarHistorico() {
    this.gameService.getHistorico(this.dataSelecionada).subscribe(logs => {
      this.historico = logs;
      this.recalcularMinutos();
    });
  }

  recalcularMinutos() {
    this.totalMinutosHoje = this.historico.reduce((acc, log) => acc + (log.protocolo.duracaoMinutos || 15), 0);
  }
  
  aoMudarData(event: any) {
    this.dataSelecionada = event.target.value;
    this.carregarHistorico();
  }
  
  atualizarGrafico(u: Usuario) {
    this.chartOptions.series = [{
      name: 'Nível',
      data: [u.forca, u.destreza, u.intelecto, u.carisma, u.constituicao]
    }];
  }

  executar(protocolo: Protocolo) {
    
    // 1. UI
    const novoLog = {
      id: Date.now(),
      protocolo: protocolo,
      dataHora: new Date().toISOString(),
      xpGanho: this.estimarXp(protocolo.dificuldade),
      goldGanho: this.estimarGold(protocolo.dificuldade)
    };

    this.historico = [novoLog, ...this.historico]; 
    this.recalcularMinutos();

    // 2. Chamada ao backend
    this.gameService.fazerCheckin(protocolo.id).subscribe({
      next: (u) => {
        this.usuario = u;
        this.atualizarGrafico(u);
      },
      error: (err) => {
        console.error('Erro ao completar missão:', err);
      }
    });
  }
    // 3. Funções para estimar recompensas (temporárias)
  estimarXp(dificuldade: string): number {
    switch(dificuldade) {
      case 'EASY': return 15;
      case 'MEDIUM': return 30;
      case 'HARD': return 50;
      case 'EPIC': return 100;
      default: return 10;
    }
  }

  estimarGold(dificuldade: string): number {
    switch(dificuldade) {
      case 'EASY': return 5;
      case 'MEDIUM': return 10;
      case 'HARD': return 20;
      case 'EPIC': return 50;
      default: return 5;
    }
  }

  curar() {
    if (this.usuario && this.usuario.mpAtual < 20) {
      alert('Mana insuficiente!');
      return;
    }
    this.gameService.usarCura().subscribe(u => { this.usuario = u; });
  }

  tomarDano() {
    this.gameService.sofrerDano(10).subscribe(u => {
      this.usuario = u;
      if (u.hpAtual === 0) alert('💀 VOCÊ MORREU!');
    });
  }
}