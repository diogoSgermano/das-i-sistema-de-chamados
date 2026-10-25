package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.request.ChamadoRequest;
import br.com.sistemas.chamados.dto.request.AlterarStatusRequest;
import br.com.sistemas.chamados.dto.request.AtribuirTecnicoRequest;
import br.com.sistemas.chamados.dto.response.ChamadoResponse;
import br.com.sistemas.chamados.enums.Prioridade;
import br.com.sistemas.chamados.enums.StatusChamado;
import br.com.sistemas.chamados.exception.GlobalExceptionHandler;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChamadoController.class)
@Import(GlobalExceptionHandler.class)
class ChamadoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ChamadoService service;

    private ChamadoResponse chamadoAberto() {
        return new ChamadoResponse(
                1L, "Título", "Descrição",
                LocalDateTime.now(), null,
                Prioridade.ALTA, StatusChamado.ABERTO,
                10L, "Cliente", 20L, "Categoria", null, null
        );
    }

    @Test
    @DisplayName("POST /chamados — deve criar chamado e retornar 201")
    void deveCriarChamado() throws Exception {
        ChamadoRequest req = new ChamadoRequest(
                "Título", "Descrição", Prioridade.ALTA, 10L, 20L, null);

        when(service.criar(any())).thenReturn(chamadoAberto());

        mockMvc.perform(post("/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"));
    }

    @Test
    @DisplayName("POST /chamados — título em branco deve retornar 400")
    void deveRetornar400TituloEmBranco() throws Exception {
        ChamadoRequest req = new ChamadoRequest(
                "", "Descrição", Prioridade.ALTA, 10L, 20L, null);

        mockMvc.perform(post("/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /chamados/{id} — deve retornar 404 para chamado inexistente")
    void deveRetornar404ChamadoNaoEncontrado() throws Exception {
        when(service.buscar(99L))
                .thenThrow(new RecursoNaoEncontradoException("Chamado 99 não encontrado"));

        mockMvc.perform(get("/chamados/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /chamados — deve retornar página com filtros")
    void deveListarComFiltros() throws Exception {
        when(service.listar(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(chamadoAberto())));

        mockMvc.perform(get("/chamados")
                        .param("status", "ABERTO")
                        .param("prioridade", "ALTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ABERTO"));
    }

    @Test
    @DisplayName("PATCH /chamados/{id}/atribuir — deve atribuir técnico")
    void deveAtribuirTecnico() throws Exception {
        AtribuirTecnicoRequest req = new AtribuirTecnicoRequest(5L);
        ChamadoResponse resp = new ChamadoResponse(
                1L, "T", "D", LocalDateTime.now(), null,
                Prioridade.ALTA, StatusChamado.EM_ATENDIMENTO,
                10L, "Cliente", 20L, "Cat", 5L, "Técnico João"
        );

        when(service.atribuirTecnico(eq(1L), any())).thenReturn(resp);

        mockMvc.perform(patch("/chamados/1/atribuir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"))
                .andExpect(jsonPath("$.tecnicoNome").value("Técnico João"));
    }

    @Test
    @DisplayName("PATCH /chamados/{id}/atribuir — chamado fechado deve retornar 409")
    void deveRetornar409AtribuirEmChamadoFechado() throws Exception {
        AtribuirTecnicoRequest req = new AtribuirTecnicoRequest(5L);

        when(service.atribuirTecnico(eq(1L), any()))
                .thenThrow(new RegraNegocioException("Chamado fechado"));

        mockMvc.perform(patch("/chamados/1/atribuir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PATCH /chamados/{id}/status — deve fechar chamado e preencher dataFechamento")
    void deveFecharChamado() throws Exception {
        AlterarStatusRequest req = new AlterarStatusRequest(StatusChamado.FECHADO);
        ChamadoResponse resp = new ChamadoResponse(
                1L, "T", "D", LocalDateTime.now(), LocalDateTime.now(),
                Prioridade.ALTA, StatusChamado.FECHADO,
                10L, "Cliente", 20L, "Cat", null, null
        );

        when(service.alterarStatus(eq(1L), any())).thenReturn(resp);

        mockMvc.perform(patch("/chamados/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADO"))
                .andExpect(jsonPath("$.dataFechamento").isNotEmpty());
    }
}
