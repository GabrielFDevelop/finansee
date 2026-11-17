import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BehaviorSubject, EMPTY, catchError, switchMap, tap } from 'rxjs';

import { CategoriaService } from '../core/categoria.service';
import { Categoria } from '../core/modelos';
import { FormCategoriaComponent } from './form-categoria/form-categoria.component';

@Component({
  selector: 'app-categorias',
  imports: [FormCategoriaComponent],
  templateUrl: './categorias.component.html',
  styleUrl: './categorias.component.scss',
})
export class CategoriasComponent {
  private readonly categoriaService = inject(CategoriaService);
  private readonly recarregar$ = new BehaviorSubject<void>(undefined);

  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    this.recarregar$
      .pipe(
        tap(() => {
          this.carregando.set(true);
          this.erro.set(null);
        }),
        switchMap(() =>
          this.categoriaService.listar().pipe(
            catchError(() => {
              this.erro.set('Não foi possível carregar as categorias. Verifique se a API está no ar.');
              this.carregando.set(false);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((categorias) => {
        this.categorias.set(categorias);
        this.carregando.set(false);
      });
  }

  protected recarregar(): void {
    this.recarregar$.next();
  }

  protected excluir(categoria: Categoria): void {
    if (!window.confirm(`Excluir a categoria "${categoria.nome}"?`)) {
      return;
    }
    this.categoriaService.excluir(categoria.id).subscribe({
      next: () => this.recarregar(),
      error: (e: HttpErrorResponse) =>
        this.erro.set(e.error?.detail ?? 'Não foi possível excluir a categoria.'),
    });
  }
}