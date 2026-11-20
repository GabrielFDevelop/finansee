package com.finansee.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.finansee.backend.categoria.Categoria;
import com.finansee.backend.categoria.CategoriaRepository;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class BackendApplicationTests {

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Test
	@DisplayName("a aplicação sobe: migrations aplicadas e entidades compatíveis com o banco")
	void contextLoads() {
	}

	@Test
	@DisplayName("a migration V2 insere as categorias padrão")
	void migrationsCriamAsCategoriasPadrao() {
		assertThat(categoriaRepository.findAll())
				.extracting(Categoria::getNome)
				.contains("Alimentação", "Transporte", "Moradia", "Saúde", "Educação", "Lazer", "Outros");
	}
}