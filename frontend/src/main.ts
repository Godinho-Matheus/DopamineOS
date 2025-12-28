import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .then(() => console.log('🚀 DopamineOS iniciado com sucesso!'))
  .catch((err) => console.error('❌ Erro fatal ao iniciar:', err));