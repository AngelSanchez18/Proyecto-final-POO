package personas;
import java.time.LocalDate;

public abstract class Persona{

    protected int ID;
    protected String nombre;
    protected String apellidos;
    protected LocalDate fechaNacimiento;
    protected String telefono;
    protected String direccion;


    public Persona(int ID, String nombre, String apellidos, LocalDate fechaNacimiento, String telefono, String direccion) {
        this.ID = ID;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        setTelefono(telefono);
        this.direccion = direccion;
    }

    public void consultarInformacion() {
        // Lógica para consultar la información de la persona
    }

    public void actualizarDatos() {
        // Lógica para actualizar los datos de la persona
    }

    // Getters y Setters (opcionales pero recomendados)
    public int getId() {
        return ID;
    }

    public void setId(int ID) {
        this.ID = ID;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (telefono == null || telefono.isEmpty() || telefono.length() != 10) {
            throw new IllegalArgumentException("Número de teléfono inválido. Debe contener 10 dígitos.");
        }
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
