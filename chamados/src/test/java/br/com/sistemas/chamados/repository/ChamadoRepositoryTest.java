package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.enums.Prioridade;
import br.com.sistemas.chamados.enums.StatusChamado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ChamadoRepositoryTest {

    @Autowired
    ChamadoRepository chamadoRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    CategoriaRepository categoriaRepository;

    private Cliente criarCliente(String nome, String email) {
        return clienteRepository.save(new Cliente(nome, email, null));
    }

    private Categoria criarCategoria(String nome) {
        return categoriaRepository.save(new Categoria(nome, null));
    }

    @Test
    @DisplayName("Deve salvar chamado e preencher dataAbertura e status ABERTO automaticamente")
    void deveSalvarChamadoComPrePersist() {
        Cliente cliente = criarCliente("Cliente A", "a@a.com");
        Categoria cat = criarCategoria("Infraestrutura");

        Chamado chamado = new Chamado("Título", "Desc", Prioridade.ALTA, cliente, cat, null);
        Chamado salvo = chamadoRepository.save(chamado);

        assertThat(salvo.getIdChamado()).isNotNull();
        assertThat(salvo.getDataAbertura()).isNotNull();
        assertThat(salvo.getStatus()).isEqualTo(StatusChamado.ABERTO);
    }

    @Test
    @DisplayName("Deve encontrar chamados de um cliente")
    void deveListarChamadosPorCliente() {
        Cliente cliente = criarCliente("Cliente B", "b@b.com");
        Categoria cat = criarCategoria("Software");

        chamadoRepository.save(new Chamado("C1", "Desc1", Prioridade.BAIXA, cliente, cat, null));
        chamadoRepository.save(new Chamado("C2", "Desc2", Prioridade.MEDIA, cliente, cat, null));

        List<Chamado> resultado = chamadoRepository.findByClienteIdCliente(cliente.getIdCliente());

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("Deve filtrar chamados por status")
    void deveFiltrarPorStatus() {
        Cliente cliente = criarCliente("Cliente C", "c@c.com");
        Categoria cat = criarCategoria("Rede");

        Chamado c1 = chamadoRepository.save(new Chamado("Chamado 1", "D1", Prioridade.ALTA, cliente, cat, null));
        Chamado c2 = chamadoRepository.save(new Chamado("Chamado 2", "D2", Prioridade.CRITICA, cliente, cat, null));

        // Fecha o segundo chamado manualmente
        c2.setStatus(StatusChamado.FECHADO);
        chamadoRepository.save(c2);

        Page<Chamado> abertos = chamadoRepository.filtrar(
                StatusChamado.ABERTO, null, null, PageRequest.of(0, 10));

        assertThat(abertos.getContent()).extracting(Chamado::getStatus)
                .containsOnly(StatusChamado.ABERTO);
    }

    @Test
    @DisplayName("Deve filtrar chamados por prioridade")
    void deveFiltrarPorPrioridade() {
        Cliente cliente = criarCliente("Cliente D", "d@d.com");
        Categoria cat = criarCategoria("Banco de Dados");

        chamadoRepository.save(new Chamado("Alta", "D", Prioridade.ALTA, cliente, cat, null));
        chamadoRepository.save(new Chamado("Baixa", "D", Prioridade.BAIXA, cliente, cat, null));

        Page<Chamado> altas = chamadoRepository.filtrar(
                null, Prioridade.ALTA, null, PageRequest.of(0, 10));

        assertThat(altas.getContent()).allMatch(c -> c.getPrioridade() == Prioridade.ALTA);
    }
}
