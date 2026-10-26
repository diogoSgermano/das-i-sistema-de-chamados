package br.com.sistemas.chamados.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.request.ComentarioRequest;
import br.com.sistemas.chamados.dto.response.ComentarioResponse;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Comentario;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ComentarioRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;

    public ComentarioService(
            ComentarioRepository comentarioRepository,
            ChamadoRepository chamadoRepository) {
        this.comentarioRepository = comentarioRepository;
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional
    public ComentarioResponse criar(Long chamadoId, ComentarioRequest dto) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Chamado " + chamadoId + " não encontrado"));

        Comentario comentario = new Comentario(
                dto.texto(),
                LocalDateTime.now(),
                dto.autor(),
                chamado
        );

        return ComentarioResponse.de(comentarioRepository.save(comentario));
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarPorChamado(Long chamadoId) {
        // Valida que o chamado existe
        chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Chamado " + chamadoId + " não encontrado"));

        return comentarioRepository.findByChamadoIdChamadoOrderByDataAsc(chamadoId)
                .stream()
                .map(ComentarioResponse::de)
                .toList();
    }
}
