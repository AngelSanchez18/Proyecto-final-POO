package test;

import ciclo.Ciclo;
import ciclo.EstadoCiclo;
import java.time.LocalDate;
import java.util.List;
import personas.Docente;
import personas.Estudiante;
import servicios.*;
import static org.junit.jupiter.api.Assertions.*;

public class Test {
    private Departamento departamento() {
        Departamento departamento = new Departamento();
        departamento.agregarDocente(docente(1, "Ana"));
        return departamento;
    }

    private Docente docente(int id, String nombre) {
        return new Docente(id, nombre, "Lopez", LocalDate.of(1980, 1, 1),
                "1234567890", "Centro", "Matematicas", "docente@escuela.mx");
    }

    private Estudiante alumno(int id) {
        return new Estudiante(id, "Luis", "Perez", LocalDate.of(2013, 1, 1),
                "1234567890", "Centro", "1234567890", "123456789012345678");
    }

    private Grupo grupo(int id, int grado) {
        return new Grupo(id, 'A', 1, new ProgramaEducativo(grado,
                List.of(new Materia(1, "Matematicas", 30), new Taller(2, "Tecnologia", 30))), Turno.MATUTINO);
    }

    private Ciclo ciclo(Departamento departamento) {
        return new Ciclo(1, 2026, LocalDate.of(2026, 8, 1), LocalDate.of(2027, 7, 31),
                EstadoCiclo.CURSO, departamento, List.of(grupo(1, 1), grupo(2, 2), grupo(3, 3)));
    }

    @org.junit.jupiter.api.Test
    void exigeTresGradosUnicos() {
        Departamento departamento = departamento();
        assertThrows(IllegalArgumentException.class, () -> new Ciclo(1, 2026,
                LocalDate.of(2026, 8, 1), LocalDate.of(2027, 7, 31), EstadoCiclo.CURSO, departamento,
                List.of(grupo(1, 1), grupo(2, 1), grupo(3, 3))));
        assertThrows(IllegalArgumentException.class, () -> new Ciclo(1, 2026,
                LocalDate.of(2026, 8, 1), LocalDate.of(2027, 7, 31), EstadoCiclo.CURSO, departamento,
                List.of(grupo(1, 1))));
        assertEquals(3, ciclo(departamento).consultarGrupos().size());
    }

    @org.junit.jupiter.api.Test
    void validaInscripcionYCupo() {
        Ciclo ciclo = ciclo(departamento());
        ciclo.inscribirEstudiante(1, alumno(10));
        assertThrows(IllegalArgumentException.class, () -> ciclo.inscribirEstudiante(2, alumno(10)));
        assertThrows(IllegalStateException.class, () -> ciclo.inscribirEstudiante(1, alumno(11)));
        assertThrows(UnsupportedOperationException.class, () -> ciclo.consultarGrupos().clear());
    }

