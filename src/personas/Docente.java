package personas;
import java.time.LocalDate;

public class Docente extends Persona {
    protected String especialidad;
    protected String correo;

    public Docente(int ID, String nombre, String apellidos, LocalDate fechaNacimiento, String telefono, String direccion, String especialidad, String correo) {
        super(ID, nombre, apellidos, fechaNacimiento, telefono, direccion);
        this.especialidad = especialidad;
        this.correo = correo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

}
