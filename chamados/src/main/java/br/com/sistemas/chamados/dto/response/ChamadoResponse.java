
package br.com.sistemas.chamados.dto.response;

import java.time.LocalDateTime;

public record ChamadoResponse(
        Long idChamado,
        String titulo,
        String descricao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        String prioridade,
        String status,
        Long clienteId,
        Long categoriaId,
        Long tecnicoId
) {
}