    @org.junit.jupiter.api.Test
    void validaDocenteCursoYAlumno() {
        Ciclo ciclo = ciclo(departamento());
        ciclo.inscribirEstudiante(1, alumno(10));
        assertThrows(IllegalArgumentException.class, () -> ciclo.asignarDocente(1, 1, 99));
        assertThrows(IllegalArgumentException.class, () -> ciclo.asignarDocente(1, 99, 1));
        assertThrows(IllegalStateException.class, () -> ciclo.registrarCalificacion(10, 1, 9));
        ciclo.asignarDocente(1, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> ciclo.registrarCalificacion(99, 1, 9));
        assertThrows(IllegalArgumentException.class, () -> ciclo.registrarCalificacion(10, 99, 9));
        assertThrows(IllegalArgumentException.class, () -> ciclo.registrarCalificacion(10, 1, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> ciclo.registrarCalificacion(10, 1, 11));
        assertThrows(IllegalArgumentException.class, () -> ciclo.registrarCalificacion(10, 1, 5.0, EstadoCalificacion.APROBADO));
    }

    @org.junit.jupiter.api.Test
    void boletaIncluyePendientesMaestroYPromedio() {
        Ciclo ciclo = ciclo(departamento());
        ciclo.inscribirEstudiante(1, alumno(10));
        Boleta pendiente = ciclo.generarBoleta(10);
        assertTrue(pendiente.getPromedio().isEmpty());
        assertEquals(EstadoCalificacion.NO_CALIFICADO, pendiente.getCalificaciones().get(0).getEstado());
        assertEquals("Sin asignar", pendiente.getCalificaciones().get(0).getNombreDocente());
        ciclo.asignarDocente(1, 1, 1);
        ciclo.registrarCalificacion(10, 1, 6);
        Boleta boleta = ciclo.generarBoleta(10);
        assertEquals(2, boleta.getCalificaciones().size());
        assertEquals("Ana Lopez", boleta.getCalificaciones().get(0).getNombreDocente());
        assertEquals(EstadoCalificacion.APROBADO, boleta.getCalificaciones().get(0).getEstado());
        assertEquals(6.0, boleta.getPromedio().orElseThrow());
        assertTrue(pendiente.getPromedio().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> boleta.getCalificaciones().clear());
    }

    @org.junit.jupiter.api.Test
    void reprobadoYExtraordinario() {
        Ciclo ciclo = ciclo(departamento());
        ciclo.inscribirEstudiante(1, alumno(10));
        ciclo.asignarDocente(1, 1, 1);
        ciclo.registrarCalificacion(10, 1, 5);
        Boleta reprobada = ciclo.generarBoleta(10);
        assertEquals(EstadoCalificacion.REPROBADO, reprobada.getCalificaciones().get(0).getEstado());
        ciclo.registrarCalificacion(10, 1, 8.0, EstadoCalificacion.EXTRAORDINARIO);
        assertEquals(EstadoCalificacion.EXTRAORDINARIO, ciclo.generarBoleta(10).getCalificaciones().get(0).getEstado());
        assertEquals(5.0, reprobada.getPromedio().orElseThrow());
    }

    @org.junit.jupiter.api.Test
    void conservaMaestroOriginalYComparteDepartamentoSinCompartirResultados() {
        Departamento departamento = departamento();
        Ciclo anterior = ciclo(departamento);
        anterior.inscribirEstudiante(1, alumno(10));
        anterior.asignarDocente(1, 1, 1);
        anterior.registrarCalificacion(10, 1, 9);
        departamento.agregarDocente(docente(2, "Pedro"));
        anterior.asignarDocente(1, 1, 2);
        assertEquals("Ana Lopez", anterior.generarBoleta(10).getCalificaciones().get(0).getNombreDocente());
        Ciclo siguiente = new Ciclo(2, 2027, LocalDate.of(2027, 8, 1), LocalDate.of(2028, 7, 31),
                EstadoCiclo.CURSO, departamento, List.of(grupo(1, 1), grupo(2, 2), grupo(3, 3)));
        siguiente.inscribirEstudiante(2, alumno(10));
        assertSame(anterior.getDepartamento(), siguiente.getDepartamento());
        assertEquals(2, siguiente.getDepartamento().consultarDocentes().size());
        assertTrue(siguiente.generarBoleta(10).getPromedio().isEmpty());
        assertThrows(IllegalStateException.class, () -> siguiente.consultarDocente(2, 1));
    }

    @org.junit.jupiter.api.Test
    void tallerEsCursoExtraConMaestroYNotaEnBoleta() {
        Ciclo ciclo = ciclo(departamento());
        ciclo.inscribirEstudiante(1, alumno(10));
        ciclo.asignarDocente(1, 1, 1);
        ciclo.asignarDocente(1, 2, 1);
        ciclo.registrarCalificacion(10, 1, 8);
        ciclo.registrarCalificacion(10, 2, 10);
        Boleta boleta = ciclo.generarBoleta(10);
        Calificacion taller = boleta.getCalificaciones().get(1);
        assertEquals("Tecnologia", taller.getNombreCurso());
        assertEquals("Ana Lopez", taller.getNombreDocente());
        assertEquals(10.0, taller.getNota());
        assertEquals(EstadoCalificacion.APROBADO, taller.getEstado());
        assertEquals(9.0, boleta.getPromedio().orElseThrow());
        assertTrue(boleta.toString().contains("Tecnologia | Maestro: Ana Lopez"));
    }

    @org.junit.jupiter.api.Test
    void permiteModificarEnCursoYBloqueaAlTerminar() {
        Departamento departamento = departamento();
        Ciclo ciclo = ciclo(departamento);
        assertEquals(EstadoCiclo.CURSO, ciclo.getEstado());
        ciclo.setAnioEscolar(2027);
        ciclo.setIdCiclo(5);
        ciclo.setFechaFin(LocalDate.of(2027, 8, 1));
        ciclo.inscribirEstudiante(1, alumno(10));
        ciclo.asignarDocente(1, 2, 1);
        ciclo.registrarCalificacion(10, 2, 8);
        ciclo.registrarCalificacion(10, 2, 9);
        ciclo.setEstado(EstadoCiclo.TERMINADO);
        assertEquals(EstadoCiclo.TERMINADO, ciclo.getEstado());
        assertEquals(9.0, ciclo.generarBoleta(10).getPromedio().orElseThrow());
        assertNotNull(ciclo.consultarDocente(1, 2));
        assertEquals(3, ciclo.consultarGrupos().size());
        assertThrows(IllegalStateException.class, () -> ciclo.inscribirEstudiante(2, alumno(11)));
        assertThrows(IllegalStateException.class, () -> ciclo.agregarGrupo(grupo(4, 1)));
        assertThrows(IllegalStateException.class, () -> ciclo.asignarDocente(1, 2, 1));
        assertThrows(IllegalStateException.class, () -> ciclo.registrarCalificacion(10, 2, 7));
        assertThrows(IllegalStateException.class, () -> ciclo.registrarCalificacion(10, 2, 7.0, EstadoCalificacion.EXTRAORDINARIO));
        assertThrows(IllegalStateException.class, () -> ciclo.setIdCiclo(6));
        assertThrows(IllegalStateException.class, () -> ciclo.setAnioEscolar(2028));
        assertThrows(IllegalStateException.class, () -> ciclo.setFechaInicio(LocalDate.of(2026, 9, 1)));
        assertThrows(IllegalStateException.class, () -> ciclo.setFechaFin(LocalDate.of(2027, 9, 1)));
        assertThrows(IllegalStateException.class, () -> ciclo.setEstado(EstadoCiclo.CURSO));
        assertThrows(IllegalStateException.class, ciclo::terminar);
        assertThrows(IllegalStateException.class, () -> ciclo.buscarGrupo(1).retirarEstudiante(10));
        assertThrows(IllegalStateException.class, () -> ciclo.buscarGrupo(2).agregarEstudiante(alumno(11)));
        Curso taller = ciclo.buscarGrupo(1).getProgramaEducativo().buscarCurso(2);
        assertThrows(IllegalStateException.class, () -> taller.setNombre("Otro"));
        assertThrows(IllegalStateException.class, () -> taller.setId(20));
        assertThrows(IllegalStateException.class, () -> taller.setCapacidad(40));
        departamento.agregarDocente(docente(2, "Pedro"));
        assertEquals(2, departamento.consultarDocentes().size());
    }

    @org.junit.jupiter.api.Test
    void cierreConservaBoletasAunqueCambienDatosPersonalesCompartidos() {
        Departamento departamento = departamento();
        Ciclo ciclo = ciclo(departamento);
        Estudiante estudiante = alumno(10);
        ciclo.inscribirEstudiante(1, estudiante);
        ciclo.asignarDocente(1, 2, 1);
        ciclo.terminar();
        estudiante.setNombre("Otro nombre");
        estudiante.setId(99);
        departamento.buscarDocente(1).setNombre("Otro maestro");
        Boleta boleta = ciclo.generarBoleta(10);
        assertEquals("Luis Perez", boleta.getNombreEstudiante());
        assertEquals("Ana Lopez", boleta.getCalificaciones().get(1).getNombreDocente());
        assertEquals(EstadoCalificacion.NO_CALIFICADO, boleta.getCalificaciones().get(1).getEstado());
        assertThrows(IllegalArgumentException.class, () -> ciclo.generarBoleta(99));
    }

    @org.junit.jupiter.api.Test
    void constructorAceptaTerminadoYRechazaEstadoNulo() {
        Ciclo terminado = new Ciclo(1, 2026, LocalDate.of(2026, 8, 1), LocalDate.of(2027, 7, 31),
                EstadoCiclo.TERMINADO, departamento(), List.of(grupo(1, 1), grupo(2, 2), grupo(3, 3)));
        assertEquals(EstadoCiclo.TERMINADO, terminado.getEstado());
        assertEquals(3, terminado.consultarGrupos().size());
        assertThrows(IllegalStateException.class, () -> terminado.inscribirEstudiante(1, alumno(10)));
        assertThrows(NullPointerException.class, () -> ciclo(departamento()).setEstado(null));
    }

    @org.junit.jupiter.api.Test
    void rechazaDuplicadosYFechasInvertidas() {
        Departamento departamento = departamento();
        assertThrows(IllegalArgumentException.class, () -> departamento.agregarDocente(docente(1, "Otra")));
        assertThrows(IllegalArgumentException.class, () -> new ProgramaEducativo(1,
                List.of(new Materia(1, "Uno", 30), new Taller(1, "Dos", 30))));
        assertThrows(IllegalArgumentException.class, () -> new Ciclo(1, 2026,
                LocalDate.of(2027, 8, 1), LocalDate.of(2026, 7, 31), EstadoCiclo.CURSO));
    }
}
