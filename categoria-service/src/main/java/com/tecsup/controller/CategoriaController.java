package com.tecsup.controller;
import java.util.List;
import java.net.URI;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import com.tecsup.dto.CategoriaDTO;
import com.tecsup.model.Categoria;
import com.tecsup.services.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaService service;
    public CategoriaController(CategoriaService service) { this.service = service; }
    @GetMapping public List<Categoria> listar() { return service.listar(); }
    @GetMapping("/{id}") public Categoria obtener(@PathVariable @Positive Long id) { return service.obtener(id); }
    @PostMapping public ResponseEntity<Categoria> crear(@Valid @RequestBody CategoriaDTO dto) {
        Categoria entity = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/categorias/" + entity.getId())).body(entity);
    }
    @PutMapping("/{id}") public Categoria actualizar(@PathVariable @Positive Long id, @Valid @RequestBody CategoriaDTO dto) { return service.actualizar(id, dto); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
