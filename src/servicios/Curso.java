package servicios;

public abstract class Curso {
    private int id;
    private String nombre;
    private int capacidad;
    private boolean bloqueado;

    protected Curso(int id, String nombre, int capacidad) {
        this.id = id;
        this.nombre = nombre;
        this.capacidad = capacidad;
    }

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