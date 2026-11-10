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

import br.com.sistemas.chamados.dto.request.TecnicoRequest;
import br.com.sistemas.chamados.dto.response.TecnicoResponse;
import br.com.sistemas.chamados.enums.PerfilTecnico;
import br.com.sistemas.chamados.service.TecnicoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/tecnicos")
public class TecnicoController {

    private final TecnicoService service;

    public TecnicoController(TecnicoService service) {
        this.service = service;
    }

    // -------------------------------------------------------------------------
    // Endpoints REST (JSON)
    // -------------------------------------------------------------------------

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<TecnicoResponse> criar(
            @Valid @RequestBody TecnicoRequest dto) {
        TecnicoResponse criado = service.criar(dto);
        return ResponseEntity
                .created(URI.create("/tecnicos/" + criado.idTecnico()))
                .body(criado);
    }

    @GetMapping(produces = "application/json")
    @ResponseBody
    public List<TecnicoResponse> listarJson() {
        return service.listar();
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public TecnicoResponse buscarJson(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public TecnicoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TecnicoRequest dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Endpoints Web (Thymeleaf)
    // -------------------------------------------------------------------------

    @GetMapping(produces = "text/html")
    public String paginaListar(Model model) {
        model.addAttribute("tecnicos", service.listar());
        return "tecnicos/lista";
    }

    @GetMapping("/novo")
    public String paginaNovo(Model model) {
        model.addAttribute("perfis", PerfilTecnico.values());
        return "tecnicos/form";
    }

    @GetMapping("/{id}/editar")
    public String paginaEditar(@PathVariable Long id, Model model) {
        model.addAttribute("tecnico", service.buscar(id));
        model.addAttribute("perfis", PerfilTecnico.values());
        return "tecnicos/form";
    }

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    public String salvar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam PerfilTecnico perfil) {
        service.criar(new TecnicoRequest(nome, email, perfil));
        return "redirect:/tecnicos";
    }

    @PostMapping(value = "/{id}/atualizar", consumes = "application/x-www-form-urlencoded")
    public String atualizar(
            @PathVariable Long id,
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam PerfilTecnico perfil) {
        service.atualizar(id, new TecnicoRequest(nome, email, perfil));
        return "redirect:/tecnicos";
    }

    @PostMapping("/{id}/excluir")
    public String excluirWeb(@PathVariable Long id) {
        service.excluir(id);
        return "redirect:/tecnicos";
    }
}
