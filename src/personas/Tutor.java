package personas;
import java.time.LocalDate;

public class Tutor extends Persona{
    protected String parentesco;

    Tutor(int ID, String nombre, String apellidos, LocalDate fechaNacimiento, String telefono, String direccion, String parentesco) {
        super(ID, nombre, apellidos, fechaNacimiento, telefono, direccion);
        this.parentesco = parentesco;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }
}