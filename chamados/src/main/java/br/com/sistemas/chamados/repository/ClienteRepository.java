package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long idCliente);

    boolean existsByEmailAndIdClienteNot(@NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email, Long id);
}
