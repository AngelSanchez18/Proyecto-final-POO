package persistencia;

import ciclo.*;
import com.google.gson.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import personas.*;
import servicios.*;

/** Formato explicito versionado; carga mediante las validaciones del modelo. */
public final class CicloJson {
    private static final Gson GSON = new GsonBuilder().setStrictness(Strictness.STRICT)
            .setPrettyPrinting().serializeNulls().create();
    private CicloJson() { }

    private record PersonaDatos(int id, String nombre, String apellidos, String fechaNacimiento,
            String telefono, String direccion) {
        static PersonaDatos de(Persona p) {
            return new PersonaDatos(p.getId(), p.getNombre(), p.getApellidos(),
                    p.getFechaNacimiento().toString(), p.getTelefono(), p.getDireccion());
        }
    }
    private record MaestroDatos(PersonaDatos persona, String especialidad, String correo) { }
    private record AlumnoDatos(PersonaDatos persona, String matricula, String curp,
            List<ResultadoDatos> calificaciones) { }
    private record CursoDatos(int id, String tipo, String nombre, int capacidad, Integer idDocente) { }
    private record ResultadoDatos(int idCurso, String nombreCurso, Integer idDocente,
            String nombreDocente, Double nota, EstadoCalificacion estado) {
        static ResultadoDatos de(Calificacion c) {
            return new ResultadoDatos(c.getIdCurso(), c.getNombreCurso(), c.getIdDocente(),
                    c.getNombreDocente(), c.getNota(), c.getEstado());
        }
        Calificacion restaurar() {
            return new Calificacion(idCurso, nombreCurso, idDocente, nombreDocente, nota, estado);
        }
    }
    private record BoletaDatos(int idEstudiante, String nombreEstudiante, String matricula,
            int grado, String grupo, Turno turno, List<ResultadoDatos> calificaciones) { }
    private record GrupoDatos(int id, int grado, String letra, Turno turno, int capacidad,
            List<CursoDatos> cursos, List<AlumnoDatos> alumnos) { }
    private record Archivo(int version, int idCiclo, int anioEscolar, String fechaInicio,
            String fechaFin, EstadoCiclo estado, List<MaestroDatos> maestros, List<GrupoDatos> grupos,
            List<BoletaDatos> boletasFinales) { }

