package com.tecsup.services;
import java.util.List;
import com.tecsup.dto.ClienteDTO;
import com.tecsup.model.Cliente;
import com.tecsup.repository.ClienteRepository;
import com.tecsup.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
public class ClienteService {
    private final ClienteRepository repo;
    public ClienteService(ClienteRepository repo) { this.repo = repo; }
    public List<Cliente> listar() { return repo.findAll(); }
    public Cliente obtener(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id)); }
    @Transactional
    public Cliente crear(ClienteDTO dto) { return repo.save(aplicar(new Cliente(), dto)); }
    @Transactional
    public Cliente actualizar(Long id, ClienteDTO dto) { return repo.save(aplicar(obtener(id), dto)); }
    @Transactional
    public void eliminar(Long id) { repo.delete(obtener(id)); }
    private Cliente aplicar(Cliente entity, ClienteDTO dto) {
        entity.setNombre(dto.nombre());
        entity.setApellidos(dto.apellidos());
        entity.setDireccion(dto.direccion());
        entity.setTelefono(dto.telefono());
        entity.setNroDni(dto.nroDni());
        entity.setEstado(dto.estado());
        return entity;
    }
}
