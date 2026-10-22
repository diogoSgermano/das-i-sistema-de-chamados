package br.com.sistemas.chamados.dto.response;

import br.com.sistemas.chamados.enums.PerfilTecnico;

public record TecnicoResponse(
        Long idTecnico,
        String nome,
        String email,
        PerfilTecnico perfil
) {
}