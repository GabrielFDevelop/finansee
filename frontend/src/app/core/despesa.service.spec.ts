import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { DespesaService } from './despesa.service';
import { Despesa, DespesaRequest } from './modelos';

describe('DespesaService', () => {
  let servico: DespesaService;
  let http: HttpTestingController;

  const despesa: Despesa = {
    id: 7,
    descricao: 'Almoço',
    valor: 32.5,
    data: '2026-11-15',
    categoriaId: 1,
    categoriaNome: 'Alimentação',
  };

  const dados: DespesaRequest = {
    descricao: 'Almoço',
    valor: 32.5,
    data: '2026-11-15',
    categoriaId: 1,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servico = TestBed.inject(DespesaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('listar faz GET em /api/despesas com ano e mês e devolve a lista', () => {
    let resultado: Despesa[] | undefined;

    servico.listar(2026, 11).subscribe((lista) => (resultado = lista));

    const requisicao = http.expectOne((r) => r.url === '/api/despesas');
    expect(requisicao.request.method).toBe('GET');
    expect(requisicao.request.params.get('ano')).toBe('2026');
    expect(requisicao.request.params.get('mes')).toBe('11');
    requisicao.flush([despesa]);

    expect(resultado).toEqual([despesa]);
  });

  it('criar faz POST em /api/despesas com os dados no corpo', () => {
    let resultado: Despesa | undefined;

    servico.criar(dados).subscribe((criada) => (resultado = criada));

    const requisicao = http.expectOne('/api/despesas');
    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toEqual(dados);
    requisicao.flush(despesa);

    expect(resultado).toEqual(despesa);
  });

  it('atualizar faz PUT em /api/despesas/{id} com os dados no corpo', () => {
    servico.atualizar(7, dados).subscribe();

    const requisicao = http.expectOne('/api/despesas/7');
    expect(requisicao.request.method).toBe('PUT');
    expect(requisicao.request.body).toEqual(dados);
    requisicao.flush(despesa);
  });

  it('excluir faz DELETE em /api/despesas/{id} e completa', () => {
    let concluiu = false;

    servico.excluir(7).subscribe({ complete: () => (concluiu = true) });

    const requisicao = http.expectOne('/api/despesas/7');
    expect(requisicao.request.method).toBe('DELETE');
    requisicao.flush(null);

    expect(concluiu).toBeTrue();
  });
});