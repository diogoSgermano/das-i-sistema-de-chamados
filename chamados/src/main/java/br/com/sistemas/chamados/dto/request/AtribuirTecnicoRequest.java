package br.com.sistemas.chamados.dto.request;

import br.com.sistemas.chamados.enums.StatusChamado;
import jakarta.validation.constraints.NotNull;

public record AtribuirTecnicoRequest(
        @NotNull(message = "ID do técnico é obrigatório")
        Long tecnicoId
) {
}
