package servicios;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class ProgramaEducativo {
    private final int grado;
    private final List<Curso> cursos;

    public ProgramaEducativo(int grado, List<? extends Curso> cursos) {
        if (grado < 1 || grado > 3) {
            throw new IllegalArgumentException("El grado de secundaria debe ser 1, 2 o 3.");
        }
        this.grado = grado;
        this.cursos = List.copyOf(cursos);
        if (this.cursos.isEmpty()) {
            throw new IllegalArgumentException("El plan debe contener al menos un curso.");
        }
        Set<Integer> ids = new HashSet<>();
        for (Curso curso : this.cursos) {
            if (!ids.add(curso.getId())) {
                throw new IllegalArgumentException("No se puede repetir un curso en el plan.");
            }
        }
    }

    public int getGrado() {
        return grado;
    }

    public Curso buscarCurso(int idCurso) {
        return cursos.stream().filter(curso -> curso.getId() == idCurso).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El curso no pertenece al plan de estudios."));
    }

    public List<Curso> getCursos() {
        return cursos;
    }
}
