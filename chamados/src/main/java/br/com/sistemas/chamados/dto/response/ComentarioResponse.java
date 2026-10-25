package br.com.sistemas.chamados.dto.response;

import java.time.LocalDateTime;

import br.com.sistemas.chamados.entity.Comentario;

public record ComentarioResponse(
        Long idComentario,
        String texto,
        LocalDateTime data,
        String autor,
        Long chamadoId
) {
    public static ComentarioResponse de(Comentario c) {
        return new ComentarioResponse(
                c.getIdComentario(),
                c.getTexto(),
                c.getData(),
                c.getAutor(),
                c.getChamado() != null ? c.getChamado().getIdChamado() : null
        );
    }
}
