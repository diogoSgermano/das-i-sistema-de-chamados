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

import br.com.sistemas.chamados.dto.form.ClienteForm;
import br.com.sistemas.chamados.dto.request.ClienteRequest;
import br.com.sistemas.chamados.dto.response.ChamadoResponse;
import br.com.sistemas.chamados.dto.response.ClienteResponse;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import jakarta.validation.Valid;

/**
 * Clientes: API REST em /clientes e telas HTML (Thymeleaf) em /web/clientes.
 */
@Controller
public class ClienteController {

    private final ClienteService service;
    private final ChamadoService chamadoService;

    public ClienteController(ClienteService service, ChamadoService chamadoService) {
        this.service = service;
        this.chamadoService = chamadoService;
    }

    // ------------------------------------------------------------------
    // API REST
    // ------------------------------------------------------------------

    @PostMapping("/clientes")
    @ResponseBody
    public ResponseEntity<ClienteResponse> criar(
            @Valid @RequestBody ClienteRequest dto) {
        ClienteResponse criado = service.criar(dto);
        return ResponseEntity.created(URI.create("/clientes/" + criado.id())).body(criado);
    }

    @GetMapping("/clientes")
    @ResponseBody
    public List<ClienteResponse> listar() {
        return service.listar();
    }

    @GetMapping("/clientes/{id}")
    @ResponseBody
    public ClienteResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/clientes/{id}")
    @ResponseBody
    public ClienteResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/clientes/{id}")
    @ResponseBody
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /clientes/{id}/chamados — lista chamados de um cliente (13.5) */
    @GetMapping("/clientes/{id}/chamados")
    @ResponseBody
    public List<ChamadoResponse> listarChamados(@PathVariable Long id) {
        return chamadoService.listarPorCliente(id);
    }

    // ------------------------------------------------------------------
    // Telas (Thymeleaf)
    // ------------------------------------------------------------------

    /** Tela 4: lista de clientes. */
    @GetMapping("/web/clientes")
    public String telaLista(Model model) {
        model.addAttribute("clientes", service.listar());
        return "clientes/lista";
    }

    @GetMapping("/web/clientes/novo")
    public String telaNovo(Model model) {
        model.addAttribute("clienteForm", new ClienteForm());
        return "clientes/form";
    }

    @PostMapping("/web/clientes")
    public String telaCriar(@Valid @ModelAttribute("clienteForm") ClienteForm form,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "clientes/form";
        }
        try {
            service.criar(form.toRequest());
            redirect.addFlashAttribute("sucesso", "Cliente cadastrado.");
            return "redirect:/web/clientes";
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "clientes/form";
        }
    }
}
