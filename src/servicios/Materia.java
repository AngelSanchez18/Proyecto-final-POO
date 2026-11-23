package servicios;

// HERENCIA: Materia especializa a Curso como asignatura curricular.
public class Materia extends Curso {
    public Materia(int id, String nombre, int capacidad) {
        super(id, nombre, capacidad);
    }

    // POLIMORFISMO: Sobreescritura con comportamiento propio de una materia curricular.
    @Override
    public String describirCurso() {
        return "Materia curricular: " + getNombre() + " (Cupo: " + getCapacidad() + ")";
    }
}
