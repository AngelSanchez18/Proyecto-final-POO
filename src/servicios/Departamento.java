package servicios;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import personas.Docente;

// Catalogo de docentes compartido por varios ciclos escolares.
public class Departamento {
    private final Map<Integer, Docente> docentes = new LinkedHashMap<>();

    public void agregarDocente(Docente docente) {
        Objects.requireNonNull(docente, "El docente es obligatorio.");
        if (docentes.putIfAbsent(docente.getId(), docente) != null) {
            throw new IllegalArgumentException("Ya existe un docente con ese ID.");
        }
    }

    public Docente buscarDocente(int idDocente) {
        Docente docente = docentes.get(idDocente);
        if (docente == null) {
            throw new IllegalArgumentException("El docente no pertenece al departamento.");
        }
        return docente;
    }

    public List<Docente> consultarDocentes() {
        return List.copyOf(docentes.values());
    }
}
