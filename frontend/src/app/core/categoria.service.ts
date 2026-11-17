import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from './api';
import { Categoria, CategoriaRequest } from './modelos';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private readonly http = inject(HttpClient);

  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${API_URL}/categorias`);
  }
  
  criar(dados: CategoriaRequest): Observable<Categoria> {
    return this.http.post<Categoria>(`${API_URL}/categorias`, dados);
  }

  atualizar(id: number, dados: CategoriaRequest): Observable<Categoria> {
    return this.http.put<Categoria>(`${API_URL}/categorias/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/categorias/${id}`);
  }
}