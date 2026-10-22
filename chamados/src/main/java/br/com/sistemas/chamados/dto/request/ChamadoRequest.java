
package br.com.sistemas.chamados.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChamadoRequest(

        @NotBlank(message = "Título é obrigatório")
        @Size(max = 150, message = "Título deve ter no máximo 150 caracteres")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotNull(message = "Data de abertura é obrigatória")
        LocalDateTime dataAbertura,

        LocalDateTime dataFechamento,

        @NotBlank(message = "Prioridade é obrigatória")
        @Size(max = 30, message = "Prioridade deve ter no máximo 30 caracteres")
        String prioridade,

        @NotBlank(message = "Status é obrigatório")
        @Size(max = 30, message = "Status deve ter no máximo 30 caracteres")
        String status,

        @NotNull(message = "Cliente é obrigatório")
        Long clienteId,

        @NotNull(message = "Categoria é obrigatória")
        Long categoriaId,

        Long tecnicoId

) {
}
