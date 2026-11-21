package personas;
import java.time.LocalDate;

public class Estudiante extends Persona {
    private String matricula;
    private String curp;

    public Estudiante(int ID, String nombre, String apellidos, LocalDate fechaNacimiento, String telefono, String direccion, String matricula, String curp) {
        super(ID, nombre, apellidos, fechaNacimiento, telefono, direccion);
        this.matricula = matricula;
        this.curp = curp;
    }

    // Getters y Setters para matricula y curp
    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        if (matricula == null || matricula.isEmpty() || matricula.length() != 10) {
            throw new IllegalArgumentException("Matrícula inválida. No puede estar vacía o debe contar con 10 digitos.");
        }
        this.matricula = matricula;
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        if (curp == null || curp.length() != 18) {
            throw new IllegalArgumentException("CURP inválido. Debe tener 18 caracteres.");
        }
        this.curp = curp;
    }

}
