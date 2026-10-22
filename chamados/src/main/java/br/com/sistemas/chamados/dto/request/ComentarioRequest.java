
package br.com.sistemas.chamados.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(

        @NotBlank(message = "Texto é obrigatório")
        String texto,

        @NotNull(message = "Data é obrigatória")
        LocalDateTime data,

        @NotBlank(message = "Autor é obrigatório")
        @Size(max = 100, message = "Autor deve ter no máximo 100 caracteres")
        String autor,

        @NotNull(message = "Chamado é obrigatório")
        Long chamadoId

) {
}
