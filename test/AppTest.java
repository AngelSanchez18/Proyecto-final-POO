import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistencia.CicloJson;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    @TempDir Path temporal;

    private String ejecutar(String instrucciones) {
        InputStream entradaOriginal = System.in;
        PrintStream salidaOriginal = System.out;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (PrintStream captura = new PrintStream(salida, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(instrucciones.getBytes(StandardCharsets.UTF_8)));
            System.setOut(captura);
            App.main(new String[0]);
            return salida.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(entradaOriginal);
            System.setOut(salidaOriginal);
        }
    }

    @Test
    void solicitaCicloAntesDelMenu() {
        String salida = ejecutar("2028\n0\n");
        assertTrue(salida.contains("Seleccione el ciclo escolar"));
        assertTrue(salida.contains("Ciclo 2028-2029 | Estado: CURSO"));
        assertFalse(salida.contains("Ciclo 2026-2027 | Estado:"));
        assertTrue(salida.indexOf("Anio de inicio") < salida.indexOf("1. Agregar maestro"));
    }

    @Test
    void permiteCorregirAnioInvalido() {
        String salida = ejecutar("texto\n1999\n999999999\n2027\n0\n");
        assertEquals(3, salida.split("No se pudo crear el ciclo:", -1).length - 1);
        assertTrue(salida.contains("Ciclo 2027-2028 | Estado: CURSO"));
    }

    @Test
    void conservaInscripcionesSoloEnElCicloSeleccionado() {
        String salida = ejecutar(String.join("\n",
                "2028", "2", "1", "10", "Luis", "Perez", "2013-01-01",
                "1234567890", "Centro", "1234567890", "123456789012345678",
                "7", "2028", "2029", "6", "8", "1", "6", "0", ""));
        assertTrue(salida.contains("Ya existe un ciclo para ese anio"));
        int nuevo = salida.indexOf("Ciclo 2029-2030 | Estado: CURSO");
        int anterior = salida.indexOf("Ciclo 2028-2029 | Estado: CURSO", nuevo);
        assertTrue(nuevo >= 0 && anterior > nuevo);
        assertFalse(salida.substring(nuevo, anterior).contains("Alumno 10: Luis Perez"));
        assertTrue(salida.substring(anterior).contains("Alumno 10: Luis Perez"));
    }

    @Test
    void cargaYGuardaDesdeElMenu() throws IOException {
        Path archivo = temporal.resolve("copia.json");
        String salida = ejecutar("json\ndatos/ciclo-2026-prueba.json\n5\n104\n10\n"
                + archivo + "\n0\n");
        assertTrue(salida.contains("Valeria Sanchez"));
        assertTrue(salida.contains("Ciclo guardado correctamente."));
        assertEquals(9.5, CicloJson.cargar(archivo).generarBoleta(104).getPromedio().orElseThrow());
    }
}
