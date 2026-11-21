/// <reference types="jasmine" />

import { MesService, Periodo } from './mes.service';

describe('MesService', () => {
  beforeEach(() => {
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  function criarEm(ano: number, mesIndice: number): MesService {
    jasmine.clock().mockDate(new Date(ano, mesIndice, 15));
    return new MesService();
  }

  it('começa no mês atual', () => {
    const servico = criarEm(2026, 10); // novembro (os meses do Date começam em 0)

    expect(servico.atual).toEqual({ ano: 2026, mes: 11 });
  });

  it('mudar(1) avança um mês', () => {
    const servico = criarEm(2026, 10);

    servico.mudar(1);

    expect(servico.atual).toEqual({ ano: 2026, mes: 12 });
  });

  it('de dezembro, avançar vai para janeiro do ano seguinte', () => {
    const servico = criarEm(2026, 11);

    servico.mudar(1);

    expect(servico.atual).toEqual({ ano: 2027, mes: 1 });
  });

  it('de janeiro, voltar vai para dezembro do ano anterior', () => {
    const servico = criarEm(2026, 0);

    servico.mudar(-1);

    expect(servico.atual).toEqual({ ano: 2025, mes: 12 });
  });

  it('rotulo$ mostra mês e ano com a primeira letra maiúscula', () => {
    const servico = criarEm(2026, 10);
    let rotulo = '';

    servico.rotulo$.subscribe((valor) => (rotulo = valor));

    expect(rotulo).toBe('Novembro de 2026');
  });

  it('periodo$ emite o mês atual ao assinar e um valor a cada mudança', () => {
    const servico = criarEm(2026, 10);
    const emitidos: Periodo[] = [];

    servico.periodo$.subscribe((periodo) => emitidos.push(periodo));
    servico.mudar(1);
    servico.mudar(1);

    expect(emitidos).toEqual([
      { ano: 2026, mes: 11 },
      { ano: 2026, mes: 12 },
      { ano: 2027, mes: 1 },
    ]);
  });
});