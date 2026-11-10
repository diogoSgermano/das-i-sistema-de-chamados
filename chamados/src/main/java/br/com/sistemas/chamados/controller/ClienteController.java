package br.com.sistemas.chamados.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.sistemas.chamados.dto.request.ClienteRequest;
import br.com.sistemas.chamados.dto.response.ChamadoResponse;
import br.com.sistemas.chamados.dto.response.ClienteResponse;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;
    private final ChamadoService chamadoService;

    public ClienteController(ClienteService service, ChamadoService chamadoService) {
        this.service = service;
        this.chamadoService = chamadoService;
    }

    // -------------------------------------------------------------------------
    // Endpoints REST (JSON)
    // -------------------------------------------------------------------------

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<ClienteResponse> criar(
            @Valid @RequestBody ClienteRequest dto) {
        ClienteResponse criado = service.criar(dto);
        return ResponseEntity.created(URI.create("/clientes/" + criado.id())).body(criado);
    }

    @GetMapping(produces = "application/json")
    @ResponseBody
    public List<ClienteResponse> listarJson() {
        return service.listar();
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public ClienteResponse buscarJson(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ClienteResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/chamados", produces = "application/json")
    @ResponseBody
    public List<ChamadoResponse> listarChamados(@PathVariable Long id) {
        return chamadoService.listarPorCliente(id);
    }

    // -------------------------------------------------------------------------
    // Endpoints Web (Thymeleaf)
    // -------------------------------------------------------------------------

    @GetMapping(produces = "text/html")
    public String paginaListar(Model model) {
        model.addAttribute("clientes", service.listar());
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String paginaNovo() {
        return "clientes/form";
    }

    @GetMapping("/{id}/editar")
    public String paginaEditar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", service.buscar(id));
        return "clientes/form";
    }

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    public String salvar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam(required = false) String telefone) {
        service.criar(new ClienteRequest(nome, email, telefone));
        return "redirect:/clientes";
    }

    @PostMapping(value = "/{id}/atualizar", consumes = "application/x-www-form-urlencoded")
    public String atualizar(
            @PathVariable Long id,
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam(required = false) String telefone) {
        service.atualizar(id, new ClienteRequest(nome, email, telefone));
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/excluir")
    public String excluirWeb(@PathVariable Long id) {
        service.excluir(id);
        return "redirect:/clientes";
    }
}
