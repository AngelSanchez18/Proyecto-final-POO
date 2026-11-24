import ciclo.Ciclo;
import ciclo.EstadoCiclo;
import java.time.LocalDate;
import java.nio.file.Path;
import java.io.IOException;
import persistencia.CicloJson;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import personas.Docente;
import personas.Estudiante;
import servicios.*;

public class App {
    public static void main(String[] args) {
        Departamento departamento = new Departamento();
        List<Ciclo> historial = new ArrayList<>();
        try (Scanner entrada = new Scanner(System.in)) {
            System.out.println("Seleccione el ciclo escolar en el que desea trabajar.");
            Ciclo ciclo = solicitarNuevoCiclo(entrada, historial, departamento);
            while (true) {
                departamento = ciclo.getDepartamento();
                System.out.println("\nCiclo " + periodo(ciclo) + " | Estado: " + ciclo.getEstado()
                        + " | 1. Agregar maestro | 2. Inscribir alumno | 3. Asignar maestro"
                        + "\n4. Calificar | 5. Boleta | 6. Consultar grupos y maestros"
                        + "\n7. Nuevo ciclo | 8. Seleccionar ciclo | 9. Terminar ciclo"
                        + "\n10. Guardar ciclo JSON | 11. Cargar ciclo JSON | 0. Salir");
                try {
                    int opcion = numero(entrada, "Opcion");
                    switch (opcion) {
                        case 0: return;
                        case 1:
                            departamento.agregarDocente(new Docente(numero(entrada, "ID del maestro"),
                                    texto(entrada, "Nombre"), texto(entrada, "Apellidos"),
                                    LocalDate.parse(texto(entrada, "Nacimiento (AAAA-MM-DD)")),
                                    texto(entrada, "Telefono (10 digitos)"), texto(entrada, "Direccion"),
                                    texto(entrada, "Especialidad"), texto(entrada, "Correo")));
                            break;
                        case 2:
                            int grupo = numero(entrada, "ID del grupo (consulte la opcion 6)");
                            ciclo.inscribirEstudiante(grupo, new Estudiante(numero(entrada, "ID del alumno"),
                                    texto(entrada, "Nombre"), texto(entrada, "Apellidos"),
                                    LocalDate.parse(texto(entrada, "Nacimiento (AAAA-MM-DD)")),
                                    texto(entrada, "Telefono (10 digitos)"), texto(entrada, "Direccion"),
                                    texto(entrada, "Matricula"), texto(entrada, "CURP")));
                            break;
                        case 3:
                            ciclo.asignarDocente(numero(entrada, "ID del grupo"),
                                    numero(entrada, "ID del curso"), numero(entrada, "ID del maestro"));
                            break;
                        case 4:
                            int alumno = numero(entrada, "ID del alumno");
                            int curso = numero(entrada, "ID del curso");
                            double nota = Double.parseDouble(texto(entrada, "Nota (0 a 10)"));
                            if (texto(entrada, "Es extraordinario? (s/n)").equalsIgnoreCase("s")) {
                                ciclo.registrarCalificacion(alumno, curso, nota, EstadoCalificacion.EXTRAORDINARIO);
                            } else {
                                ciclo.registrarCalificacion(alumno, curso, nota);
                            }
                            break;
                        case 5:
                            System.out.println(ciclo.generarBoleta(numero(entrada, "ID del alumno")));
                            break;
                        case 6:
                            for (Grupo g : ciclo.consultarGrupos()) {
                                System.out.println("Grupo ID " + g.getIdGrupo() + " | Grado " + g.getGrado()
                                        + g.getLetra() + " | " + g.getTurno()
                                        + " | Alumnos: " + g.consultarEstudiantes().size());
                                for (Curso c : g.getProgramaEducativo().getCursos()) {
                                    System.out.println("  Curso " + c.getId() + ": " + c.getNombre());
                                }
                                for (Estudiante e : g.consultarEstudiantes()) {
                                    System.out.println("  Alumno " + e.getId() + ": " + e.getNombre() + " " + e.getApellidos());
                                }
                            }
                            for (Docente d : departamento.consultarDocentes()) {
                                System.out.println("Maestro " + d.getId() + ": " + d.getNombre() + " " + d.getApellidos());
                            }
                            break;
                        case 7:
                            ciclo = solicitarNuevoCiclo(entrada, historial, departamento);
                            break;
                        case 8:
                            for (Ciclo c : historial) {
                                System.out.println("Ciclo " + c.getIdCiclo() + ": " + periodo(c)
                                        + " | Estado: " + c.getEstado());
                            }
                            int id = numero(entrada, "ID del ciclo a seleccionar");
                            ciclo = historial.stream().filter(c -> c.getIdCiclo() == id).findFirst()
                                    .orElseThrow(() -> new IllegalArgumentException("Ciclo inexistente."));
                            break;
                        case 9:
                            if (texto(entrada, "Terminar el ciclo definitivamente? (s/n)").equalsIgnoreCase("s")) {
                                ciclo.terminar();
                                System.out.println("Ciclo terminado. Solo se permiten consultas.");
                            }
                            break;
                        case 10:
                            CicloJson.guardar(ciclo, Path.of(texto(entrada, "Ruta del archivo JSON a guardar")));
                            System.out.println("Ciclo guardado correctamente.");
                            break;
                        case 11:
                            Ciclo cargado = CicloJson.cargar(Path.of(texto(entrada, "Ruta del archivo JSON a cargar")));
                            boolean conflicto = historial.stream().anyMatch(c -> c.getIdCiclo() == cargado.getIdCiclo()
                                    || c.getAnioEscolar() == cargado.getAnioEscolar());
                            if (conflicto) {
                                if (!texto(entrada, "Ya existe ese ciclo o ID. Reemplazar en memoria? (s/n)")
                                        .equalsIgnoreCase("s")) break;
                                historial.removeIf(c -> c.getIdCiclo() == cargado.getIdCiclo()
                                        || c.getAnioEscolar() == cargado.getAnioEscolar());
                            }
                            historial.add(cargado);
                            ciclo = cargado;
                            System.out.println("Ciclo cargado correctamente.");
                            break;
                        default:
                            System.out.println("Opcion no valida.");
                    }
                } catch (IOException | IllegalArgumentException | IllegalStateException | java.time.DateTimeException e) {
                    System.out.println("No se pudo realizar la operacion: " + e.getMessage());
                }
            }
        }
    }

