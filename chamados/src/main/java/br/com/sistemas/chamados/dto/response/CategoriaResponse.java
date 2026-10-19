package br.com.sistemas.chamados.dto.response;

public record CategoriaResponse(
        Long idCategoria,
        String nome,
        String descricao
) {
}
