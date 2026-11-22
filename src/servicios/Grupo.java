package servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import personas.Estudiante;

public class Grupo {
    private final int idGrupo;
    private final char letra;
    private final Turno turno;
    private final int capacidad;
    private final ProgramaEducativo programaEducativo;
    private final List<Estudiante> estudiantes = new ArrayList<>();
    private boolean bloqueado;

    public Grupo(int idGrupo, char letra, int capacidad, ProgramaEducativo programaEducativo) {
        this(idGrupo, letra, capacidad, programaEducativo, Turno.MATUTINO);
    }

    public Grupo(int idGrupo, char letra, int capacidad, ProgramaEducativo programaEducativo, Turno turno) {
        this.turno = Objects.requireNonNull(turno, "El turno es obligatorio.");
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        }
        this.idGrupo = idGrupo;
        this.letra = letra;
        this.capacidad = capacidad;
        this.programaEducativo = Objects.requireNonNull(programaEducativo, "El grupo necesita un programa educativo.");
    }

    public void agregarEstudiante(Estudiante estudiante) {
        validarEditable();
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
        validarEditable();
        return estudiantes.removeIf(estudiante -> estudiante.getId() == idEstudiante);
    }

    /** Impide cambiar la inscripcion o los cursos de un ciclo terminado. */
    public void bloquear() {
        bloqueado = true;
        programaEducativo.getCursos().forEach(Curso::bloquear);
    }

    private void validarEditable() {
        if (bloqueado) {
            throw new IllegalStateException("El grupo pertenece a un ciclo terminado.");
        }
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

    public Turno getTurno() {
        return turno;
    }

    public int getCapacidad() {
        return capacidad;
    }
}
