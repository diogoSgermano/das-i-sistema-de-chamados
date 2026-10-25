package br.com.sistemas.chamados.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.request.TecnicoRequest;
import br.com.sistemas.chamados.dto.response.TecnicoResponse;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.TecnicoRepository;

@Service
public class TecnicoService {

    private final TecnicoRepository repository;

    public TecnicoService(TecnicoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TecnicoResponse criar(TecnicoRequest dto) {
        String email = dto.email().trim();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Já existe técnico com o e-mail " + email);
        }
        Tecnico salvo = repository.save(new Tecnico(dto.nome().trim(), email, dto.perfil()));
        return TecnicoResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public List<TecnicoResponse> listar() {
        return repository.findAll().stream().map(TecnicoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public TecnicoResponse buscar(Long id) {
        return TecnicoResponse.de(buscarEntity(id));
    }

    @Transactional
    public TecnicoResponse atualizar(Long id, TecnicoRequest dto) {
        Tecnico tecnico = buscarEntity(id);
        String email = dto.email().trim();
        if (repository.existsByEmailIgnoreCaseAndIdTecnicoNot(email, id)) {
            throw new RegraNegocioException("Já existe técnico com o e-mail " + email);
        }
        tecnico.setNome(dto.nome().trim());
        tecnico.setEmail(email);
        tecnico.setPerfil(dto.perfil());
        return TecnicoResponse.de(tecnico);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntity(id));
    }

    public Tecnico buscarEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Técnico " + id + " não encontrado"));
    }
}
