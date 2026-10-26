
package br.com.sistemas.chamados.entity;

import br.com.sistemas.chamados.enums.PerfilTecnico;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tecnicos")
public class Tecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tecnico")
    private Long idTecnico;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 120, message = "E-mail deve ter no máximo 120 caracteres")
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @NotNull(message = "Perfil é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PerfilTecnico perfil;

    public Tecnico() {
    }

    public Tecnico(String nome, String email, PerfilTecnico perfil) {
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
    }

    public Long getIdTecnico() {
        return idTecnico;
    }

    public void setIdTecnico(Long idTecnico) {
        this.idTecnico = idTecnico;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PerfilTecnico getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilTecnico perfil) {
        this.perfil = perfil;
    }
}