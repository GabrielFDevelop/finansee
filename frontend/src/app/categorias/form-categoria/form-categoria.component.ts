import { HttpErrorResponse } from '@angular/common/http';
import {
  Component, ElementRef, EventEmitter, Output, ViewChild, inject, signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { CategoriaService } from '../../core/categoria.service';
import { Categoria, CategoriaRequest } from '../../core/modelos';

@Component({
  selector: 'app-form-categoria',
  imports: [ReactiveFormsModule],
  templateUrl: './form-categoria.component.html',
  styleUrl: './form-categoria.component.scss',
})
export class FormCategoriaComponent {
  @Output() salva = new EventEmitter<void>();
  @ViewChild('dialogo', { static: true }) private dialogo!: ElementRef<HTMLDialogElement>;

  private readonly fb = inject(FormBuilder);
  private readonly categoriaService = inject(CategoriaService);

  protected readonly editandoId = signal<number | null>(null);
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(80)]],
  });

  abrir(categoria?: Categoria): void {
    this.editandoId.set(categoria?.id ?? null);
    this.form.reset({ nome: categoria?.nome ?? '' });
    this.erro.set(null);
    this.dialogo.nativeElement.showModal();
  }

  protected fechar(): void {
    this.dialogo.nativeElement.close();
  }

  protected invalido(): boolean {
    const controle = this.form.controls.nome;
    return controle.invalid && (controle.touched || controle.dirty);
  }

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const dados: CategoriaRequest = { nome: this.form.getRawValue().nome!.trim() };
    const id = this.editandoId();
    const requisicao = id
      ? this.categoriaService.atualizar(id, dados)
      : this.categoriaService.criar(dados);

    this.salvando.set(true);
    this.erro.set(null);

    requisicao.subscribe({
      next: () => {
        this.salvando.set(false);
        this.fechar();
        this.salva.emit();
      },
      error: (e: HttpErrorResponse) => {
        this.salvando.set(false);
        this.erro.set(e.error?.detail ?? 'Não foi possível salvar a categoria.');
      },
    });
  }
}