package br.com.sistemas.chamados.controller;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.form.ChamadoForm;
import br.com.sistemas.chamados.dto.request.AlterarStatusRequest;
import br.com.sistemas.chamados.dto.request.AtribuirTecnicoRequest;
import br.com.sistemas.chamados.dto.request.ChamadoRequest;
import br.com.sistemas.chamados.dto.response.ChamadoResponse;
import br.com.sistemas.chamados.enums.Prioridade;
import br.com.sistemas.chamados.enums.StatusChamado;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.ComentarioService;
import br.com.sistemas.chamados.service.TecnicoService;
import jakarta.validation.Valid;

/**
 * Chamados: API REST em /chamados e telas HTML (Thymeleaf) em /web/chamados.
 */
@Controller
public class ChamadoController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Comentário já formatado para exibição na tela de detalhe. */
    public record ComentarioView(String autor, String texto, String data) {
    }

    private final ChamadoService service;
    private final ComentarioService comentarioService;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;

    public ChamadoController(ChamadoService service,
                             ComentarioService comentarioService,
                             ClienteService clienteService,
                             CategoriaService categoriaService,
                             TecnicoService tecnicoService) {
        this.service = service;
        this.comentarioService = comentarioService;
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
    }

    // ------------------------------------------------------------------
    // API REST
    // ------------------------------------------------------------------

    @PostMapping("/chamados")
    @ResponseBody
    public ResponseEntity<ChamadoResponse> criar(
            @Valid @RequestBody ChamadoRequest dto) {
        ChamadoResponse criado = service.criar(dto);
        return ResponseEntity
                .created(URI.create("/chamados/" + criado.idChamado()))
                .body(criado);
    }

    @GetMapping("/chamados")
    @ResponseBody
    public Page<ChamadoResponse> listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long clienteId,
            @PageableDefault(size = 20, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(status, prioridade, clienteId, pageable);
    }

    @GetMapping("/chamados/{id}")
    @ResponseBody
    public ChamadoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/chamados/{id}")
    @ResponseBody
    public ChamadoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ChamadoRequest dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/chamados/{id}")
    @ResponseBody
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/chamados/{id}/atribuir")
    @ResponseBody
    public ChamadoResponse atribuirTecnico(
            @PathVariable Long id,
            @Valid @RequestBody AtribuirTecnicoRequest dto) {
        return service.atribuirTecnico(id, dto);
    }

    @PatchMapping("/chamados/{id}/status")
    @ResponseBody
    public ChamadoResponse alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AlterarStatusRequest dto) {
        return service.alterarStatus(id, dto);
    }

    // ------------------------------------------------------------------
    // Telas (Thymeleaf)
    // ------------------------------------------------------------------

    @GetMapping("/")
    public String inicio() {
        return "redirect:/web/chamados";
    }

    /** Tela 1: lista de chamados. */
    @GetMapping("/web/chamados")
    public String telaLista(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<ChamadoResponse> chamados = service.listar(
                null, null, null,
                PageRequest.of(Math.max(page, 0), 20, Sort.by(Sort.Direction.DESC, "dataAbertura")));
        model.addAttribute("chamados", chamados);
        return "chamados/lista";
    }

    /** Tela 2: novo chamado. */
    @GetMapping("/web/chamados/novo")
    public String telaNovo(Model model) {
        model.addAttribute("chamadoForm", new ChamadoForm());
        carregarListas(model);
        return "chamados/form";
    }

    @PostMapping("/web/chamados")
    public String telaCriar(@Valid @ModelAttribute("chamadoForm") ChamadoForm form,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            carregarListas(model);
            return "chamados/form";
        }
        try {
            ChamadoResponse criado = service.criar(form.toRequest());
            redirect.addFlashAttribute("sucesso", "Chamado #" + criado.idChamado() + " aberto.");
            return "redirect:/web/chamados/" + criado.idChamado();
        } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            carregarListas(model);
            return "chamados/form";
        }
    }

    /** Tela 3: detalhe do chamado. */
    @GetMapping("/web/chamados/{id}")
    public String telaDetalhe(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            ChamadoResponse chamado = service.buscar(id);
            List<ComentarioView> comentarios = comentarioService.listarPorChamado(id).stream()
                    .map(c -> new ComentarioView(c.autor(), c.texto(), c.data().format(DATA)))
                    .toList();
            model.addAttribute("chamado", chamado);
            model.addAttribute("fechado", chamado.status() == StatusChamado.FECHADO);
            model.addAttribute("comentarios", comentarios);
            model.addAttribute("statusLista", StatusChamado.values());
            model.addAttribute("tecnicos", tecnicoService.listar());
            return "chamados/detalhe";
        } catch (RecursoNaoEncontradoException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/web/chamados";
        }
    }

    @PostMapping("/web/chamados/{id}/atualizar")
    public String telaAtualizar(@PathVariable Long id,
                                @RequestParam(required = false) StatusChamado status,
                                @RequestParam(required = false) Long tecnicoId,
                                RedirectAttributes redirect) {
        try {
            ChamadoResponse atual = service.buscar(id);
            // Atribuir técnico move o chamado de Aberto para Em atendimento (regra do service)
            if (tecnicoId != null && !tecnicoId.equals(atual.tecnicoId())) {
                service.atribuirTecnico(id, new AtribuirTecnicoRequest(tecnicoId));
            }
            if (status != null && status != atual.status()) {
                service.alterarStatus(id, new AlterarStatusRequest(status));
            }
            redirect.addFlashAttribute("sucesso", "Chamado atualizado.");
        } catch (RecursoNaoEncontradoException | RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/chamados/" + id;
    }

    private void carregarListas(Model model) {
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("categorias", categoriaService.listarTodas());
    }
}
