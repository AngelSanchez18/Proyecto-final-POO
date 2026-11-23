package servicios;

// HERENCIA: Taller especializa a Curso como actividad práctica o extracurricular.
public class Taller extends Curso {
    public Taller(int id, String nombre, int capacidad) {
        super(id, nombre, capacidad);
    }

    // POLIMORFISMO: Sobreescritura con comportamiento propio de un taller.
    @Override
    public String describirCurso() {
        return "Taller práctico/extra: " + getNombre() + " (Cupo: " + getCapacidad() + ")";
    }
}
