package servicios;

// CLASE ABSTRACTA: Define estructura común y obliga a las subclases a detallar su tipo de curso.
public abstract class Curso {
    private int id;
    private String nombre;
    private int capacidad;
    private boolean bloqueado;

    protected Curso(int id, String nombre, int capacidad) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del curso es obligatorio.");
        }
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad del curso debe ser mayor a cero.");
        }
        this.id = id;
        this.nombre = nombre;
        this.capacidad = capacidad;
    }

    public abstract String describirCurso();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        validarEditable();
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        validarEditable();
        this.nombre = nombre;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        validarEditable();
        this.capacidad = capacidad;
    }

    public void bloquear() {
        bloqueado = true;
    }

    private void validarEditable() {
        if (bloqueado) {
            throw new IllegalStateException("El curso pertenece a un ciclo terminado.");
        }
    }
}