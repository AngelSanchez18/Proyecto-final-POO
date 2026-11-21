package ciclo;

import java.time.LocalDate;
import java.util.Objects;

public class Ciclo {
    private int idCiclo;
    private int anioEscolar;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;

    public Ciclo(int idCiclo, int anioEscolar, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        this.idCiclo = idCiclo;
        this.anioEscolar = anioEscolar;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }
//SET Y GET BASICOS
    public int getIdCiclo() {
        return idCiclo;
    }
//comentario generico
    public void setIdCiclo(int idCiclo) {
        if (idCiclo <= 0) {
            throw new IllegalArgumentException("EL ID debe ser mayor a 0");
        }
        this.idCiclo = idCiclo;
    }

    public int getAnioEscolar() {
        return anioEscolar;
    }

    public void setAnioEscolar(int anioEscolar) {
        if (anioEscolar > 2027 || anioEscolar < 2000) {
            throw new IllegalArgumentException("El anioEscolar es obligatorio.");
        }
        this.anioEscolar = anioEscolar;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "La fechaInicio es obligatoria.");
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = Objects.requireNonNull(fechaFin, "La fechaFin es obligatoria.");
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado.trim();
    }
}