    private static Ciclo solicitarNuevoCiclo(Scanner entrada, List<Ciclo> historial,
            Departamento departamento) {
        while (true) {
            try {
                String seleccion = texto(entrada,
                        "Anio de inicio del ciclo escolar (ejemplo: 2026 para 2026-2027), o 'json' para cargar");
                if (seleccion.equalsIgnoreCase("json")) {
                    Ciclo cargado = CicloJson.cargar(Path.of(texto(entrada, "Ruta del archivo JSON a cargar")));
                    if (historial.stream().anyMatch(c -> c.getIdCiclo() == cargado.getIdCiclo()
                            || c.getAnioEscolar() == cargado.getAnioEscolar())) {
                        throw new IllegalArgumentException("El ciclo o ID ya existe. Use la opcion 11 para reemplazarlo.");
                    }
                    historial.add(cargado);
                    return cargado;
                }
                int anio = Integer.parseInt(seleccion);
                if (historial.stream().anyMatch(c -> c.getAnioEscolar() == anio)) {
                    throw new IllegalArgumentException("Ya existe un ciclo para ese anio. Use Seleccionar ciclo.");
                }
                int id = historial.stream().mapToInt(Ciclo::getIdCiclo).max().orElse(0) + 1;
                Ciclo ciclo = crearCiclo(id, anio, departamento);
                historial.add(ciclo);
                return ciclo;
            } catch (IOException | IllegalArgumentException | java.time.DateTimeException e) {
                System.out.println("No se pudo crear el ciclo: " + e.getMessage());
            }
        }
    }

    private static String periodo(Ciclo ciclo) {
        return ciclo.getAnioEscolar() + "-" + ciclo.getFechaFin().getYear();
    }

    /** Planes iniciales de ejemplo: pueden sustituirse por los de la escuela. */
    private static Ciclo crearCiclo(int id, int anio, Departamento departamento) {
        List<Grupo> grupos = new ArrayList<>();
        for (int grado = 1; grado <= 3; grado++) {
            ProgramaEducativo plan = new ProgramaEducativo(grado, List.of(
                    new Materia(1, "Espanol " + grado, 30),
                    new Materia(2, "Matematicas " + grado, 30),
                    new Materia(3, "Ciencias " + grado, 30),
                    new Taller(4, "Tecnologia " + grado, 30)));
            grupos.add(new Grupo(grado, 'A', 30, plan, Turno.MATUTINO));
        }
        return new Ciclo(id, anio, LocalDate.of(anio, 8, 1), LocalDate.of(anio + 1, 7, 31),
                EstadoCiclo.CURSO, departamento, grupos);
    }

    private static String texto(Scanner entrada, String mensaje) {
        System.out.print(mensaje + ": ");
        return entrada.nextLine().trim();
    }

    private static int numero(Scanner entrada, String mensaje) {
        return Integer.parseInt(texto(entrada, mensaje));
    }
}
