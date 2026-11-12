package br.com.sistemas.chamados.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.sistemas.chamados.dto.request.ComentarioRequest;
import br.com.sistemas.chamados.dto.response.ComentarioResponse;
import br.com.sistemas.chamados.service.ComentarioService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/chamados/{chamadoId:\\d+}/comentarios")
public class ComentarioController {

    private final ComentarioService service;

    public ComentarioController(ComentarioService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // Endpoints REST (JSON)
    // -------------------------------------------------------------------------

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<ComentarioResponse> criar(
            @PathVariable Long chamadoId,
            @Valid @RequestBody ComentarioRequest dto) {
        ComentarioResponse criado = service.criar(chamadoId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping(produces = "application/json")
    @ResponseBody
    public List<ComentarioResponse> listarJson(@PathVariable Long chamadoId) {
        return service.listarPorChamado(chamadoId);
    }

    // -------------------------------------------------------------------------
    // Endpoints Web (Thymeleaf)
    // -------------------------------------------------------------------------

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    public String salvarWeb(
            @PathVariable Long chamadoId,
            @RequestParam String texto,
            @RequestParam String autor) {
        service.criar(chamadoId, new ComentarioRequest(texto, autor));
        return "redirect:/chamados/" + chamadoId + "/detalhe";
    }
}
