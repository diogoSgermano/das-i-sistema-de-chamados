package br.com.sistemas.chamados.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.request.AlterarStatusRequest;
import br.com.sistemas.chamados.dto.request.AtribuirTecnicoRequest;
import br.com.sistemas.chamados.dto.request.ChamadoRequest;
import br.com.sistemas.chamados.dto.response.ChamadoResponse;
import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.enums.Prioridade;
import br.com.sistemas.chamados.enums.StatusChamado;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;

    public ChamadoService(
            ChamadoRepository chamadoRepository,
            ClienteRepository clienteRepository,
            CategoriaRepository categoriaRepository,
            TecnicoRepository tecnicoRepository) {
        this.chamadoRepository = chamadoRepository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @Transactional
    public ChamadoResponse criar(ChamadoRequest dto) {
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente " + dto.clienteId() + " não encontrado"));

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria " + dto.categoriaId() + " não encontrada"));

        Tecnico tecnico = null;
        if (dto.tecnicoId() != null) {
            tecnico = tecnicoRepository.findById(dto.tecnicoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Técnico " + dto.tecnicoId() + " não encontrado"));
        }

        Chamado chamado = new Chamado(
                dto.titulo(),
                dto.descricao(),
                dto.prioridade(),
                cliente,
                categoria,
                tecnico
        );

        return ChamadoResponse.de(chamadoRepository.save(chamado));
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listar(StatusChamado status, Prioridade prioridade, Long clienteId, Pageable pageable) {
        return chamadoRepository.filtrar(status, prioridade, clienteId, pageable)
                .map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public ChamadoResponse buscar(Long id) {
        return ChamadoResponse.de(buscarEntity(id));
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest dto) {
        Chamado chamado = buscarEntity(id);

        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new RegraNegocioException("Não é possível alterar um chamado fechado.");
        }

        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente " + dto.clienteId() + " não encontrado"));

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria " + dto.categoriaId() + " não encontrada"));

        Tecnico tecnico = null;
        if (dto.tecnicoId() != null) {
            tecnico = tecnicoRepository.findById(dto.tecnicoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Técnico " + dto.tecnicoId() + " não encontrado"));
        }

        chamado.setTitulo(dto.titulo());
        chamado.setDescricao(dto.descricao());
        chamado.setPrioridade(dto.prioridade());
        chamado.setCliente(cliente);
        chamado.setCategoria(categoria);
        chamado.setTecnico(tecnico);

        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public void excluir(Long id) {
        chamadoRepository.delete(buscarEntity(id));
    }

    @Transactional
    public ChamadoResponse atribuirTecnico(Long id, AtribuirTecnicoRequest dto) {
        Chamado chamado = buscarEntity(id);

        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new RegraNegocioException(
                    "Não é possível atribuir técnico a um chamado fechado.");
        }

        Tecnico tecnico = tecnicoRepository.findById(dto.tecnicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Técnico " + dto.tecnicoId() + " não encontrado"));

        chamado.setTecnico(tecnico);

        // Ao atribuir técnico, mover para EM_ATENDIMENTO se ainda ABERTO
        if (chamado.getStatus() == StatusChamado.ABERTO) {
            chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        }

        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse alterarStatus(Long id, AlterarStatusRequest dto) {
        Chamado chamado = buscarEntity(id);

        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new RegraNegocioException(
                    "Não é possível alterar o status de um chamado já fechado.");
        }

        chamado.setStatus(dto.status());

        if (dto.status() == StatusChamado.FECHADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        }

        return ChamadoResponse.de(chamado);
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponse> listarPorCliente(Long clienteId) {
        // Valida que o cliente existe
        clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente " + clienteId + " não encontrado"));

        return chamadoRepository.findByClienteIdCliente(clienteId)
                .stream()
                .map(ChamadoResponse::de)
                .toList();
    }

    private Chamado buscarEntity(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Chamado " + id + " não encontrado"));
    }
}
