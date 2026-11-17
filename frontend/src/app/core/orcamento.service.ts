import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from './api';
import { Orcamento } from './modelos';

@Injectable({ providedIn: 'root' })
export class OrcamentoService {
  private readonly http = inject(HttpClient);

  listar(ano: number, mes: number): Observable<Orcamento[]> {
    const params = new HttpParams().set('ano', ano).set('mes', mes);
    return this.http.get<Orcamento[]>(`${API_URL}/orcamentos`, { params });
  }

  definir(ano: number, mes: number, categoriaId: number, valorLimite: number): Observable<Orcamento> {
    return this.http.put<Orcamento>(
      `${API_URL}/orcamentos/${ano}/${mes}/${categoriaId}`,
      { valorLimite },
    );
  }

  remover(ano: number, mes: number, categoriaId: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/orcamentos/${ano}/${mes}/${categoriaId}`);
  }
}