import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from './api';
import { Despesa, DespesaRequest } from './modelos';
@Injectable({ providedIn: 'root' })
export class DespesaService {
  private readonly http = inject(HttpClient);

  listar(ano: number, mes: number): Observable<Despesa[]> {
    const params = new HttpParams().set('ano', ano).set('mes', mes);
    return this.http.get<Despesa[]>(`${API_URL}/despesas`, { params });
  }

  criar(dados: DespesaRequest): Observable<Despesa> {
    return this.http.post<Despesa>(`${API_URL}/despesas`, dados);
  }
  
  atualizar(id: number, dados: DespesaRequest): Observable<Despesa> {
    return this.http.put<Despesa>(`${API_URL}/despesas/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/despesas/${id}`);
  }
}