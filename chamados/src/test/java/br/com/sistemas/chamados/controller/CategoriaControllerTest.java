package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.request.CategoriaRequest;
import br.com.sistemas.chamados.dto.response.CategoriaResponse;
import br.com.sistemas.chamados.exception.GlobalExceptionHandler;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
@Import(GlobalExceptionHandler.class)
class CategoriaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CategoriaService service;

    @Test
    @DisplayName("POST /categorias — deve criar e retornar 201")
    void deveCriarCategoria() throws Exception {
        CategoriaRequest req = new CategoriaRequest("Infraestrutura", "Desc");
        CategoriaResponse resp = new CategoriaResponse(1L, "Infraestrutura", "Desc");

        when(service.criar(any())).thenReturn(resp);

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria").value(1L))
                .andExpect(jsonPath("$.nome").value("Infraestrutura"));
    }

    @Test
    @DisplayName("POST /categorias — nome em branco deve retornar 400")
    void deveRetornar400QuandoNomeEmBranco() throws Exception {
        CategoriaRequest req = new CategoriaRequest("", null);

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /categorias — nome duplicado deve retornar 409")
    void deveRetornar409QuandoNomeDuplicado() throws Exception {
        CategoriaRequest req = new CategoriaRequest("Rede", null);

        when(service.criar(any())).thenThrow(new RegraNegocioException("Nome duplicado"));

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /categorias — deve retornar lista")
    void deveListarCategorias() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(
                new CategoriaResponse(1L, "Infra", "Desc")
        ));

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Infra"));
    }

    @Test
    @DisplayName("GET /categorias/{id} — deve retornar 404 quando não encontrado")
    void deveRetornar404QuandoNaoEncontrado() throws Exception {
        when(service.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("Categoria 99 não encontrada"));

        mockMvc.perform(get("/categorias/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /categorias/{id} — deve atualizar e retornar 200")
    void deveAtualizarCategoria() throws Exception {
        CategoriaRequest req = new CategoriaRequest("Rede", "Desc atualizada");
        CategoriaResponse resp = new CategoriaResponse(1L, "Rede", "Desc atualizada");

        when(service.atualizar(eq(1L), any())).thenReturn(resp);

        mockMvc.perform(put("/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rede"));
    }

    @Test
    @DisplayName("DELETE /categorias/{id} — deve retornar 204")
    void deveExcluirCategoria() throws Exception {
        doNothing().when(service).excluir(1L);

        mockMvc.perform(delete("/categorias/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /categorias/{id} — deve retornar 404 quando não encontrado")
    void deveRetornar404AoExcluirInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Categoria não encontrada"))
                .when(service).excluir(99L);

        mockMvc.perform(delete("/categorias/99"))
                .andExpect(status().isNotFound());
    }
}
