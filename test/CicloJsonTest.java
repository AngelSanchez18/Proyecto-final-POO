import ciclo.*;
import java.io.IOException;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistencia.CicloJson;
import servicios.*;
import static org.junit.jupiter.api.Assertions.*;

public class CicloJsonTest {
    @TempDir Path temporal;
    private Ciclo ejemplo() throws IOException {
        return CicloJson.cargar(Path.of("datos/ciclo-2026-prueba.json"));
    }

    @Test
    void cargaCuatroGruposConCursosMaestrosYNotas() throws IOException {
        Ciclo c = ejemplo();
        assertEquals(4, c.consultarGrupos().size());
        assertEquals(2, c.consultarGrupos().stream().filter(g -> g.getTurno() == Turno.MATUTINO).count());
        assertEquals(2, c.consultarGrupos().stream().filter(g -> g.getTurno() == Turno.VESPERTINO).count());
        assertEquals(4, c.getDepartamento().consultarDocentes().size());
        for (Grupo g : c.consultarGrupos()) {
            assertEquals(4, g.getProgramaEducativo().getCursos().size());
            assertInstanceOf(Taller.class, g.getProgramaEducativo().buscarCurso(4));
            g.getProgramaEducativo().getCursos().forEach(curso ->
                    assertNotNull(c.consultarDocente(g.getIdGrupo(), curso.getId())));
        }
        assertEquals(9, c.generarBoleta(101).getPromedio().orElseThrow());
        assertEquals(7.5, c.generarBoleta(102).getPromedio().orElseThrow());
        assertEquals(EstadoCalificacion.EXTRAORDINARIO,
                c.generarBoleta(103).getCalificaciones().get(1).getEstado());
        assertEquals(9.5, c.generarBoleta(104).getPromedio().orElseThrow());
    }

    @Test
    void guardarYCargarConservaBoletasYAsignacionesHistoricas() throws IOException {
        Ciclo original = ejemplo();
        original.asignarDocente(1, 1, 2);
        original.getDepartamento().buscarDocente(1).setNombre("Nombre cambiado");
        Path archivo = temporal.resolve("sub/ciclo.json");
        CicloJson.guardar(original, archivo);
        Ciclo copia = CicloJson.cargar(archivo);
        for (int id = 101; id <= 104; id++) {
            assertEquals(original.generarBoleta(id).toString(), copia.generarBoleta(id).toString());
        }
        assertEquals(2, copia.consultarDocente(1, 1).getId());
        assertEquals("Ana Lopez", copia.generarBoleta(101).getCalificaciones().get(0).getNombreDocente());
        assertEquals("Nombre cambiado", copia.getDepartamento().buscarDocente(1).getNombre());
        copia.registrarCalificacion(101, 1, 6);
        assertEquals(6, copia.generarBoleta(101).getCalificaciones().get(0).getNota());
    }

    @Test
    void conservaCierreYBloqueos() throws IOException {
        Ciclo c = ejemplo();
        c.terminar();
        Path archivo = temporal.resolve("cerrado.json");
        CicloJson.guardar(c, archivo);
        Ciclo copia = CicloJson.cargar(archivo);
        assertEquals(EstadoCiclo.TERMINADO, copia.getEstado());
        assertEquals(c.generarBoleta(101).toString(), copia.generarBoleta(101).toString());
        assertThrows(IllegalStateException.class, () -> copia.registrarCalificacion(101, 1, 8));
        assertThrows(IllegalStateException.class, () -> copia.buscarGrupo(1).retirarEstudiante(101));
    }

    @Test
    void conservaBoletaFinalAunqueCambienDatosPersonales() throws IOException {
        Ciclo c = ejemplo();
        c.terminar();
        String boleta = c.generarBoleta(101).toString();
        var alumno = c.buscarGrupo(1).consultarEstudiantes().get(0);
        alumno.setNombre("Otro nombre");
        alumno.setMatricula("9999999999");
        alumno.setId(999);
        Path archivo = temporal.resolve("historico.json");
        CicloJson.guardar(c, archivo);
        Ciclo copia = CicloJson.cargar(archivo);
        assertEquals(boleta, copia.generarBoleta(101).toString());
        assertEquals("Otro nombre", copia.buscarGrupo(1).consultarEstudiantes().get(0).getNombre());
    }

    @Test
    void cursoPendienteSigueReflejandoAsignacionesNuevas() throws IOException {
        Ciclo c = ejemplo();
        c.inscribirEstudiante(1, new personas.Estudiante(105, "Nuevo", "Alumno",
                java.time.LocalDate.of(2013, 1, 1), "1234567890", "Centro",
                "2026000105", "123456789012345678"));
        Path archivo = temporal.resolve("pendiente.json");
        CicloJson.guardar(c, archivo);
        Ciclo copia = CicloJson.cargar(archivo);
        copia.asignarDocente(1, 1, 2);
        assertEquals("Pedro Garcia", copia.generarBoleta(105).getCalificaciones().get(0).getNombreDocente());
        assertTrue(copia.generarBoleta(105).getPromedio().isEmpty());
    }

    @Test
    void rechazaJsonInvalidoYReferenciasInexistentes() throws IOException {
        Path archivo = temporal.resolve("malo.json");
        Files.writeString(archivo, "{invalido");
        assertThrows(IOException.class, () -> CicloJson.cargar(archivo));
        String json = Files.readString(Path.of("datos/ciclo-2026-prueba.json"));
        Files.writeString(archivo, json.replace("\"idDocente\": 1", "\"idDocente\": 999"));
        assertThrows(IOException.class, () -> CicloJson.cargar(archivo));
        Files.writeString(archivo, json.replace("\"nota\": 9", "\"nota\": 19"));
        assertThrows(IOException.class, () -> CicloJson.cargar(archivo));
    }
}
