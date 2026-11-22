# Sistema de Gestión Escolar - Escuela Secundaria

## Descripción del Problema
Las escuelas secundarias públicas requieren un control organizado y confiable de su operación académica, pero muchas veces la administración de estudiantes, tutores, docentes, grupos por turno, ciclos escolares y calificaciones se lleva de forma dispersa o manual. Esto dificulta el seguimiento académico, el cálculo oportuno de promedios y la emisión ágil de documentos oficiales como boletas y constancias.

Este sistema resuelve esa problemática mediante una aplicación desarrollada en **Java** bajo el paradigma de **Programación Orientada a Objetos (POO)**, centralizando la gestión académica y facilitando la consulta histórica de información entre ciclos escolares.

---

## Lista de Funcionalidades
* Tres grados de secundaria, con un grupo, un turno y un plan de estudios por grado.
* Inscripción de alumnos con validación de cupo e inscripción única dentro del ciclo.
* Catálogo de maestros en `servicios.Departamento`, compartido entre ciclos escolares.
* Asignación de maestros a materias y talleres de cada grupo.
* Calificaciones de 0 a 10: `NO_CALIFICADO`, `REPROBADO`, `APROBADO` y `EXTRAORDINARIO`.
* Boletas con alumno, matrícula, grado, grupo, turno, cursos, maestros, notas y promedio.
* Menú de consola para gestionar la escuela y seleccionar ciclos anteriores.
* Estado del ciclo con `EstadoCiclo.CURSO` y `EstadoCiclo.TERMINADO`; cierre definitivo desde el menú.

### Organización y reglas
`App` mantiene un departamento y el historial de ciclos. Cada `ciclo.Ciclo` administra sus
propios grupos, inscripciones, asignaciones y resultados. Los planes se representan con
`ProgramaEducativo`; admiten tanto `Materia` como `Taller`. El taller es un curso extra
con su propio maestro, nota y renglón en la boleta; su nota también participa en el promedio.
En los planes de ejemplo de `App`, el taller de Tecnología tiene el ID 4: se asigna y se
califica con las mismas opciones que las materias.

El constructor completo de `Ciclo` exige los tres grados. El constructor anterior se conserva
para configurar los grupos posteriormente con `agregarGrupo`. Para mantener la inscripción
única entre grupos, las inscripciones deben hacerse mediante `Ciclo.inscribirEstudiante`.
Los grupos y planes deben ser objetos nuevos para cada ciclo, como hace `App`; únicamente
el departamento se comparte.

Los constructores reciben ahora `EstadoCiclo`, no un texto como `"ABIERTO"`.
Mientras el ciclo está en `CURSO`, se permiten cambios a sus datos, inscripciones,
asignaciones y calificaciones. `terminar()` o `setEstado(EstadoCiclo.TERMINADO)` lo cierran
definitivamente: ya no se pueden modificar sus datos ni volver a `CURSO`. Los grupos
y sus cursos también se bloquean para evitar modificaciones por esas referencias.
Las consultas y boletas siguen disponibles, incluso con cursos pendientes de calificar.
Al cerrar se conservan copias de las boletas, de modo que cambios posteriores en los datos
personales de alumnos o maestros no alteren los documentos del ciclo terminado.
El catálogo de `Departamento` es independiente y sigue siendo editable para otros ciclos.

Se aprueba desde 6. `EXTRAORDINARIO` identifica una evaluación extraordinaria con su nota
numérica (también puede ser menor que 6). Registrar otra calificación sustituye el resultado
actual de ese curso; no se lleva un historial de intentos ni se manejan parciales.
Los cursos pendientes aparecen sin nota y no participan en el promedio. Las boletas son
copias inmutables: una boleta ya emitida no cambia al registrar nuevas calificaciones.
El maestro de un resultado registrado se conserva aunque después se reasigne el curso.

**Los datos se almacenan únicamente en memoria y se pierden al cerrar el programa.**
Los planes iniciales de `App.crearCiclo` son ejemplos editables, no un plan oficial.
---

## Diagrama de Clases UML
*insertar imagen*
---

## Instrucciones de Compilación y Ejecución

### Requisitos Previos
* **Java Development Kit (JDK):** Versión 17 o superior instalada y agregada al `PATH`.
* **Editor recomendado:** Visual Studio Code (con soporte para Java) o el IDE de su preferencia.
* **Git** instalado para clonar el repositorio.
  
> Si deseas personalizar la estructura de carpetas en Visual Studio Code, abre `.vscode/settings.json` y actualiza la configuración correspondiente.

### Pasos para Compilar y Ejecutar

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/AngelSanchez18/Proyecto-final-POO.git
   cd Proyecto-final-POO
   ```

2. **Compilar y ejecutar** (desde la raíz, con Bash/Git Bash):
   ```bash
   javac -encoding UTF-8 -d bin src/personas/*.java src/servicios/*.java src/ciclo/*.java src/App.java
   java -cp bin App
   ```

3. **Ejecutar las pruebas** (Windows/Git Bash):
   ```bash
   javac -encoding UTF-8 -cp 'bin;lib/junit-platform-console-standalone-6.1.3.jar' -d bin test/Test.java
   java -jar lib/junit-platform-console-standalone-6.1.3.jar execute --class-path bin --scan-class-path
   ```
   En Linux/macOS, cambiar `;` por `:` en el classpath de compilación de las pruebas.
   La versión incluida de JUnit requiere Java 17 o superior.

El menú inicia en el ciclo 2026 con los grupos 1A, 2A y 3A del turno matutino.
Primero registra maestros y alumnos; después asigna maestros a los cursos y captura notas.
La opción de consulta muestra los IDs de grupos, cursos, alumnos y maestros.
Crear un ciclo nuevo reutiliza el departamento, pero comienza sin alumnos, asignaciones ni notas.
La opción 9 termina el ciclo seleccionado tras pedir confirmación; el estado se muestra en el menú.
Crear otro ciclo no termina automáticamente el anterior.
---
## Autores
<p align="center">
  <a href="https://github.com/Romelisrael" title="Romelisrael">
    <img src="https://images.weserv.nl/?url=github.com/Romelisrael.png&w=100&h=100&fit=cover&mask=circle" width="80" height="80" alt="Romelisrael"/>
  </a>
  &nbsp;
  <a href="https://github.com/zS25018108" title="zS25018108">
    <img src="https://images.weserv.nl/?url=github.com/zS25018108.png&w=100&h=100&fit=cover&mask=circle" width="80" height="80" alt="zS25018108"/>
  </a>
  &nbsp;
  <a href="https://github.com/AngelSanchez18" title="AngelSanchez18">
    <img src="https://images.weserv.nl/?url=github.com/AngelSanchez18.png&w=100&h=100&fit=cover&mask=circle" width="80" height="80" alt="AngelSanchez18"/>
  </a>
</p>
