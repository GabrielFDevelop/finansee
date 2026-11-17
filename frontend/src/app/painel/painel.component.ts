import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  BehaviorSubject, EMPTY, catchError, combineLatest, forkJoin, switchMap, tap,
} from 'rxjs';

import { DespesaService } from '../core/despesa.service';
import { MesService } from '../core/mes.service';
import { Despesa, OrcamentoResumo, ResumoMensal } from '../core/modelos';
import { ResumoService } from '../core/resumo.service';
import { FormDespesaComponent } from '../compartilhado/form-despesa/form-despesa.component';

@Component({
  selector: 'app-painel',
  imports: [CurrencyPipe, DatePipe, DecimalPipe, RouterLink, FormDespesaComponent],
  templateUrl: './painel.component.html',
  styleUrl: './painel.component.scss',
})
export class PainelComponent {
  private readonly mes = inject(MesService);
  private readonly resumoService = inject(ResumoService);
  private readonly despesaService = inject(DespesaService);
  private readonly recarregar$ = new BehaviorSubject<void>(undefined);

  protected readonly resumo = signal<ResumoMensal | null>(null);
  protected readonly despesas = signal<Despesa[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  protected readonly ultimas = computed(() => this.despesas().slice(0, 5));
  protected readonly gastoOrcado = computed(() =>
    (this.resumo()?.orcamentos ?? []).reduce((soma, o) => soma + o.gasto, 0),
  );
  protected readonly gradiente = computed(() => {
    const categorias = this.resumo()?.porCategoria ?? [];
    if (categorias.length === 0) {
      return 'var(--cor-borda)';
    }
    let acumulado = 0;
    const trechos = categorias.map((c, i) => {
      acumulado = i === categorias.length - 1 ? 100 : acumulado + c.percentual;
      return `${this.cor(i)} 0 ${acumulado}%`;
    });
    return `conic-gradient(${trechos.join(', ')})`;
  });

  constructor() {
    combineLatest([this.mes.periodo$, this.recarregar$])
      .pipe(
        tap(() => {
          this.carregando.set(true);
          this.erro.set(null);
        }),
        switchMap(([{ ano, mes }]) =>
          forkJoin({
            resumo: this.resumoService.buscar(ano, mes),
            despesas: this.despesaService.listar(ano, mes),
          }).pipe(
            catchError(() => {
              this.erro.set('Não foi possível carregar os dados. Verifique se a API está no ar.');
              this.carregando.set(false);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe(({ resumo, despesas }) => {
        this.resumo.set(resumo);
        this.despesas.set(despesas);
        this.carregando.set(false);
      });
  }

  protected recarregar(): void {
    this.recarregar$.next();
  }

  protected cor(indice: number): string {
    return `var(--cat-${(indice % 7) + 1})`;
  }

  protected estado(orcamento: OrcamentoResumo): 'ok' | 'alerta' | 'estourado' {
    if (orcamento.gasto > orcamento.limite) {
      return 'estourado';
    }
    return orcamento.percentualUsado >= 80 ? 'alerta' : 'ok';
  }

  protected largura(orcamento: OrcamentoResumo): number {
    return Math.min(orcamento.percentualUsado, 100);
  }
}