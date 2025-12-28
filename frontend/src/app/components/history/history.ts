import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { GameService, LogAtividade } from '../../services/game.service';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, SidebarComponent, DatePipe],
  templateUrl: './history.html'
})
export class HistoryComponent implements OnInit {
  logs: LogAtividade[] = [];
  diasAgrupados: { data: string, logs: LogAtividade[], totalXp: number }[] = [];

  constructor(private gameService: GameService) {}

  ngOnInit() {
    this.gameService.getHistoricoGeral().subscribe(dados => {
      this.logs = dados;
      this.agruparPorDia();
    });
  }

  agruparPorDia() {
    const grupos = new Map<string, LogAtividade[]>();

    this.logs.forEach(log => {
      const data = new Date(log.dataHora).toLocaleDateString();
      if (!grupos.has(data)) grupos.set(data, []);
      grupos.get(data)?.push(log);
    });

    this.diasAgrupados = [];
    grupos.forEach((logsDoDia, data) => {
      const totalXp = logsDoDia.reduce((acc, curr) => acc + curr.xpGanho, 0);
      this.diasAgrupados.push({ data, logs: logsDoDia, totalXp });
    });
  }
}