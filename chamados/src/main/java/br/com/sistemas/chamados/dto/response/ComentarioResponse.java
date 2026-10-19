
package br.com.sistemas.chamados.dto.response;

import java.time.LocalDateTime;

public record ComentarioResponse(
        Long idComentario,
        String texto,
        LocalDateTime data,
        String autor,
        Long chamadoId
) {
}
