import { HttpErrorResponse } from '@angular/common/http';
import {
  Component, ElementRef, EventEmitter, OnInit, Output, ViewChild, inject, signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { CategoriaService } from '../../core/categoria.service';
import { DespesaService } from '../../core/despesa.service';
import { Categoria, Despesa, DespesaRequest } from '../../core/modelos';

@Component({
  selector: 'app-form-despesa',
  imports: [ReactiveFormsModule],
  templateUrl: './form-despesa.component.html',
  styleUrl: './form-despesa.component.scss',
})
export class FormDespesaComponent implements OnInit {
  @Output() salva = new EventEmitter<void>();
  @ViewChild('dialogo', { static: true }) private dialogo!: ElementRef<HTMLDialogElement>;

  private readonly fb = inject(FormBuilder);
  private readonly categoriaService = inject(CategoriaService);
  private readonly despesaService = inject(DespesaService);

  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly editandoId = signal<number | null>(null);

  protected readonly form = this.fb.group({
    descricao: ['', [Validators.required, Validators.maxLength(150)]],
    valor: [null as number | null, [Validators.required, Validators.min(0.01)]],
    data: [this.hoje(), Validators.required],
    categoriaId: [null as number | null, Validators.required],
  });

  ngOnInit(): void {
    this.categoriaService.listar().subscribe({
      next: (categorias) => this.categorias.set(categorias),
      error: () => this.erro.set('Não foi possível carregar as categorias.'),
    });
  }

  abrir(despesa?: Despesa): void {
    this.editandoId.set(despesa?.id ?? null);
    this.form.reset({
      descricao: despesa?.descricao ?? '',
      valor: despesa?.valor ?? null,
      data: despesa?.data ?? this.hoje(),
      categoriaId: despesa?.categoriaId ?? null,
    });
    this.erro.set(null);
    this.dialogo.nativeElement.showModal();
  }

  protected fechar(): void {
    this.dialogo.nativeElement.close();
  }

  protected invalido(campo: string): boolean {
    const controle = this.form.get(campo);
    return !!controle && controle.invalid && (controle.touched || controle.dirty);
  }

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const dados: DespesaRequest = {
      descricao: valores.descricao!.trim(),
      valor: valores.valor!,
      data: valores.data!,
      categoriaId: valores.categoriaId!,
    };

    const id = this.editandoId();
    const requisicao = id
      ? this.despesaService.atualizar(id, dados)
      : this.despesaService.criar(dados);

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
        this.erro.set(e.error?.detail ?? 'Não foi possível salvar a despesa.');
      },
    });
  }

  private hoje(): string {
    const d = new Date();
    const mes = String(d.getMonth() + 1).padStart(2, '0');
    const dia = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${mes}-${dia}`;
  }
}