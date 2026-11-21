import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { CategoriaService } from '../../core/categoria.service';
import { DespesaService } from '../../core/despesa.service';
import { Categoria, Despesa } from '../../core/modelos';
import { FormDespesaComponent } from './form-despesa.component';

describe('FormDespesaComponent', () => {
  let fixture: ComponentFixture<FormDespesaComponent>;
  let componente: FormDespesaComponent;
  let despesaService: jasmine.SpyObj<DespesaService>;

  const categorias: Categoria[] = [
    { id: 1, nome: 'Alimentação' },
    { id: 2, nome: 'Transporte' },
  ];

  const despesaExistente: Despesa = {
    id: 7,
    descricao: 'Almoço',
    valor: 32.5,
    data: '2026-11-10',
    categoriaId: 1,
    categoriaNome: 'Alimentação',
  };

  beforeEach(() => {
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date(2026, 10, 15, 23, 30));

    despesaService = jasmine.createSpyObj<DespesaService>('DespesaService', ['criar', 'atualizar']);
    const categoriaService = jasmine.createSpyObj<CategoriaService>('CategoriaService', ['listar']);
    categoriaService.listar.and.returnValue(of(categorias));

    TestBed.configureTestingModule({
      imports: [FormDespesaComponent],
      providers: [
        { provide: DespesaService, useValue: despesaService },
        { provide: CategoriaService, useValue: categoriaService },
      ],
    });

    fixture = TestBed.createComponent(FormDespesaComponent);
    componente = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  function html(): HTMLElement {
    return fixture.nativeElement as HTMLElement;
  }

  function enviar(): void {
    html().querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    fixture.detectChanges();
  }

  function preencherValido(): void {
    componente['form'].patchValue({ descricao: '  Almoço  ', valor: 32.5, categoriaId: 1 });
  }

  it('lista as categorias da API no select', () => {
    const opcoes = Array.from(html().querySelectorAll('select option')).map((o) => o.textContent?.trim());

    expect(opcoes).toEqual(['Selecione…', 'Alimentação', 'Transporte']);
  });

  it('abre com a data de hoje e o campo de categoria sem seleção', () => {
    componente.abrir();

    expect(componente['form'].value.data).toBe('2026-11-15');
    expect(componente['form'].value.categoriaId).toBeNull();
  });

  it('não envia e mostra os erros quando os campos obrigatórios estão vazios', () => {
    componente.abrir();

    enviar();

    expect(despesaService.criar).not.toHaveBeenCalled();
    expect(html().textContent).toContain('Informe uma descrição.');
    expect(html().textContent).toContain('Informe um valor maior que zero.');
    expect(html().textContent).toContain('Escolha uma categoria.');
  });

  it('envia os dados sem espaços nas pontas e avisa o painel para recarregar', () => {
    despesaService.criar.and.returnValue(of(despesaExistente));
    const aviso = spyOn(componente.salva, 'emit');
    componente.abrir();
    preencherValido();

    enviar();

    expect(despesaService.criar).toHaveBeenCalledOnceWith({
      descricao: 'Almoço',
      valor: 32.5,
      data: '2026-11-15',
      categoriaId: 1,
    });
    expect(aviso).toHaveBeenCalled();
  });

  it('em modo edição, preenche os campos e salva com atualizar usando o id', () => {
    despesaService.atualizar.and.returnValue(of(despesaExistente));
    componente.abrir(despesaExistente);
    fixture.detectChanges();

    expect(html().querySelector('h2')?.textContent).toContain('Editar despesa');
    expect(html().querySelector<HTMLInputElement>('#descricao')!.value).toBe('Almoço');

    enviar();

    expect(despesaService.atualizar).toHaveBeenCalledOnceWith(7, {
      descricao: 'Almoço',
      valor: 32.5,
      data: '2026-11-10',
      categoriaId: 1,
    });
    expect(despesaService.criar).not.toHaveBeenCalled();
  });

  it('mostra a mensagem devolvida pela API quando o salvamento falha, sem avisar o painel', () => {
    despesaService.criar.and.returnValue(
      throwError(() => new HttpErrorResponse({
        status: 404,
        error: { detail: 'Categoria não encontrada: 1' },
      })),
    );
    const aviso = spyOn(componente.salva, 'emit');
    componente.abrir();
    preencherValido();

    enviar();

    expect(html().querySelector('[role="alert"]')?.textContent).toContain('Categoria não encontrada: 1');
    expect(aviso).not.toHaveBeenCalled();
  });

  it('o botão Cancelar fecha a janela sem salvar', () => {
    const janela = html().querySelector('dialog') as HTMLDialogElement;
    componente.abrir();
    expect(janela.open).toBeTrue();

    html().querySelector<HTMLButtonElement>('.botao-secundario')!.click();

    expect(janela.open).toBeFalse();
    expect(despesaService.criar).not.toHaveBeenCalled();
  });
});