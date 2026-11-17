export interface CategoriaResumo {
  categoriaId: number;
  categoriaNome: string;
  total: number;
  percentual: number;
}

export interface ResumoMensal {
  ano: number;
  mes: number;
  totalGasto: number;
  quantidadeDespesas: number;
  porCategoria: CategoriaResumo[];
  totalOrcado: number;
  orcamentos: OrcamentoResumo[];
}

export interface Despesa {
  id: number;
  descricao: string;
  valor: number;
  data: string; // "2026-11-15"
  categoriaId: number;
  categoriaNome: string;
}

export interface Categoria {
  id: number;
  nome: string;
}

export interface DespesaRequest {
  descricao: string;
  valor: number;
  data: string; // "2026-11-15"
  categoriaId: number;
}

export interface CategoriaRequest {
  nome: string;
}

export interface OrcamentoResumo {
  categoriaId: number;
  categoriaNome: string;
  limite: number;
  gasto: number;
  percentualUsado: number;
}

export interface Orcamento {
  categoriaId: number;
  categoriaNome: string;
  ano: number;
  mes: number;
  valorLimite: number;
}