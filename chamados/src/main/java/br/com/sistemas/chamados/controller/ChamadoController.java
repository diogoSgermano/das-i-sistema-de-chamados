package br.com.sistemas.chamados.controller;

import java.net.URI;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.sistemas.chamados.dto.request.AlterarStatusRequest;
import br.com.sistemas.chamados.dto.request.AtribuirTecnicoRequest;
import br.com.sistemas.chamados.dto.request.ChamadoRequest;
import br.com.sistemas.chamados.dto.response.ChamadoResponse;
import br.com.sistemas.chamados.enums.Prioridade;
import br.com.sistemas.chamados.enums.StatusChamado;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.TecnicoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService service;

    @Autowired(required = false)
    private ClienteService clienteService;

    @Autowired(required = false)
    private CategoriaService categoriaService;

    @Autowired(required = false)
    private TecnicoService tecnicoService;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // Endpoints REST (JSON)
    // -------------------------------------------------------------------------

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<ChamadoResponse> criar(
            @Valid @RequestBody ChamadoRequest dto) {
        ChamadoResponse criado = service.criar(dto);
        return ResponseEntity
                .created(URI.create("/chamados/" + criado.idChamado()))
                .body(criado);
    }

    @GetMapping(produces = "application/json")
    @ResponseBody
    public Page<ChamadoResponse> listarJson(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long clienteId,
            @PageableDefault(size = 20, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(status, prioridade, clienteId, pageable);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public ChamadoResponse buscarJson(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ChamadoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ChamadoRequest dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}/atribuir", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ChamadoResponse atribuirTecnico(
            @PathVariable Long id,
            @Valid @RequestBody AtribuirTecnicoRequest dto) {
        return service.atribuirTecnico(id, dto);
    }

    @PatchMapping(value = "/{id}/status", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ChamadoResponse alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AlterarStatusRequest dto) {
        return service.alterarStatus(id, dto);
    }

    // -------------------------------------------------------------------------
    // Endpoints Web (Thymeleaf)
    // -------------------------------------------------------------------------

    @GetMapping(produces = "text/html")
    public String paginaListar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long clienteId,
            @PageableDefault(size = 20, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        model.addAttribute("chamados", service.listar(status, prioridade, clienteId, pageable));
        model.addAttribute("statusList", StatusChamado.values());
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("clientes", clienteService != null ? clienteService.listar() : Collections.emptyList());
        model.addAttribute("statusSel", status);
        model.addAttribute("prioridadeSel", prioridade);
        model.addAttribute("clienteIdSel", clienteId);
        return "chamados/lista";
    }

    @GetMapping("/novo")
    public String paginaNovo(Model model) {
        model.addAttribute("clientes", clienteService != null ? clienteService.listar() : Collections.emptyList());
        model.addAttribute("categorias", categoriaService != null ? categoriaService.listarTodas() : Collections.emptyList());
        model.addAttribute("tecnicos", tecnicoService != null ? tecnicoService.listar() : Collections.emptyList());
        model.addAttribute("prioridades", Prioridade.values());
        return "chamados/form";
    }

    @GetMapping("/{id}/editar")
    public String paginaEditar(@PathVariable Long id, Model model) {
        model.addAttribute("chamado", service.buscar(id));
        model.addAttribute("clientes", clienteService != null ? clienteService.listar() : Collections.emptyList());
        model.addAttribute("categorias", categoriaService != null ? categoriaService.listarTodas() : Collections.emptyList());
        model.addAttribute("tecnicos", tecnicoService != null ? tecnicoService.listar() : Collections.emptyList());
        model.addAttribute("prioridades", Prioridade.values());
        return "chamados/form";
    }

    @GetMapping("/{id}/detalhe")
    public String paginaDetalhe(@PathVariable Long id, Model model) {
        model.addAttribute("chamado", service.buscar(id));
        model.addAttribute("tecnicos", tecnicoService != null ? tecnicoService.listar() : Collections.emptyList());
        model.addAttribute("statusList", StatusChamado.values());
        return "chamados/detalhe";
    }

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    public String salvar(
            @RequestParam String titulo,
            @RequestParam String descricao,
            @RequestParam Prioridade prioridade,
            @RequestParam Long clienteId,
            @RequestParam Long categoriaId,
            @RequestParam(required = false) Long tecnicoId) {
        service.criar(new ChamadoRequest(titulo, descricao, prioridade, clienteId, categoriaId, tecnicoId));
        return "redirect:/chamados";
    }

    @PostMapping(value = "/{id}/atualizar", consumes = "application/x-www-form-urlencoded")
    public String atualizarWeb(
            @PathVariable Long id,
            @RequestParam String titulo,
            @RequestParam String descricao,
            @RequestParam Prioridade prioridade,
            @RequestParam Long clienteId,
            @RequestParam Long categoriaId,
            @RequestParam(required = false) Long tecnicoId) {
        service.atualizar(id, new ChamadoRequest(titulo, descricao, prioridade, clienteId, categoriaId, tecnicoId));
        return "redirect:/chamados";
    }

    @PostMapping("/{id}/excluir")
    public String excluirWeb(@PathVariable Long id) {
        service.excluir(id);
        return "redirect:/chamados";
    }

    @PostMapping(value = "/{id}/atribuir-tecnico", consumes = "application/x-www-form-urlencoded")
    public String atribuirTecnicoWeb(
            @PathVariable Long id,
            @RequestParam Long tecnicoId) {
        service.atribuirTecnico(id, new AtribuirTecnicoRequest(tecnicoId));
        return "redirect:/chamados/" + id + "/detalhe";
    }

    @PostMapping(value = "/{id}/alterar-status", consumes = "application/x-www-form-urlencoded")
    public String alterarStatusWeb(
            @PathVariable Long id,
            @RequestParam StatusChamado status) {
        service.alterarStatus(id, new AlterarStatusRequest(status));
        return "redirect:/chamados/" + id + "/detalhe";
    }
}
