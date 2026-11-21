import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Orcamento } from './modelos';
import { OrcamentoService } from './orcamento.service';

describe('OrcamentoService', () => {
  let servico: OrcamentoService;
  let http: HttpTestingController;

  const orcamento: Orcamento = {
    categoriaId: 1,
    categoriaNome: 'Alimentação',
    ano: 2026,
    mes: 11,
    valorLimite: 900,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servico = TestBed.inject(OrcamentoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('listar faz GET em /api/orcamentos com ano e mês', () => {
    let resultado: Orcamento[] | undefined;

    servico.listar(2026, 11).subscribe((lista) => (resultado = lista));

    const requisicao = http.expectOne((r) => r.url === '/api/orcamentos');
    expect(requisicao.request.method).toBe('GET');
    expect(requisicao.request.params.get('ano')).toBe('2026');
    expect(requisicao.request.params.get('mes')).toBe('11');
    requisicao.flush([orcamento]);

    expect(resultado).toEqual([orcamento]);
  });

  it('definir faz PUT em /api/orcamentos/{ano}/{mes}/{categoria} com o limite no corpo', () => {
    let resultado: Orcamento | undefined;

    servico.definir(2026, 11, 1, 900).subscribe((definido) => (resultado = definido));

    const requisicao = http.expectOne('/api/orcamentos/2026/11/1');
    expect(requisicao.request.method).toBe('PUT');
    expect(requisicao.request.body).toEqual({ valorLimite: 900 });
    requisicao.flush(orcamento);

    expect(resultado).toEqual(orcamento);
  });

  it('remover faz DELETE em /api/orcamentos/{ano}/{mes}/{categoria}', () => {
    servico.remover(2026, 11, 1).subscribe();

    const requisicao = http.expectOne('/api/orcamentos/2026/11/1');
    expect(requisicao.request.method).toBe('DELETE');
    requisicao.flush(null);
  });
});