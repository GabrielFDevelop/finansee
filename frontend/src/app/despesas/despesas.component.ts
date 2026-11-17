import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BehaviorSubject, EMPTY, catchError, combineLatest, switchMap, tap } from 'rxjs';

import { FormDespesaComponent } from '../compartilhado/form-despesa/form-despesa.component';
import { DespesaService } from '../core/despesa.service';
import { MesService } from '../core/mes.service';
import { Despesa } from '../core/modelos';

@Component({
  selector: 'app-despesas',
  imports: [CurrencyPipe, DatePipe, FormDespesaComponent],
  templateUrl: './despesas.component.html',
  styleUrl: './despesas.component.scss',
})
export class DespesasComponent {
  private readonly mes = inject(MesService);
  private readonly despesaService = inject(DespesaService);
  private readonly recarregar$ = new BehaviorSubject<void>(undefined);

  protected readonly despesas = signal<Despesa[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  protected readonly total = computed(() =>
    this.despesas().reduce((soma, despesa) => soma + despesa.valor, 0),
  );

  constructor() {
    combineLatest([this.mes.periodo$, this.recarregar$])
      .pipe(
        tap(() => {
          this.carregando.set(true);
          this.erro.set(null);
        }),
        switchMap(([{ ano, mes }]) =>
          this.despesaService.listar(ano, mes).pipe(
            catchError(() => {
              this.erro.set('Não foi possível carregar as despesas. Verifique se a API está no ar.');
              this.carregando.set(false);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((despesas) => {
        this.despesas.set(despesas);
        this.carregando.set(false);
      });
  }

  protected recarregar(): void {
    this.recarregar$.next();
  }

  protected excluir(despesa: Despesa): void {
    if (!window.confirm(`Excluir a despesa "${despesa.descricao}"?`)) {
      return;
    }
    this.despesaService.excluir(despesa.id).subscribe({
      next: () => this.recarregar(),
      error: () => this.erro.set('Não foi possível excluir a despesa.'),
    });
  }
}