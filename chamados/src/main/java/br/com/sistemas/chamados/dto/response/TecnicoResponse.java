package br.com.sistemas.chamados.dto.response;

import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.enums.PerfilTecnico;

public record TecnicoResponse(
        Long idTecnico,
        String nome,
        String email,
        PerfilTecnico perfil
) {
    public static TecnicoResponse de(Tecnico t) {
        return new TecnicoResponse(t.getIdTecnico(), t.getNome(), t.getEmail(), t.getPerfil());
    }
}