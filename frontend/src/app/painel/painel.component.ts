import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { DespesaService } from '../core/despesa.service';
import { Despesa, ResumoMensal } from '../core/modelos';
import { ResumoService } from '../core/resumo.service';

@Component({
  selector: 'app-painel',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './painel.component.html',
  styleUrl: './painel.component.scss',
})
export class PainelComponent implements OnInit {
  private readonly resumoService = inject(ResumoService);
  private readonly despesaService = inject(DespesaService);

  private readonly hoje = new Date();
  protected readonly ano = signal(this.hoje.getFullYear());
  protected readonly mes = signal(this.hoje.getMonth() + 1);

  protected readonly resumo = signal<ResumoMensal | null>(null);
  protected readonly despesas = signal<Despesa[]>([]);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly rotuloMes = computed(() => {
    const data = new Date(this.ano(), this.mes() - 1, 1);
    const texto = data.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
    return texto.charAt(0).toUpperCase() + texto.slice(1);
  });

  protected readonly ultimas = computed(() => this.despesas().slice(0, 5));

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

  ngOnInit(): void {
    this.carregar();
  }

  protected mudarMes(delta: number): void {
    const data = new Date(this.ano(), this.mes() - 1 + delta, 1);
    this.ano.set(data.getFullYear());
    this.mes.set(data.getMonth() + 1);
    this.carregar();
  }

  protected cor(indice: number): string {
    return `var(--cat-${(indice % 7) + 1})`;
  }

  private carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);

    forkJoin({
      resumo: this.resumoService.buscar(this.ano(), this.mes()),
      despesas: this.despesaService.listar(this.ano(), this.mes()),
    }).subscribe({
      next: ({ resumo, despesas }) => {
        this.resumo.set(resumo);
        this.despesas.set(despesas);
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar os dados. Verifique se a API está no ar.');
        this.carregando.set(false);
      },
    });
  }
}