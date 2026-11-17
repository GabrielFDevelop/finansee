import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { MesService } from '../core/mes.service';

@Component({
  selector: 'app-cabecalho',
  imports: [AsyncPipe, RouterLink, RouterLinkActive],
  templateUrl: './cabecalho.component.html',
  styleUrl: './cabecalho.component.scss',
})
export class CabecalhoComponent {
  protected readonly mes = inject(MesService);
}