package br.com.sistemas.chamados.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import br.com.sistemas.chamados.dto.request.CategoriaRequest;
import br.com.sistemas.chamados.dto.response.CategoriaResponse;
import br.com.sistemas.chamados.service.CategoriaService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // -------------------------------------------------------------------------
    // Endpoints REST (JSON)
    // -------------------------------------------------------------------------

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<CategoriaResponse> criar(
            @Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse response = categoriaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<CategoriaResponse>> listarTodas() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<CategoriaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<CategoriaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.atualizar(id, request));
    }

    @DeleteMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Endpoints Web (Thymeleaf)
    // -------------------------------------------------------------------------

    @GetMapping(produces = "text/html")
    public String paginaListar(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "categorias/lista";
    }

    @GetMapping("/novo")
    public String paginaNovo() {
        return "categorias/form";
    }

    @GetMapping("/{id}/editar")
    public String paginaEditar(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", categoriaService.buscarPorId(id));
        return "categorias/form";
    }

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    public String salvar(
            @RequestParam String nome,
            @RequestParam(required = false) String descricao) {
        categoriaService.criar(new CategoriaRequest(nome, descricao));
        return "redirect:/categorias";
    }

    @PostMapping(value = "/{id}/atualizar", consumes = "application/x-www-form-urlencoded")
    public String atualizar(
            @PathVariable Long id,
            @RequestParam String nome,
            @RequestParam(required = false) String descricao) {
        categoriaService.atualizar(id, new CategoriaRequest(nome, descricao));
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/excluir")
    public String excluirWeb(@PathVariable Long id) {
        categoriaService.excluir(id);
        return "redirect:/categorias";
    }
}
