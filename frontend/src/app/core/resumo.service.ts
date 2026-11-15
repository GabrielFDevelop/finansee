import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from './api';
import { ResumoMensal } from './modelos';

@Injectable({ providedIn: 'root' })
export class ResumoService {
  private readonly http = inject(HttpClient);

  buscar(ano: number, mes: number): Observable<ResumoMensal> {
    const params = new HttpParams().set('ano', ano).set('mes', mes);
    return this.http.get<ResumoMensal>(`${API_URL}/resumo`, { params });
  }
}