package servicios;

import java.util.Objects;
import personas.Docente;

/** Resultado inmutable: conserva el curso y el maestro al momento de calificar. */
public final class Calificacion {
    private final int idCurso;
    private final String nombreCurso;
    private final Integer idDocente;
    private final String nombreDocente;
    private final Double nota;
    private final EstadoCalificacion estado;

    public Calificacion(Curso curso, Docente docente, Double nota, EstadoCalificacion estado) {
        this(Objects.requireNonNull(curso, "El curso es obligatorio.").getId(), curso.getNombre(),
                docente == null ? null : docente.getId(),
                docente == null ? "Sin asignar" : docente.getNombre() + " " + docente.getApellidos(), nota, estado);
    }

    /** Reconstruye los datos historicos conservados en un archivo. */
    public Calificacion(int idCurso, String nombreCurso, Integer idDocente, String nombreDocente,
            Double nota, EstadoCalificacion estado) {
        this.estado = Objects.requireNonNull(estado, "El estado es obligatorio.");
        if (estado == EstadoCalificacion.NO_CALIFICADO) {
            if (nota != null) {
                throw new IllegalArgumentException("Un curso no calificado no puede tener nota.");
            }
        } else {
            Objects.requireNonNull(idDocente, "Se necesita el maestro que impartio el curso.");
            if (nota == null || !Double.isFinite(nota) || nota < 0 || nota > 10) {
                throw new IllegalArgumentException("La nota debe estar entre 0 y 10.");
            }
            if ((estado == EstadoCalificacion.APROBADO && nota < 6)
                    || (estado == EstadoCalificacion.REPROBADO && nota >= 6)) {
                throw new IllegalArgumentException("La nota no corresponde al estado.");
            }
        }
        this.idCurso = idCurso;
        this.nombreCurso = Objects.requireNonNull(nombreCurso, "El nombre del curso es obligatorio.");
        this.idDocente = idDocente;
        this.nombreDocente = Objects.requireNonNull(nombreDocente, "El nombre del maestro es obligatorio.");
        this.nota = nota;
    }

    public int getIdCurso() { return idCurso; }
    public String getNombreCurso() { return nombreCurso; }
    public Integer getIdDocente() { return idDocente; }
    public String getNombreDocente() { return nombreDocente; }
    public Double getNota() { return nota; }
    public EstadoCalificacion getEstado() { return estado; }
}
