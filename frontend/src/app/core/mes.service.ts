import { Injectable } from '@angular/core';
import { BehaviorSubject, map } from 'rxjs';

export interface Periodo {
  ano: number;
  mes: number;
}

@Injectable({ providedIn: 'root' })
export class MesService {
  private readonly estado = new BehaviorSubject<Periodo>(this.mesAtual());

  readonly periodo$ = this.estado.asObservable();

  readonly rotulo$ = this.periodo$.pipe(
    map(({ ano, mes }) => {
      const texto = new Date(ano, mes - 1, 1).toLocaleDateString('pt-BR', {
        month: 'long',
        year: 'numeric',
      });
      return texto.charAt(0).toUpperCase() + texto.slice(1);
    }),
  );

  mudar(delta: number): void {
    const { ano, mes } = this.estado.value;
    const data = new Date(ano, mes - 1 + delta, 1);
    this.estado.next({ ano: data.getFullYear(), mes: data.getMonth() + 1 });
  }

  private mesAtual(): Periodo {
    const hoje = new Date();
    return { ano: hoje.getFullYear(), mes: hoje.getMonth() + 1 };
  }

  get atual(): Periodo {
    return this.estado.value;
  }
}