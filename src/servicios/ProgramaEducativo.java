package servicios;

import java.util.List;

public class ProgramaEducativo {
    private final int grado;
    private final List<Curso> cursos;

    public ProgramaEducativo(int grado, List<? extends Curso> cursos) {
        if (grado < 1 || grado > 3) {
            throw new IllegalArgumentException("El grado de secundaria debe ser 1, 2 o 3.");
        }
        this.grado = grado;
        this.cursos = List.copyOf(cursos);
    }

    public int getGrado() {
        return grado;
    }

    public List<Curso> getCursos() {
        return cursos;
    }
}
