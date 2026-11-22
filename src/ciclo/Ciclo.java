package ciclo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import personas.Docente;
import personas.Estudiante;
import servicios.*;

/** Centraliza la operacion academica; el departamento puede compartirse entre ciclos. */
public class Ciclo {
    private int idCiclo;
    private int anioEscolar;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoCiclo estado = EstadoCiclo.CURSO;
    private Departamento departamento;
    private final Map<Integer, Grupo> grupos = new LinkedHashMap<>();
    private final Map<Integer, Map<Integer, Docente>> asignaciones = new LinkedHashMap<>();
    private final Map<Integer, Map<Integer, Calificacion>> calificaciones = new LinkedHashMap<>();

    private final Map<Integer, Boleta> boletasFinales = new LinkedHashMap<>();

    /** Permite configurar los tres grupos posteriormente mediante agregarGrupo. */
    public Ciclo(int idCiclo, int anioEscolar, LocalDate fechaInicio, LocalDate fechaFin, EstadoCiclo estado) {
        setIdCiclo(idCiclo);
        setAnioEscolar(anioEscolar);
        setFechaInicio(fechaInicio);
        setFechaFin(fechaFin);
        this.departamento = new Departamento();
        setEstado(estado);
    }

    public Ciclo(int idCiclo, int anioEscolar, LocalDate fechaInicio, LocalDate fechaFin,
            EstadoCiclo estado, Departamento departamento, List<Grupo> grupos) {
        this(idCiclo, anioEscolar, fechaInicio, fechaFin, EstadoCiclo.CURSO);
        Objects.requireNonNull(estado, "El estado es obligatorio.");
        this.departamento = Objects.requireNonNull(departamento, "El departamento es obligatorio.");
        if (grupos.size() != 3) {
            throw new IllegalArgumentException("El ciclo necesita un grupo por cada uno de los tres grados.");
        }
        grupos.forEach(this::agregarGrupo);
        setEstado(estado);
    }

    public void agregarGrupo(Grupo grupo) {
        validarEnCurso();
        Objects.requireNonNull(grupo, "El grupo es obligatorio.");
        if (grupos.containsKey(grupo.getIdGrupo())
                || grupos.values().stream().anyMatch(g -> g.getGrado() == grupo.getGrado())) {
            throw new IllegalArgumentException("Solo puede existir un grupo por grado y los IDs deben ser unicos.");
        }
        for (Estudiante estudiante : grupo.consultarEstudiantes()) {
            if (estaInscrito(estudiante.getId())) {
                throw new IllegalArgumentException("El alumno ya esta inscrito en otro grupo del ciclo.");
            }
        }
        grupos.put(grupo.getIdGrupo(), grupo);
        asignaciones.put(grupo.getIdGrupo(), new LinkedHashMap<>());
    }

    public Grupo buscarGrupo(int idGrupo) {
        Grupo grupo = grupos.get(idGrupo);
        if (grupo == null) {
            throw new IllegalArgumentException("El grupo no pertenece al ciclo.");
        }
        return grupo;
    }

    public List<Grupo> consultarGrupos() {
        return List.copyOf(grupos.values());
    }

    public Departamento getDepartamento() { return departamento; }

    public void inscribirEstudiante(int idGrupo, Estudiante estudiante) {
        validarEnCurso();
        Objects.requireNonNull(estudiante, "El estudiante es obligatorio.");
        Grupo grupo = buscarGrupo(idGrupo);
        if (estaInscrito(estudiante.getId())) {
            throw new IllegalArgumentException("El alumno ya esta inscrito en este ciclo.");
        }
        grupo.agregarEstudiante(estudiante);
    }

    private boolean estaInscrito(int idEstudiante) {
        return grupos.values().stream().anyMatch(g -> g.consultarEstudiantes().stream()
                .anyMatch(e -> e.getId() == idEstudiante));
    }

