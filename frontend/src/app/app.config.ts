import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import localePt from '@angular/common/locales/pt';
import { ApplicationConfig, LOCALE_ID } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';

registerLocaleData(localePt); // dados de formatação do português do Brasil

export const appConfig: ApplicationConfig = {
  providers: [
    // ...os providers que já estavam aqui (provideRouter etc.) continuam
    provideRouter(routes),
    provideHttpClient(),                           // habilita chamadas HTTP
    { provide: LOCALE_ID, useValue: 'pt-BR' },     // moeda e datas em português
  ],
};