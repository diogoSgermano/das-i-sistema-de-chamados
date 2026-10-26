package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.enums.Prioridade;
import br.com.sistemas.chamados.enums.StatusChamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    List<Chamado> findByClienteIdCliente(Long clienteId);

    @Query("""
            SELECT c FROM Chamado c
            WHERE (:status IS NULL OR c.status = :status)
              AND (:prioridade IS NULL OR c.prioridade = :prioridade)
              AND (:clienteId IS NULL OR c.cliente.idCliente = :clienteId)
            """)
    Page<Chamado> filtrar(
            @Param("status") StatusChamado status,
            @Param("prioridade") Prioridade prioridade,
            @Param("clienteId") Long clienteId,
            Pageable pageable
    );
}
