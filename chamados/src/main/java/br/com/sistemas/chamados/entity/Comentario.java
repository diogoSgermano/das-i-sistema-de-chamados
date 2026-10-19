package br.com.sistemas.chamados.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comentario")
    private Long idComentario;

    @NotBlank(message = "Texto é obrigatório")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @NotNull(message = "Data é obrigatória")
    @Column(nullable = false)
    private LocalDateTime data;

    @NotBlank(message = "Autor é obrigatório")
    @Size(max = 100, message = "Autor deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String autor;

    @NotNull(message = "Chamado é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    public Comentario() {
    }

    public Comentario(
            String texto,
            LocalDateTime data,
            String autor,
            Chamado chamado) {
        this.texto = texto;
        this.data = data;
        this.autor = autor;
        this.chamado = chamado;
    }

    public Long getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(Long idComentario) {
        this.idComentario = idComentario;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Chamado getChamado() {
        return chamado;
    }

    public void setChamado(Chamado chamado) {
        this.chamado = chamado;
    }
}
