package com.tecsup.controller;
import java.util.List;
import java.net.URI;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import com.tecsup.dto.ProductoDTO;
import com.tecsup.model.Producto;
import com.tecsup.services.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService service;
    public ProductoController(ProductoService service) { this.service = service; }
    @GetMapping public List<Producto> listar() { return service.listar(); }
    @GetMapping("/{id}") public Producto obtener(@PathVariable @Positive Long id) { return service.obtener(id); }
    @PostMapping public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoDTO dto) {
        Producto entity = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/productos/" + entity.getId())).body(entity);
    }
    @PutMapping("/{id}") public Producto actualizar(@PathVariable @Positive Long id, @Valid @RequestBody ProductoDTO dto) { return service.actualizar(id, dto); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
