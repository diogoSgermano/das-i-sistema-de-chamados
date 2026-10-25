package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdTecnicoNot(String email, Long idTecnico);
}
