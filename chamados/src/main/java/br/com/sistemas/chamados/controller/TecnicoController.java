package br.com.sistemas.chamados.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.form.TecnicoForm;
import br.com.sistemas.chamados.dto.request.TecnicoRequest;
import br.com.sistemas.chamados.dto.response.TecnicoResponse;
import br.com.sistemas.chamados.enums.PerfilTecnico;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.TecnicoService;
import jakarta.validation.Valid;

/**
 * Técnicos: API REST em /tecnicos e telas HTML (Thymeleaf) em /web/tecnicos.
 */
@Controller
public class TecnicoController {

    private final TecnicoService service;

    public TecnicoController(TecnicoService service) {
        this.service = service;
    }

    // ------------------------------------------------------------------
    // API REST
    // ------------------------------------------------------------------

    @PostMapping("/tecnicos")
    @ResponseBody
    public ResponseEntity<TecnicoResponse> criar(
            @Valid @RequestBody TecnicoRequest dto) {
        TecnicoResponse criado = service.criar(dto);
        return ResponseEntity
                .created(URI.create("/tecnicos/" + criado.idTecnico()))
                .body(criado);
    }

    @GetMapping("/tecnicos")
    @ResponseBody
    public List<TecnicoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/tecnicos/{id}")
    @ResponseBody
    public TecnicoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/tecnicos/{id}")
    @ResponseBody
    public TecnicoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TecnicoRequest dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/tecnicos/{id}")
    @ResponseBody
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Telas (Thymeleaf)
    // ------------------------------------------------------------------

    /** Tela 5: lista de técnicos. */
    @GetMapping("/web/tecnicos")
    public String telaLista(Model model) {
        model.addAttribute("tecnicos", service.listar());
        return "tecnicos/lista";
    }

    @GetMapping("/web/tecnicos/novo")
    public String telaNovo(Model model) {
        model.addAttribute("tecnicoForm", new TecnicoForm());
        model.addAttribute("perfis", PerfilTecnico.values());
        return "tecnicos/form";
    }

    @PostMapping("/web/tecnicos")
    public String telaCriar(@Valid @ModelAttribute("tecnicoForm") TecnicoForm form,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("perfis", PerfilTecnico.values());
            return "tecnicos/form";
        }
        try {
            service.criar(form.toRequest());
            redirect.addFlashAttribute("sucesso", "Técnico cadastrado.");
            return "redirect:/web/tecnicos";
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("perfis", PerfilTecnico.values());
            return "tecnicos/form";
        }
    }
}
