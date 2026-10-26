package br.com.sistemas.chamados.dto.response;

import java.time.LocalDateTime;

import br.com.sistemas.chamados.enums.Prioridade;
import br.com.sistemas.chamados.enums.StatusChamado;
import br.com.sistemas.chamados.entity.Chamado;

public record ChamadoResponse(
        Long idChamado,
        String titulo,
        String descricao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        Prioridade prioridade,
        StatusChamado status,
        Long clienteId,
        String clienteNome,
        Long categoriaId,
        String categoriaNome,
        Long tecnicoId,
        String tecnicoNome
) {
    public static ChamadoResponse de(Chamado c) {
        return new ChamadoResponse(
                c.getIdChamado(),
                c.getTitulo(),
                c.getDescricao(),
                c.getDataAbertura(),
                c.getDataFechamento(),
                c.getPrioridade(),
                c.getStatus(),
                c.getCliente() != null ? c.getCliente().getIdCliente() : null,
                c.getCliente() != null ? c.getCliente().getNome() : null,
                c.getCategoria() != null ? c.getCategoria().getIdCategoria() : null,
                c.getCategoria() != null ? c.getCategoria().getNome() : null,
                c.getTecnico() != null ? c.getTecnico().getIdTecnico() : null,
                c.getTecnico() != null ? c.getTecnico().getNome() : null
        );
    }
}
