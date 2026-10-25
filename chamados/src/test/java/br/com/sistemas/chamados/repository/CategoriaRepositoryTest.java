package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Categoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoriaRepositoryTest {

    @Autowired
    CategoriaRepository repository;

    @Test
    @DisplayName("Deve salvar e encontrar categoria por ID")
    void deveSalvarEBuscarCategoria() {
        Categoria cat = repository.save(new Categoria("Infraestrutura", "Servidores e redes"));
        assertThat(cat.getIdCategoria()).isNotNull();
        assertThat(repository.findById(cat.getIdCategoria())).isPresent();
    }

    @Test
    @DisplayName("Deve detectar nome duplicado (case-insensitive)")
    void deveDetectarNomeDuplicado() {
        repository.save(new Categoria("Hardware", null));
        assertThat(repository.existsByNomeIgnoreCase("hardware")).isTrue();
        assertThat(repository.existsByNomeIgnoreCase("HARDWARE")).isTrue();
    }

    @Test
    @DisplayName("Deve detectar duplicidade excluindo o próprio ID")
    void deveDetectarDuplicidadeParaUpdate() {
        Categoria cat = repository.save(new Categoria("Software", null));
        repository.save(new Categoria("Rede", null));

        // Verificar se 'Rede' existe mas não para o ID de 'Software'
        assertThat(repository.existsByNomeIgnoreCaseAndIdCategoriaNot(
                "Rede", cat.getIdCategoria())).isTrue();

        // A própria categoria não deve conflitar consigo mesma
        assertThat(repository.existsByNomeIgnoreCaseAndIdCategoriaNot(
                "Software", cat.getIdCategoria())).isFalse();
    }
}
