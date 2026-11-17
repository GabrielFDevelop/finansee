import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter, map } from 'rxjs';

import { MesService } from '../core/mes.service';

@Component({
  selector: 'app-cabecalho',
  imports: [AsyncPipe, RouterLink, RouterLinkActive],
  templateUrl: './cabecalho.component.html',
  styleUrl: './cabecalho.component.scss',
})
export class CabecalhoComponent {
  protected readonly mes = inject(MesService);
  private readonly router = inject(Router);

  protected readonly mostraMes = toSignal(
    this.router.events.pipe(
      filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd),
      map((evento) => !evento.urlAfterRedirects.startsWith('/categorias')),
    ),
    { initialValue: !this.router.url.startsWith('/categorias') },
  );
}