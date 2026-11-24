package servicios;

import java.util.List;
import java.util.OptionalDouble;
import personas.Estudiante;

/** Copia de los resultados de un alumno en un ciclo; no cambia al registrar otras notas. */
public final class Boleta {
    private final int idCiclo;
    private final int anioEscolar;
    private final String nombreEstudiante;
    private final String matricula;
    private final int grado;
    private final char grupo;
    private final Turno turno;
    private final List<Calificacion> calificaciones;

    public Boleta(int idCiclo, int anioEscolar, Estudiante estudiante, Grupo grupo,
            List<Calificacion> calificaciones) {
        this(idCiclo, anioEscolar, estudiante.getNombre() + " " + estudiante.getApellidos(),
                estudiante.getMatricula(), grupo.getGrado(), grupo.getLetra(), grupo.getTurno(), calificaciones);
    }

    /** Reconstruye una boleta final sin consultar datos personales que pudieron cambiar. */
    public Boleta(int idCiclo, int anioEscolar, String nombreEstudiante, String matricula,
            int grado, char grupo, Turno turno, List<Calificacion> calificaciones) {
        this.idCiclo = idCiclo;
        this.anioEscolar = anioEscolar;
        this.nombreEstudiante = java.util.Objects.requireNonNull(nombreEstudiante);
        this.matricula = java.util.Objects.requireNonNull(matricula);
        this.grado = grado;
        this.grupo = grupo;
        this.turno = java.util.Objects.requireNonNull(turno);
        this.calificaciones = List.copyOf(calificaciones);
    }

    /** Las materias pendientes no se cuentan como ceros. */
    public OptionalDouble getPromedio() {
        return calificaciones.stream().filter(c -> c.getNota() != null)
                .mapToDouble(Calificacion::getNota).average();
    }

    public int getIdCiclo() { return idCiclo; }
    public int getAnioEscolar() { return anioEscolar; }
    public String getNombreEstudiante() { return nombreEstudiante; }
    public String getMatricula() { return matricula; }
    public int getGrado() { return grado; }
    public char getGrupo() { return grupo; }
    public Turno getTurno() { return turno; }
    public List<Calificacion> getCalificaciones() { return calificaciones; }

    @Override
    public String toString() {
        StringBuilder texto = new StringBuilder("BOLETA - Ciclo " + anioEscolar + "-" + (anioEscolar + 1)
                + "\nAlumno: " + nombreEstudiante + " | Matricula: " + matricula
                + "\nGrado: " + grado + " | Grupo: " + grupo + " | Turno: " + turno + "\n");
        for (Calificacion calificacion : calificaciones) {
            texto.append(calificacion.getNombreCurso()).append(" | Maestro: ")
                    .append(calificacion.getNombreDocente()).append(" | Nota: ")
                    .append(calificacion.getNota() == null ? "--" : calificacion.getNota())
                    .append(" | ").append(calificacion.getEstado()).append('\n');
        }
        texto.append("Promedio de cursos calificados: ")
                .append(getPromedio().isPresent() ? getPromedio().getAsDouble() : "--");
        return texto.toString();
    }
}
