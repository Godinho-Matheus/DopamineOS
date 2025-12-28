import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NgApexchartsModule, ChartComponent, ApexAxisChartSeries, ApexChart, ApexXAxis, ApexStroke, ApexFill, ApexMarkers, ApexYAxis, ApexTheme } from "ng-apexcharts";
import { SidebarComponent } from '../sidebar/sidebar';

import { GameService, Usuario, Protocolo, LogAtividade } from '../../services/game.service';

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
  historico: LogAtividade[] = [];
  
  viewMode: 'rotina' | 'status' = 'rotina';
  dataSelecionada: string = '';
  totalMinutosHoje: number = 0;
  loadingCheckin = false;

  constructor(private gameService: GameService) {
    this.dataSelecionada = new Date().toISOString().split('T')[0]; // Pega YYYY-MM-DD
    this.inicializarGrafico();
  }

  ngOnInit(): void {
    this.carregarDadosIniciais();
  }

  // Inicialização limpa do gráfico
  private inicializarGrafico() {
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
      yaxis: { show: false, min: 0, max: 20 },
      theme: { mode: 'dark' }
    };
  }

  carregarDadosIniciais() {
    // 1. Carrega Perfil
    this.gameService.getPerfil().subscribe({
      next: (dados) => {
        this.usuario = dados;
        this.atualizarGrafico(dados);
      },
      error: (err) => console.error('Erro ao carregar perfil', err)
    });

    // 2. Carrega Missões Disponíveis
    this.gameService.getProtocolos().subscribe({
      next: (dados) => this.protocolos = dados,
      error: (err) => console.error('Erro ao carregar protocolos', err)
    });

    // 3. Carrega Histórico do Dia (implementar no backend)
    // this.carregarHistorico();
  }

  carregarHistorico() {
    // Exemplo de implementação futura:
    /*
    this.gameService.getHistorico(this.dataSelecionada).subscribe(logs => {
      this.historico = logs;
      this.recalcularMinutos();
    });
    */
  }

  atualizarGrafico(u: Usuario) {
    this.chartOptions.series = [{
      name: 'Nível Atual',
      data: [u.forca, u.destreza, u.intelecto, u.carisma, u.constituicao]
    }];
    const maiorAtributo = Math.max(u.forca, u.destreza, u.intelecto, u.carisma, u.constituicao);
    this.chartOptions.yaxis = { show: false, min: 0, max: maiorAtributo + 5 };
  }

  executar(protocolo: Protocolo) {
    if (!protocolo.id || this.loadingCheckin) return;
    this.loadingCheckin = true;
    this.gameService.fazerCheckin(protocolo.id).subscribe({
      next: (usuarioAtualizado) => {
        this.usuario = usuarioAtualizado;
        this.atualizarGrafico(usuarioAtualizado);
        this.adicionarLogLocal(protocolo);
        this.loadingCheckin = false;
        console.log(`Checkin realizado! XP Total: ${usuarioAtualizado.xpAtual}`);
      },
      error: (err) => {
        this.loadingCheckin = false;
        console.error('Falha no checkin:', err);
        alert('Erro ao completar missão: ' + (err.error?.message || 'Erro de conexão'));
      }
    });
  }
  private adicionarLogLocal(protocolo: Protocolo) {
    const novoLog: LogAtividade = {
      id: Date.now(),
      protocolo: protocolo,
      dataHora: new Date().toISOString(),
      xpGanho: 0,
      goldGanho: 0
    };
    
    this.historico = [novoLog, ...this.historico];
    this.totalMinutosHoje += (protocolo.duracaoMinutos || 15);
  }

  get porcentagemXP(): number {
    if (!this.usuario) return 0;
    return Math.min((this.usuario.xpAtual / this.usuario.xpParaProximoNivel) * 100, 100);
  }

  get porcentagemHP(): number {
    if (!this.usuario) return 0;
    return (this.usuario.hpAtual / this.usuario.maxHp) * 100;
  }
  
  get porcentagemMP(): number {
    if (!this.usuario) return 0;
    return (this.usuario.mpAtual / this.usuario.maxMp) * 100;
  }
}