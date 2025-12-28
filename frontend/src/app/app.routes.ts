import { Routes } from '@angular/router';
import { HistoryComponent } from './components/history/history';

export const routes: Routes = [
    {
    path: '',
    redirectTo: 'setup',
    pathMatch: 'full'
    },

    {
    path: 'setup',
    loadComponent: () => import('./components/setup/setup').then(m => m.SetupComponent),
    title: 'DopamineOS - Setup'
    },

    {
    path: 'dashboard',
    loadComponent: () => import('./components/dashboard/dashboard').then(m => m.DashboardComponent),
    title: 'DopamineOS - Visão Geral'
    },

    {
    path: 'game/quest-editor',
    loadComponent: () => import('./components/quest-editor/quest-editor').then(m => m.QuestEditorComponent),
    title: 'DopamineOS - Grimório'
    },

    {
    path: '**',
    redirectTo: 'dashboard'
    },

    {
    path: 'history',
    loadComponent: () => import('./components/history/history').then(m => m.HistoryComponent),
    title: 'DopamineOS - Histórico'
}
];