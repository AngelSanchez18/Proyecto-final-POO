import ciclo.Ciclo;
import ciclo.EstadoCiclo;
import java.time.LocalDate;
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
        Ciclo ciclo = crearCiclo(1, 2026, departamento);
        historial.add(ciclo);
        try (Scanner entrada = new Scanner(System.in)) {
            while (true) {
                System.out.println("\nCiclo " + ciclo.getAnioEscolar() + " | Estado: " + ciclo.getEstado()
                        + " | 1. Agregar maestro | 2. Inscribir alumno | 3. Asignar maestro"
                        + "\n4. Calificar | 5. Boleta | 6. Consultar grupos y maestros"
                        + "\n7. Nuevo ciclo | 8. Seleccionar ciclo anterior | 9. Terminar ciclo | 0. Salir");
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
                            int grupo = numero(entrada, "ID del grupo (1, 2 o 3)");
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
                            int anio = numero(entrada, "Anio de inicio del nuevo ciclo");
                            if (historial.stream().anyMatch(c -> c.getAnioEscolar() == anio)) {
                                throw new IllegalArgumentException("Ya existe un ciclo para ese anio.");
                            }
                            ciclo = crearCiclo(historial.size() + 1, anio, departamento);
                            historial.add(ciclo);
                            break;
                        case 8:
                            for (Ciclo c : historial) {
                                System.out.println("Ciclo " + c.getIdCiclo() + ": " + c.getAnioEscolar());
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
                        default:
                            System.out.println("Opcion no valida.");
                    }
                } catch (IllegalArgumentException | IllegalStateException | java.time.DateTimeException e) {
                    System.out.println("No se pudo realizar la operacion: " + e.getMessage());
                }
            }
        }
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