    public static void guardar(Ciclo ciclo, Path ruta) throws IOException {
        List<MaestroDatos> maestros = ciclo.getDepartamento().consultarDocentes().stream()
                .map(d -> new MaestroDatos(PersonaDatos.de(d), d.getEspecialidad(), d.getCorreo())).toList();
        List<GrupoDatos> grupos = new ArrayList<>();
        for (Grupo g : ciclo.consultarGrupos()) {
            List<CursoDatos> cursos = new ArrayList<>();
            for (Curso c : g.getProgramaEducativo().getCursos()) {
                Integer docente = null;
                try {
                    docente = ciclo.consultarDocente(g.getIdGrupo(), c.getId()).getId();
                } catch (IllegalStateException sinAsignacion) {
                    // Un curso pendiente puede no tener maestro todavia.
                }
                cursos.add(new CursoDatos(c.getId(), c instanceof Taller ? "TALLER" : "MATERIA",
                        c.getNombre(), c.getCapacidad(), docente));
            }
            List<AlumnoDatos> alumnos = new ArrayList<>();
            for (Estudiante e : g.consultarEstudiantes()) {
                List<ResultadoDatos> notas = ciclo.consultarCalificacionesRegistradas(e.getId()).stream()
                        .map(ResultadoDatos::de).toList();
                alumnos.add(new AlumnoDatos(PersonaDatos.de(e), e.getMatricula(), e.getCurp(), notas));
            }
            grupos.add(new GrupoDatos(g.getIdGrupo(), g.getGrado(), String.valueOf(g.getLetra()),
                    g.getTurno(), g.getCapacidad(), cursos, alumnos));
        }
        List<BoletaDatos> boletas = new ArrayList<>();
        ciclo.consultarBoletasFinales().forEach((id, b) -> boletas.add(new BoletaDatos(id,
                b.getNombreEstudiante(), b.getMatricula(), b.getGrado(), String.valueOf(b.getGrupo()),
                b.getTurno(), b.getCalificaciones().stream().map(ResultadoDatos::de).toList())));
        Archivo archivo = new Archivo(1, ciclo.getIdCiclo(), ciclo.getAnioEscolar(),
                ciclo.getFechaInicio().toString(), ciclo.getFechaFin().toString(), ciclo.getEstado(),
                maestros, grupos, boletas);
        Path destino = ruta.toAbsolutePath();
        Files.createDirectories(destino.getParent());
        // Primero escribe un temporal para no truncar un archivo valido si falla la escritura.
        Path temporal = Files.createTempFile(destino.getParent(), "ciclo-", ".tmp");
        try {
            Files.writeString(temporal, GSON.toJson(archivo), StandardCharsets.UTF_8);
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    public static Ciclo cargar(Path ruta) throws IOException {
        try {
            Archivo a = GSON.fromJson(Files.readString(ruta, StandardCharsets.UTF_8), Archivo.class);
            if (a == null || a.version != 1) {
                throw new IllegalArgumentException("Version de archivo no compatible.");
            }
            Objects.requireNonNull(a.estado, "Falta el estado del ciclo.");
            Departamento departamento = new Departamento();
            for (MaestroDatos m : a.maestros) {
                PersonaDatos p = m.persona;
                departamento.agregarDocente(new Docente(p.id, p.nombre, p.apellidos,
                        LocalDate.parse(p.fechaNacimiento), p.telefono, p.direccion, m.especialidad, m.correo));
            }
            List<Grupo> grupos = new ArrayList<>();
            for (GrupoDatos g : a.grupos) {
                List<Curso> cursos = new ArrayList<>();
                for (CursoDatos c : g.cursos) {
                    cursos.add(switch (c.tipo) {
                        case "MATERIA" -> new Materia(c.id, c.nombre, c.capacidad);
                        case "TALLER" -> new Taller(c.id, c.nombre, c.capacidad);
                        default -> throw new IllegalArgumentException("Tipo de curso desconocido: " + c.tipo);
                    });
                }
                if (g.letra.length() != 1) throw new IllegalArgumentException("Letra de grupo invalida.");
                grupos.add(new Grupo(g.id, g.letra.charAt(0), g.capacidad,
                        new ProgramaEducativo(g.grado, cursos), g.turno));
            }
            Ciclo ciclo = new Ciclo(a.idCiclo, a.anioEscolar, LocalDate.parse(a.fechaInicio),
                    LocalDate.parse(a.fechaFin), EstadoCiclo.CURSO, departamento, grupos);
            for (GrupoDatos g : a.grupos) {
                for (CursoDatos c : g.cursos) {
                    if (c.idDocente != null) ciclo.asignarDocente(g.id, c.id, c.idDocente);
                }
                for (AlumnoDatos e : g.alumnos) {
                    PersonaDatos p = e.persona;
                    ciclo.inscribirEstudiante(g.id, new Estudiante(p.id, p.nombre, p.apellidos,
                            LocalDate.parse(p.fechaNacimiento), p.telefono, p.direccion, e.matricula, e.curp));
                    Set<Integer> cursosCalificados = new HashSet<>();
                    for (ResultadoDatos r : e.calificaciones) {
                        if (!cursosCalificados.add(r.idCurso)) {
                            throw new IllegalArgumentException("Calificacion duplicada para el alumno " + p.id);
                        }
                        ciclo.restaurarCalificacion(p.id, r.restaurar());
                    }
                }
            }
            if (a.estado == EstadoCiclo.TERMINADO) {
                Map<Integer, Boleta> finales = new LinkedHashMap<>();
                for (BoletaDatos b : Objects.requireNonNull(a.boletasFinales, "Faltan las boletas finales.")) {
                    if (b.grupo.length() != 1 || finales.containsKey(b.idEstudiante)) {
                        throw new IllegalArgumentException("Boleta final invalida o duplicada.");
                    }
                    finales.put(b.idEstudiante, new Boleta(a.idCiclo, a.anioEscolar, b.nombreEstudiante,
                            b.matricula, b.grado, b.grupo.charAt(0), b.turno,
                            b.calificaciones.stream().map(ResultadoDatos::restaurar).toList()));
                }
                ciclo.terminarConBoletas(finales);
            }
            return ciclo;
        } catch (RuntimeException e) {
            throw new IOException("JSON de ciclo invalido: " + e.getMessage(), e);
        }
    }
}
