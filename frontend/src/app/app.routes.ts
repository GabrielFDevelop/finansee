import { Routes } from '@angular/router';

import { PainelComponent } from './painel/painel.component';
import { DespesasComponent } from './despesas/despesas.component';
import { CategoriasComponent } from './categorias/categorias.component';
import { OrcamentoComponent } from './orcamento/orcamento.component';

export const routes: Routes = [
  { path: '', component: PainelComponent },
  { path: 'despesas', component: DespesasComponent },
  { path: 'categorias', component: CategoriasComponent },
  { path: 'orcamento', component: OrcamentoComponent },
  { path: '**', redirectTo: '' },
];