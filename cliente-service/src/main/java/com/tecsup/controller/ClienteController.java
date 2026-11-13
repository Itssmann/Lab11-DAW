package com.tecsup.controller;
import java.util.List;
import java.net.URI;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import com.tecsup.dto.ClienteDTO;
import com.tecsup.model.Cliente;
import com.tecsup.services.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;
    public ClienteController(ClienteService service) { this.service = service; }
    @GetMapping public List<Cliente> listar() { return service.listar(); }
    @GetMapping("/{id}") public Cliente obtener(@PathVariable @Positive Long id) { return service.obtener(id); }
    @PostMapping public ResponseEntity<Cliente> crear(@Valid @RequestBody ClienteDTO dto) {
        Cliente entity = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/clientes/" + entity.getId())).body(entity);
    }
    @PutMapping("/{id}") public Cliente actualizar(@PathVariable @Positive Long id, @Valid @RequestBody ClienteDTO dto) { return service.actualizar(id, dto); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
