import { CurrencyPipe, AsyncPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  BehaviorSubject, EMPTY, catchError, combineLatest, forkJoin, switchMap, tap,
} from 'rxjs';

import { CategoriaService } from '../core/categoria.service';
import { MesService } from '../core/mes.service';
import { Categoria, Orcamento } from '../core/modelos';
import { OrcamentoService } from '../core/orcamento.service';
import { FormOrcamentoComponent } from './form-orcamento/form-orcamento.component';

interface LinhaOrcamento {
  categoria: Categoria;
  limite: number | null;
}

@Component({
  selector: 'app-orcamento',
  imports: [AsyncPipe, CurrencyPipe, FormOrcamentoComponent],
  templateUrl: './orcamento.component.html',
  styleUrl: './orcamento.component.scss',
})
export class OrcamentoComponent {
  protected readonly mes = inject(MesService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly orcamentoService = inject(OrcamentoService);
  private readonly recarregar$ = new BehaviorSubject<void>(undefined);

  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly orcamentos = signal<Orcamento[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  protected readonly linhas = computed<LinhaOrcamento[]>(() => {
    const limites = new Map<number, number>(
      this.orcamentos().map((o) => [o.categoriaId, o.valorLimite] as [number, number]),
    );
    return this.categorias().map((categoria) => ({
      categoria,
      limite: limites.get(categoria.id) ?? null,
    }));
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
            categorias: this.categoriaService.listar(),
            orcamentos: this.orcamentoService.listar(ano, mes),
          }).pipe(
            catchError(() => {
              this.erro.set('Não foi possível carregar o orçamento. Verifique se a API está no ar.');
              this.carregando.set(false);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe(({ categorias, orcamentos }) => {
        this.categorias.set(categorias);
        this.orcamentos.set(orcamentos);
        this.carregando.set(false);
      });
  }

  protected recarregar(): void {
    this.recarregar$.next();
  }

  protected remover(linha: LinhaOrcamento): void {
    if (!window.confirm(`Remover o limite de "${linha.categoria.nome}" neste mês?`)) {
      return;
    }
    const periodo = this.mes.atual;
    this.orcamentoService.remover(periodo.ano, periodo.mes, linha.categoria.id).subscribe({
      next: () => this.recarregar(),
      error: (e: HttpErrorResponse) =>
        this.erro.set(e.error?.detail ?? 'Não foi possível remover o limite.'),
    });
  }
}