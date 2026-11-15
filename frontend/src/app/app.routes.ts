import { Routes } from '@angular/router';

import { PainelComponent } from './painel/painel.component';

export const routes: Routes = [
  { path: '', component: PainelComponent },
  { path: '**', redirectTo: '' },
];