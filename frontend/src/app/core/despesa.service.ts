import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from './api';
import { Despesa } from './modelos';

@Injectable({ providedIn: 'root' })
export class DespesaService {
  private readonly http = inject(HttpClient);

  listar(ano: number, mes: number): Observable<Despesa[]> {
    const params = new HttpParams().set('ano', ano).set('mes', mes);
    return this.http.get<Despesa[]>(`${API_URL}/despesas`, { params });
  }
}