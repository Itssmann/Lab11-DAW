package com.tecsup.services;
import java.util.List;
import com.tecsup.dto.CategoriaDTO;
import com.tecsup.model.Categoria;
import com.tecsup.repository.CategoriaRepository;
import com.tecsup.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
public class CategoriaService {
    private final CategoriaRepository repo;
    public CategoriaService(CategoriaRepository repo) { this.repo = repo; }
    public List<Categoria> listar() { return repo.findAll(); }
    public Categoria obtener(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrado con id: " + id)); }
    @Transactional
    public Categoria crear(CategoriaDTO dto) { return repo.save(aplicar(new Categoria(), dto)); }
    @Transactional
    public Categoria actualizar(Long id, CategoriaDTO dto) { return repo.save(aplicar(obtener(id), dto)); }
    @Transactional
    public void eliminar(Long id) { repo.delete(obtener(id)); }
    private Categoria aplicar(Categoria entity, CategoriaDTO dto) {
        entity.setNombre(dto.nombre());
        entity.setEstado(dto.estado());
        return entity;
    }
}
