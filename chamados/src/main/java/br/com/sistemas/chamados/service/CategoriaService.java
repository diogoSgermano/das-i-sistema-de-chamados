
package br.com.sistemas.chamados.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.sistemas.chamados.dto.request.CategoriaRequest;
import br.com.sistemas.chamados.dto.response.CategoriaResponse;
import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        String nome = request.nome().trim();

        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma categoria com esse nome");
        }

        Categoria categoria = new Categoria(
                nome,
                request.descricao()
        );

        Categoria salva = categoriaRepository.save(categoria);

        return converterParaResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        Categoria categoria = buscarEntidadePorId(id);

        return converterParaResponse(categoria);
    }

    @Transactional
    public CategoriaResponse atualizar(
            Long id, CategoriaRequest request) {

        Categoria categoria = buscarEntidadePorId(id);
        String nome = request.nome().trim();

        boolean nomeDuplicado =
                categoriaRepository
                        .existsByNomeIgnoreCaseAndIdCategoriaNot(
                                nome, id);

        if (nomeDuplicado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe outra categoria com esse nome");
        }

        categoria.setNome(nome);
        categoria.setDescricao(request.descricao());

        Categoria atualizada = categoriaRepository.save(categoria);

        return converterParaResponse(atualizada);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarEntidadePorId(id);

        categoriaRepository.delete(categoria);
    }

    private Categoria buscarEntidadePorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria não encontrada"));
    }

    private CategoriaResponse converterParaResponse(
            Categoria categoria) {

        return new CategoriaResponse(
                categoria.getIdCategoria(),
                categoria.getNome(),
                categoria.getDescricao()
        );
    }
}