    private Grupo grupoDelEstudiante(int idEstudiante) {
        return grupos.values().stream().filter(g -> g.consultarEstudiantes().stream()
                .anyMatch(e -> e.getId() == idEstudiante)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El estudiante no esta inscrito en el ciclo."));
    }

    public void asignarDocente(int idGrupo, int idCurso, int idDocente) {
        validarEnCurso();
        Grupo grupo = buscarGrupo(idGrupo);
        grupo.getProgramaEducativo().buscarCurso(idCurso);
        asignaciones.get(idGrupo).put(idCurso, departamento.buscarDocente(idDocente));
    }

    public Docente consultarDocente(int idGrupo, int idCurso) {
        buscarGrupo(idGrupo).getProgramaEducativo().buscarCurso(idCurso);
        Docente docente = asignaciones.get(idGrupo).get(idCurso);
        if (docente == null) {
            throw new IllegalStateException("El curso todavia no tiene un docente asignado.");
        }
        return docente;
    }

    public void registrarCalificacion(int idEstudiante, int idCurso, double nota) {
        registrarCalificacion(idEstudiante, idCurso, nota,
                nota >= 6 ? EstadoCalificacion.APROBADO : EstadoCalificacion.REPROBADO);
    }

    /** EXTRAORDINARIO indica la modalidad del resultado; la nota conserva su valor numerico. */
    public void registrarCalificacion(int idEstudiante, int idCurso, Double nota, EstadoCalificacion estado) {
        validarEnCurso();
        Grupo grupo = grupoDelEstudiante(idEstudiante);
        Curso curso = grupo.getProgramaEducativo().buscarCurso(idCurso);
        Docente docente = consultarDocente(grupo.getIdGrupo(), idCurso);
        Calificacion calificacion = new Calificacion(curso, docente, nota, estado);
        calificaciones.computeIfAbsent(idEstudiante, id -> new LinkedHashMap<>()).put(idCurso, calificacion);
    }

    public Boleta generarBoleta(int idEstudiante) {
        if (estado == EstadoCiclo.TERMINADO) {
            Boleta boleta = boletasFinales.get(idEstudiante);
            if (boleta == null) {
                throw new IllegalArgumentException("El estudiante no esta inscrito en el ciclo.");
            }
            return boleta;
        }
        Grupo grupo = grupoDelEstudiante(idEstudiante);
        Estudiante estudiante = grupo.consultarEstudiantes().stream()
                .filter(e -> e.getId() == idEstudiante).findFirst().orElseThrow();
        List<Calificacion> resultados = new ArrayList<>();
        Map<Integer, Calificacion> registradas = calificaciones.getOrDefault(idEstudiante, Map.of());
        for (Curso curso : grupo.getProgramaEducativo().getCursos()) {
            Calificacion calificacion = registradas.get(curso.getId());
            if (calificacion == null) {
                calificacion = new Calificacion(curso, asignaciones.get(grupo.getIdGrupo()).get(curso.getId()),
                        null, EstadoCalificacion.NO_CALIFICADO);
            }
            resultados.add(calificacion);
        }
        return new Boleta(idCiclo, anioEscolar, estudiante, grupo, resultados);
    }

    public int getIdCiclo() { return idCiclo; }
    public void setIdCiclo(int idCiclo) {
        validarEnCurso();
        if (idCiclo <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a cero.");
        }
        this.idCiclo = idCiclo;
    }

    public int getAnioEscolar() { return anioEscolar; }
    public void setAnioEscolar(int anioEscolar) {
        validarEnCurso();
        if (anioEscolar < 2000) {
            throw new IllegalArgumentException("El anio escolar debe ser mayor o igual a 2000.");
        }
        this.anioEscolar = anioEscolar;
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) {
        validarEnCurso();
        Objects.requireNonNull(fechaInicio, "La fecha de inicio es obligatoria.");
        if (fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("El inicio no puede ser posterior al fin.");
        }
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) {
        validarEnCurso();
        Objects.requireNonNull(fechaFin, "La fecha de fin es obligatoria.");
        if (fechaInicio != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("El fin no puede ser anterior al inicio.");
        }
        this.fechaFin = fechaFin;
    }

    public EstadoCiclo getEstado() { return estado; }

    public void setEstado(EstadoCiclo estado) {
        Objects.requireNonNull(estado, "El estado es obligatorio.");
        validarEnCurso();
        if (estado == EstadoCiclo.TERMINADO) {
            terminar();
        }
    }

    /** El cierre es definitivo; las consultas y boletas siguen disponibles. */
    public void terminar() {
        validarEnCurso();
        Map<Integer, Boleta> finales = new LinkedHashMap<>();
        for (Grupo grupo : grupos.values()) {
            for (Estudiante estudiante : grupo.consultarEstudiantes()) {
                finales.put(estudiante.getId(), generarBoleta(estudiante.getId()));
            }
        }
        boletasFinales.putAll(finales);
        grupos.values().forEach(Grupo::bloquear);
        estado = EstadoCiclo.TERMINADO;
    }

    private void validarEnCurso() {
        if (estado == EstadoCiclo.TERMINADO) {
            throw new IllegalStateException("El ciclo esta terminado y no admite modificaciones.");
        }
    }
}
