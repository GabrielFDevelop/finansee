import { AsyncPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  Component, ElementRef, EventEmitter, Output, ViewChild, inject, signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { MesService } from '../../core/mes.service';
import { Categoria } from '../../core/modelos';
import { OrcamentoService } from '../../core/orcamento.service';

@Component({
  selector: 'app-form-orcamento',
  imports: [AsyncPipe, ReactiveFormsModule],
  templateUrl: './form-orcamento.component.html',
  styleUrl: './form-orcamento.component.scss',
})
export class FormOrcamentoComponent {
  @Output() salva = new EventEmitter<void>();
  @ViewChild('dialogo', { static: true }) private dialogo!: ElementRef<HTMLDialogElement>;

  private readonly fb = inject(FormBuilder);
  private readonly orcamentoService = inject(OrcamentoService);
  protected readonly mes = inject(MesService);

  protected readonly categoria = signal<Categoria | null>(null);
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly form = this.fb.group({
    valorLimite: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });

  abrir(categoria: Categoria, limiteAtual?: number | null): void {
    this.categoria.set(categoria);
    this.form.reset({ valorLimite: limiteAtual ?? null });
    this.erro.set(null);
    this.dialogo.nativeElement.showModal();
  }

  protected fechar(): void {
    this.dialogo.nativeElement.close();
  }

  protected invalido(): boolean {
    const controle = this.form.controls.valorLimite;
    return controle.invalid && (controle.touched || controle.dirty);
  }

  protected salvar(): void {
    const categoria = this.categoria();
    if (this.form.invalid || !categoria) {
      this.form.markAllAsTouched();
      return;
    }

    const periodo = this.mes.atual;
    this.salvando.set(true);
    this.erro.set(null);

    this.orcamentoService
      .definir(periodo.ano, periodo.mes, categoria.id, this.form.getRawValue().valorLimite!)
      .subscribe({
        next: () => {
          this.salvando.set(false);
          this.fechar();
          this.salva.emit();
        },
        error: (e: HttpErrorResponse) => {
          this.salvando.set(false);
          this.erro.set(e.error?.detail ?? 'Não foi possível salvar o limite.');
        },
      });
  }
}