package servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import personas.Estudiante;

public class Grupo {
    private final int idGrupo;
    private final char letra;
    private final int capacidad;
    private final ProgramaEducativo programaEducativo;
    private final List<Estudiante> estudiantes = new ArrayList<>();

    public Grupo(int idGrupo, char letra, int capacidad, ProgramaEducativo programaEducativo) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        }
        this.idGrupo = idGrupo;
        this.letra = letra;
        this.capacidad = capacidad;
        this.programaEducativo = Objects.requireNonNull(programaEducativo, "El grupo necesita un programa educativo.");
    }

    public void agregarEstudiante(Estudiante estudiante) {
        Objects.requireNonNull(estudiante, "El estudiante no puede ser nulo.");
        for (Estudiante inscrito : estudiantes) {
            if (inscrito.getId() == estudiante.getId()) {
                throw new IllegalArgumentException("El estudiante ya pertenece al grupo.");
            }
        }
        if (estudiantes.size() >= capacidad) {
            throw new IllegalStateException("El grupo no tiene cupo disponible.");
        }
        estudiantes.add(estudiante);
    }

    public boolean retirarEstudiante(int idEstudiante) {
        return estudiantes.removeIf(estudiante -> estudiante.getId() == idEstudiante);
    }

    public List<Estudiante> consultarEstudiantes() {
        return List.copyOf(estudiantes);
    }

    public ProgramaEducativo getProgramaEducativo() {
        return programaEducativo;
    }

    public int getGrado() {
        return programaEducativo.getGrado();
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public char getLetra() {
        return letra;
    }

    public int getCapacidad() {
        return capacidad;
    }
}
