package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.request.TecnicoRequest;
import br.com.sistemas.chamados.dto.response.TecnicoResponse;
import br.com.sistemas.chamados.enums.PerfilTecnico;
import br.com.sistemas.chamados.exception.GlobalExceptionHandler;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.TecnicoService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TecnicoController.class)
@Import(GlobalExceptionHandler.class)
class TecnicoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean TecnicoService service;

    private TecnicoRequest request(String email) {
        return new TecnicoRequest("Ana Silva", email, PerfilTecnico.TECNICO);
    }

    @Test
    void deveCriarTecnico() throws Exception {
        when(service.criar(any())).thenReturn(new TecnicoResponse(1L, "Ana Silva", "ana@exemplo.com", PerfilTecnico.TECNICO));
        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("ana@exemplo.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("TECNICO"));
    }

    @Test
    void deveRejeitarEmailInvalido() throws Exception {
        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("email-invalido"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarEmailDuplicado() throws Exception {
        when(service.criar(any())).thenThrow(new RegraNegocioException("E-mail duplicado"));
        mockMvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("ana@exemplo.com"))))
                .andExpect(status().isConflict());
    }

    @Test
    void deveListarBuscarAtualizarEExcluir() throws Exception {
        TecnicoResponse tecnico = new TecnicoResponse(1L, "Ana Silva", "ana@exemplo.com", PerfilTecnico.TECNICO);
        when(service.listar()).thenReturn(List.of(tecnico));
        when(service.buscar(1L)).thenReturn(tecnico);
        when(service.atualizar(eq(1L), any())).thenReturn(tecnico);

        mockMvc.perform(get("/tecnicos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].email").value("ana@exemplo.com"));
        mockMvc.perform(get("/tecnicos/1")).andExpect(status().isOk());
        mockMvc.perform(put("/tecnicos/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("ana@exemplo.com"))))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/tecnicos/1")).andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar404ParaTecnicoAusente() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Técnico não encontrado"));
        mockMvc.perform(get("/tecnicos/99")).andExpect(status().isNotFound());
    }
}
