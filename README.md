# Sistema de Gestión Escolar - Escuela Secundaria

## Descripción del Problema
Las escuelas secundarias públicas requieren un control organizado y confiable de su operación académica, pero muchas veces la administración de estudiantes, tutores, docentes, grupos por turno, ciclos escolares y calificaciones se lleva de forma dispersa o manual. Esto dificulta el seguimiento académico, el cálculo oportuno de promedios y la emisión ágil de documentos oficiales como boletas y constancias.

Este sistema resuelve esa problemática mediante una aplicación desarrollada en **Java** bajo el paradigma de **Programación Orientada a Objetos (POO)**, centralizando la gestión académica y facilitando la consulta histórica de información entre ciclos escolares.

---

## Lista de Funcionalidades
* Tres grados de secundaria, con múltiples grupos por grado, turno y plan de estudios por grupo.
* Inscripción de alumnos con validación de cupo e inscripción única dentro del ciclo.
* Catálogo de maestros en `servicios.Departamento`, compartido entre ciclos escolares.
* Asignación de maestros a materias y talleres de cada grupo.
* Calificaciones de 0 a 10: `NO_CALIFICADO`, `REPROBADO`, `APROBADO` y `EXTRAORDINARIO`.
* Boletas con alumno, matrícula, grado, grupo, turno, cursos, maestros, notas y promedio.
* Selección del año de inicio del ciclo escolar antes de entrar al menú de gestión.
* Menú de consola para gestionar la escuela y cambiar entre los ciclos creados en la sesión.
* Guardado y carga de un ciclo completo en JSON UTF-8, incluidos maestros, grupos, cursos,
  alumnos, asignaciones, calificaciones, estado y boletas finales.
* Archivo de prueba `datos/ciclo-2026-prueba.json` con dos grupos matutinos y dos vespertinos.
* Estado del ciclo con `EstadoCiclo.CURSO` y `EstadoCiclo.TERMINADO`; cierre definitivo desde el menú.

### Organización y reglas
`App` mantiene un departamento y el historial de ciclos. Cada `ciclo.Ciclo` administra sus
propios grupos, inscripciones, asignaciones y resultados. Los planes se representan con
`ProgramaEducativo`; admiten tanto `Materia` como `Taller`. El taller es un curso extra
con su propio maestro, nota y renglón en la boleta; su nota también participa en el promedio.
En los planes de ejemplo de `App`, el taller de Tecnología tiene el ID 4: se asigna y se
califica con las mismas opciones que las materias.

El constructor completo de `Ciclo` exige al menos un grupo. Se permiten varios grupos por
grado; el ID y la combinación de grado, letra y turno deben ser únicos en el ciclo.
El constructor anterior se conserva para configurar los grupos posteriormente con `agregarGrupo`.
Para mantener la inscripción
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

---
**Los cambios se realizan en memoria: usa la opción 10 para guardarlos antes de salir.**
La opción 11 carga un JSON y lo selecciona para trabajar. Si su año o ID ya existe en la sesión,
solicita confirmación antes de reemplazarlo en memoria. Cada archivo incluye su propio catálogo
de docentes; al cargarlo no se mezclan sus maestros con los de otros ciclos ya abiertos.
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
   javac --release 17 -encoding UTF-8 -cp 'lib/*' -d bin src/personas/*.java src/servicios/*.java src/ciclo/*.java src/persistencia/*.java src/App.java
   java -cp 'bin;lib/*' App
   ```

3. **Ejecutar las pruebas** (Windows/Git Bash):
   ```bash
   javac --release 17 -encoding UTF-8 -cp 'bin;lib/*' -d bin test/*.java
   java -jar lib/junit-platform-console-standalone-6.1.3.jar execute --class-path 'bin;lib/gson-2.11.0.jar' --scan-class-path
   ```
   En Linux/macOS, cambiar `;` por `:` en todos los classpaths.
   `--release 17` genera archivos `.class` compatibles con Java 17 o superior,
   incluso al compilar con un JDK más reciente. Úsalo también al recompilar las pruebas.
   Si aparece `UnsupportedClassVersionError`, recompila con estos comandos y verifica
   las versiones con `java -version` y `javac -version`.
   La versión incluida de JUnit requiere Java 17 o superior.
   La persistencia JSON usa Gson 2.11.0, incluido en `lib/gson-2.11.0.jar` (licencia Apache 2.0).
---

Al iniciar, ingresa el año de inicio del ciclo escolar: por ejemplo, `2026` para trabajar
con el periodo `2026-2027`. El programa valida el año y solicita corregirlo si no es válido.
Después se abre el menú con los grupos 1A, 2A y 3A del turno matutino para ese ciclo.
Las inscripciones, asignaciones y calificaciones se realizan únicamente en el ciclo seleccionado,
cuyo periodo y estado se muestran en el encabezado. El catálogo de maestros sigue siendo compartido.
Primero registra maestros y alumnos; después asigna maestros a los cursos y captura notas.
La opción de consulta muestra los IDs de grupos, cursos, alumnos y maestros.
La opción 7 crea y selecciona un ciclo nuevo: reutiliza el departamento, pero comienza sin
alumnos, asignaciones ni notas. La opción 8 permite volver a cualquiera de los ciclos creados
durante la sesión sin perder sus datos.
La opción 9 termina el ciclo seleccionado tras pedir confirmación; el estado se muestra en el menú.
Crear otro ciclo no termina automáticamente el anterior.

### Probar las boletas desde JSON

1. Al iniciar, en lugar de un año escribe `json`.
2. Ingresa la ruta `datos/ciclo-2026-prueba.json`.
3. Usa la opción **6** para consultar los cuatro grupos y sus alumnos.
4. Usa la opción **5** e ingresa cualquiera de los siguientes IDs:

| ID alumno | Alumno | Grupo | Turno | Promedio |
| --- | --- | --- | --- | --- |
| 101 | Luis Perez | 1A (ID 1) | Matutino | 9.00 |
| 102 | Sofia Hernandez | 2A (ID 2) | Matutino | 7.50 |
| 103 | Diego Ramirez | 1A (ID 3) | Vespertino | 7.50 |
| 104 | Valeria Sanchez | 3A (ID 4) | Vespertino | 9.50 |

Todos los datos personales del ejemplo son ficticios. Cada grupo tiene Español, Matemáticas,
Ciencias y un taller de Tecnología, con sus maestros y calificaciones. Se incluyen resultados
aprobados, reprobados y extraordinarios. El ciclo está en `CURSO` para poder modificarlo.

Para guardar tus pruebas, usa **10** y escribe, por ejemplo, `datos/mi-ciclo.json`.
En otra ejecución puedes cargar ese archivo escribiendo `json` al inicio o usando **11**
desde el menú. El guardado es manual: salir no guarda automáticamente.
El formato tiene `version: 1`; los IDs de cursos son locales a cada grupo y los IDs de alumnos
son únicos en el ciclo. Los estados y tipos se escriben como los valores del JSON de ejemplo.
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
