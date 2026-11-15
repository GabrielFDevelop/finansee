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
}

export interface Despesa {
  id: number;
  descricao: string;
  valor: number;
  data: string; // "2026-11-15"
  categoriaId: number;
  categoriaNome: string;
}