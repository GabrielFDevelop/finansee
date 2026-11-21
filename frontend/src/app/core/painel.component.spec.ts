import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { CategoriaService } from '../core/categoria.service';
import { DespesaService } from '../core/despesa.service';
import { MesService } from '../core/mes.service';
import { OrcamentoResumo, ResumoMensal } from '../core/modelos';
import { ResumoService } from '../core/resumo.service';
import { PainelComponent } from '../painel/painel.component';

registerLocaleData(localePt);

function resumoBase(sobrescrever: Partial<ResumoMensal> = {}): ResumoMensal {
  return {
    ano: 2026,
    mes: 11,
    totalGasto: 3088.9,
    quantidadeDespesas: 38,
    porCategoria: [
      { categoriaId: 1, categoriaNome: 'Moradia', total: 1200, percentual: 38.8 },
    ],
    totalOrcado: 0,
    orcamentos: [],
    ...sobrescrever,
  };
}

function orcamento(gasto: number, limite: number, percentualUsado: number): OrcamentoResumo {
  return { categoriaId: 1, categoriaNome: 'Alimentação', limite, gasto, percentualUsado };
}

describe('PainelComponent', () => {
  let resumoService: jasmine.SpyObj<ResumoService>;
  let despesaService: jasmine.SpyObj<DespesaService>;

  beforeEach(() => {
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date(2026, 10, 15));

    resumoService = jasmine.createSpyObj<ResumoService>('ResumoService', ['buscar']);
    despesaService = jasmine.createSpyObj<DespesaService>('DespesaService', ['listar']);
    despesaService.listar.and.returnValue(of([]));

    const categoriaService = jasmine.createSpyObj<CategoriaService>('CategoriaService', ['listar']);
    categoriaService.listar.and.returnValue(of([]));

    TestBed.configureTestingModule({
      imports: [PainelComponent],
      providers: [
        provideRouter([]),
        { provide: LOCALE_ID, useValue: 'pt-BR' },
        { provide: ResumoService, useValue: resumoService },
        { provide: DespesaService, useValue: despesaService },
        { provide: CategoriaService, useValue: categoriaService },
      ],
    });
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  function abrirPainel(resumo: ResumoMensal): ComponentFixture<PainelComponent> {
    resumoService.buscar.and.returnValue(of(resumo));
    const fixture = TestBed.createComponent(PainelComponent);
    fixture.detectChanges();
    return fixture;
  }

  function html(fixture: ComponentFixture<PainelComponent>): HTMLElement {
    return fixture.nativeElement as HTMLElement;
  }

  it('carrega o resumo e as despesas do mês atual ao abrir', () => {
    abrirPainel(resumoBase());

    expect(resumoService.buscar).toHaveBeenCalledWith(2026, 11);
    expect(despesaService.listar).toHaveBeenCalledWith(2026, 11);
  });

  it('mostra o total formatado em reais e a quantidade de despesas', () => {
    const fixture = abrirPainel(resumoBase());

    expect(html(fixture).querySelector('.total')?.textContent).toContain('3.088,90');
    expect(html(fixture).textContent).toContain('38 despesas');
  });

  it('usa o singular quando há uma única despesa', () => {
    const fixture = abrirPainel(resumoBase({ quantidadeDespesas: 1 }));

    expect(html(fixture).textContent).toContain('1 despesa');
    expect(html(fixture).textContent).not.toContain('1 despesas');
  });

  it('avisa quando o mês não tem despesas', () => {
    const fixture = abrirPainel(resumoBase({ totalGasto: 0, quantidadeDespesas: 0, porCategoria: [] }));

    expect(html(fixture).textContent).toContain('Nenhuma despesa neste mês.');
  });

  it('sem orçamento, convida a definir limites e aponta para a tela de orçamento', () => {
    const fixture = abrirPainel(resumoBase());

    expect(html(fixture).textContent).toContain('Nenhum limite definido para este mês.');
    const link = html(fixture).querySelector('a[href="/orcamento"]');
    expect(link?.textContent).toContain('Definir limites');
  });

  it('dentro do limite: barra proporcional, sem destaque, e mostra quanto resta', () => {
    const fixture = abrirPainel(
      resumoBase({ totalOrcado: 900, orcamentos: [orcamento(300, 900, 33.3)] }),
    );

    const barra = html(fixture).querySelector<HTMLElement>('.barra__preenchimento')!;
    expect(barra.style.width).toBe('33.3%');
    expect(barra.classList.contains('barra__preenchimento--alerta')).toBeFalse();
    expect(barra.classList.contains('barra__preenchimento--estourado')).toBeFalse();
    expect(html(fixture).textContent).toContain('Restam');
    expect(html(fixture).textContent).toContain('600,00');
    expect(html(fixture).textContent).toContain('33,3% usado');
  });

  it('acima de 80% do limite: barra em alerta', () => {
    const fixture = abrirPainel(
      resumoBase({ totalOrcado: 1000, orcamentos: [orcamento(850, 1000, 85)] }),
    );

    const barra = html(fixture).querySelector('.barra__preenchimento')!;
    expect(barra.classList.contains('barra__preenchimento--alerta')).toBeTrue();
  });

  it('limite estourado: barra limitada a 100%, destaque e valor excedido', () => {
    const fixture = abrirPainel(
      resumoBase({ totalOrcado: 1000, orcamentos: [orcamento(1500, 1000, 150)] }),
    );

    const barra = html(fixture).querySelector<HTMLElement>('.barra__preenchimento')!;
    expect(barra.style.width).toBe('100%');
    expect(barra.classList.contains('barra__preenchimento--estourado')).toBeTrue();
    expect(html(fixture).textContent).toContain('Passou');
    expect(html(fixture).textContent).toContain('500,00');
    expect(html(fixture).querySelector('[role="progressbar"]')?.getAttribute('aria-valuenow')).toBe('100');
  });

  it('mostra a mensagem de erro quando a API falha', () => {
    resumoService.buscar.and.returnValue(throwError(() => new Error('falha')));
    const fixture = TestBed.createComponent(PainelComponent);
    fixture.detectChanges();

    const alerta = html(fixture).querySelector('[role="alert"]');
    expect(alerta?.textContent).toContain('Não foi possível carregar os dados');
  });

  it('ao trocar de mês, busca os dados do novo mês', () => {
    abrirPainel(resumoBase());

    TestBed.inject(MesService).mudar(1);

    expect(resumoService.buscar).toHaveBeenCalledWith(2026, 12);
    expect(despesaService.listar).toHaveBeenCalledWith(2026, 12);
  });

  describe('estado da barra de orçamento (valores de fronteira)', () => {
    const casos = [
      { gasto: 790, limite: 1000, percentual: 79, esperado: 'ok' },
      { gasto: 800, limite: 1000, percentual: 80, esperado: 'alerta' },
      { gasto: 1000, limite: 1000, percentual: 100, esperado: 'alerta' },
      { gasto: 1000.4, limite: 1000, percentual: 100, esperado: 'estourado' },
    ];

    casos.forEach(({ gasto, limite, percentual, esperado }) => {
      it(`gasto ${gasto} de ${limite} (${percentual}%) é "${esperado}"`, () => {
        const fixture = abrirPainel(resumoBase());

        const estado = fixture.componentInstance['estado'](orcamento(gasto, limite, percentual));

        expect(estado).toBe(esperado as 'ok' | 'alerta' | 'estourado');
      });
    });
  });
});