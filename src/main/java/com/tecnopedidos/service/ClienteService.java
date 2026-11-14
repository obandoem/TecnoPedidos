package com.tecnopedidos.service;

import com.tecnopedidos.domain.Cliente;
import com.tecnopedidos.repository.ClienteRepository;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Reglas de negocio de clientes (HU-03 y HU-22)
@Service
public class ClienteService {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final ClienteRepository clienteRepository;
    private final EmpresaActualService empresaActual;

    public ClienteService(ClienteRepository clienteRepository,
                          EmpresaActualService empresaActual) {
        this.clienteRepository = clienteRepository;
        this.empresaActual = empresaActual;
    }

    @Transactional(readOnly = true)
    public List<Cliente> buscar(String texto) {
        String filtro = (texto == null || texto.isBlank()) ? null : texto.trim();
        return clienteRepository.buscar(empresaActual.getIdEmpresa(), filtro);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarActivos() {
        return clienteRepository
                .findByEmpresa_IdEmpresaAndActivoTrueOrderByNombreAsc(empresaActual.getIdEmpresa());
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository
                .findByIdClienteAndEmpresa_IdEmpresa(id, empresaActual.getIdEmpresa())
                .orElseThrow(() -> new IllegalArgumentException("El cliente no existe"));
    }

    // Registro (HU-03) y actualizacion (HU-22 esc. 2)
    @Transactional
    public Cliente guardar(Cliente datos) {

        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }

        if (datos.getIdentificacion() == null || datos.getIdentificacion().isBlank()) {
            throw new IllegalArgumentException("La identificacion del cliente es obligatoria");
        }

        // El correo es opcional, pero si se informa debe ser valido (HU-03 esc. 1)
        String correo = (datos.getCorreo() == null || datos.getCorreo().isBlank())
                ? null : datos.getCorreo().trim();

        if (correo != null && !CORREO.matcher(correo).matches()) {
            throw new IllegalArgumentException("El correo no tiene un formato valido");
        }

        String identificacion = datos.getIdentificacion().trim();

        // Identificacion unica dentro de la empresa (HU-03 esc. 2)
        var existente = clienteRepository.findByEmpresa_IdEmpresaAndIdentificacionIgnoreCase(
                empresaActual.getIdEmpresa(), identificacion);

        if (existente.isPresent()
                && !existente.get().getIdCliente().equals(datos.getIdCliente())) {
            throw new IllegalArgumentException("Ya existe un cliente con esa identificacion");
        }

        Cliente cliente;

        if (datos.getIdCliente() == null) {
            cliente = new Cliente();
            cliente.setEmpresa(empresaActual.getEmpresa());
            cliente.setActivo(true);
        } else {
            // Edicion: se conserva el identificador y la empresa (HU-22 esc. 2)
            cliente = buscarPorId(datos.getIdCliente());
        }

        cliente.setIdentificacion(identificacion);
        cliente.setNombre(datos.getNombre().trim());
        cliente.setCorreo(correo);
        cliente.setTelefono(datos.getTelefono());

        return clienteRepository.save(cliente);
    }
}
