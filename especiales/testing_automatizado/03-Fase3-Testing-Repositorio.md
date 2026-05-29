# ⚙️ FASE 3 — Testing del Repositorio con @DataJpaTest

**Laboratorio Especial de Testing · Proyecto LibroTech 📚**  
**Duración estimada:** 1 hora  
**Nivel:** Intermedio  

---

## 📋 Objetivo de esta Fase

Al finalizar esta fase serás capaz de:

1. Entender la diferencia entre un **test unitario** (con Mocks) y un **test de integración de repositorio** (con base de datos real).
2. Usar `@DataJpaTest` para levantar SOLO la capa de datos con una base de datos H2 en memoria.
3. Probar que tus **consultas derivadas** (`findByAutor`, `findByTitulo`, etc.) retornan los datos correctos.
4. Entender el **rollback automático** que mantiene cada test aislado.
5. Usar `TestEntityManager` para tener más control sobre los datos de prueba.

---

## 🧠 ¿Cuándo uso Mocks y cuándo uso @DataJpaTest?

Esta es una de las preguntas más importantes del testing. Aquí tienes una guía clara:

| Situación | Herramienta | ¿Por qué? |
|---|---|---|
| Probar la **lógica de negocio** del Service | `@Mock` (Mockito) | No necesitas BD, solo verificas que la lógica es correcta |
| Probar que las **consultas SQL** funcionan | `@DataJpaTest` | Necesitas una BD real (en memoria) para ejecutar las queries |
| Probar que `findByAutor("García Márquez")` devuelve los libros correctos | `@DataJpaTest` | Esa consulta genera SQL real que debe ejecutarse contra tablas reales |

### Analogía

- **Mockito** (Fase 2) → Pruebas que el recepcionista del hotel sigue el protocolo correcto cuando un huésped pide una habitación. No necesitas un hotel real.
- **@DataJpaTest** (Fase 3) → Pruebas que el sistema de reservas del hotel realmente puede buscar habitaciones disponibles en la base de datos. Aquí SÍ necesitas la base de datos (pero una de prueba).

---

## 📖 Contexto de Negocio

El equipo de desarrollo de **LibroTech** ha creado varias consultas personalizadas en el `LibroRepository`: búsqueda por autor, por título, por ISBN, etc. Necesitamos garantizar que esas consultas **realmente** retornan los datos correctos cuando se ejecutan contra la base de datos. Un error en una consulta podría hacer que los bibliotecarios no encuentren libros que sí existen.

---

## 🧠 ¿Qué hace exactamente `@DataJpaTest`?

Cuando pones `@DataJpaTest` en tu clase de test, Spring Boot hace esto automáticamente:

1. **Crea una base de datos H2 en memoria** (temporal, solo para el test).
2. **Escanea tus `@Entity`** y crea las tablas correspondientes.
3. **Escanea tus `@Repository`** y los hace disponibles con `@Autowired`.
4. **NO carga** controllers, services, ni ninguna otra capa — solo la de datos.
5. **Cada test es transaccional** — al terminar, hace **rollback** (deshace los cambios).

```
Lo que @DataJpaTest carga:
  ✅ Entidades (@Entity)
  ✅ Repositorios (@Repository / JpaRepository)
  ✅ Base de datos H2 en memoria
  ❌ Servicios (@Service)
  ❌ Controladores (@RestController)
  ❌ Tu application.properties real (usa configuración de test)
```

---

## 📝 Actividades

### Actividad 1 — Crear el Test para `LibroRepository`

Crea el archivo: `src/test/java/com/librotech/repository/LibroRepositoryTest.java`

```java
package com.librotech.repository;

import com.librotech.model.Libro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest  // ← Levanta SOLO la capa de datos con H2 en memoria
class LibroRepositoryTest {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private TestEntityManager entityManager; // ← Herramienta auxiliar para insertar datos

    @BeforeEach
    void setUp() {
        // Insertamos datos de prueba ANTES de cada test
        // Usamos entityManager.persistAndFlush() para asegurarnos
        // de que los datos están guardados en la BD antes de consultar
        entityManager.persistAndFlush(
            new Libro(null, "Cien años de soledad", "Gabriel García Márquez", "ISBN-001", 1967)
        );
        entityManager.persistAndFlush(
            new Libro(null, "El amor en los tiempos del cólera", "Gabriel García Márquez", "ISBN-002", 1985)
        );
        entityManager.persistAndFlush(
            new Libro(null, "1984", "George Orwell", "ISBN-003", 1949)
        );
    }

    @Test
    @DisplayName("findAll() debería retornar todos los libros insertados")
    void findAllDeberiaRetornarTodosLosLibros() {
        // WHEN
        List<Libro> libros = libroRepository.findAll();

        // THEN
        assertEquals(3, libros.size(), 
            "Deberían existir exactamente 3 libros en la BD de prueba");
    }
}
```

#### 🔍 ¿Por qué `TestEntityManager` en vez de `libroRepository.save()`?

Es una buena práctica usar `TestEntityManager` para **insertar datos de prueba** y `libroRepository` solo para **ejecutar las consultas que queremos probar**. ¿Por qué?

- Si usamos `libroRepository.save()` para insertar datos y luego `libroRepository.findAll()` para leerlos, estamos probando `save()` y `findAll()` al mismo tiempo.
- Con `TestEntityManager`, insertamos datos "por fuera" del repositorio, y así probamos SOLO la consulta.

> 💡 **Nota**: Si tu entidad `Libro` tiene relaciones obligatorias (como `@ManyToOne` con `Editorial`), necesitarás crear y persistir la editorial primero. Lo veremos en la Actividad 4.

---

### Actividad 2 — Probar la Consulta Derivada `findByAutor()`

```java
@Test
@DisplayName("findByAutor() debería retornar solo los libros de ese autor")
void findByAutorDeberiaFiltrarCorrectamente() {
    // WHEN - Buscamos los libros de García Márquez
    List<Libro> librosGM = libroRepository.findByAutor("Gabriel García Márquez");

    // THEN
    assertEquals(2, librosGM.size(), 
        "García Márquez tiene exactamente 2 libros en los datos de prueba");
    
    // Verificamos que TODOS los resultados son del autor correcto
    librosGM.forEach(libro -> 
        assertEquals("Gabriel García Márquez", libro.getAutor(),
            "Cada libro retornado debe ser del autor consultado")
    );
}

@Test
@DisplayName("findByAutor() debería retornar lista vacía para autor inexistente")
void findByAutorInexistenteDeberiaRetornarVacio() {
    // WHEN
    List<Libro> resultado = libroRepository.findByAutor("Autor Inventado");

    // THEN
    assertNotNull(resultado, "El resultado nunca debería ser null");
    assertTrue(resultado.isEmpty(), 
        "No deberían haber libros de un autor que no existe");
}
```

---

### Actividad 3 — Probar Operaciones de Persistencia

```java
@Test
@DisplayName("save() debería asignar un ID autoincremental al libro nuevo")
void saveDeberiaAsignarId() {
    // GIVEN
    Libro nuevoLibro = new Libro(null, "Rayuela", "Julio Cortázar", "ISBN-NEW", 1963);

    // WHEN
    Libro guardado = libroRepository.save(nuevoLibro);

    // THEN
    assertNotNull(guardado.getId(), 
        "El libro guardado debería tener un ID asignado automáticamente");
    assertTrue(guardado.getId() > 0, 
        "El ID debería ser un número positivo");
}

@Test
@DisplayName("deleteById() debería eliminar el libro de la base de datos")
void deleteByIdDeberiaEliminarLibro() {
    // GIVEN - Obtenemos un libro existente
    Libro libro = libroRepository.findAll().get(0);
    Long id = libro.getId();

    // WHEN
    libroRepository.deleteById(id);
    entityManager.flush(); // Forzamos que la BD procese el delete
    entityManager.clear(); // Limpiamos la caché de Hibernate

    // THEN
    Optional<Libro> resultado = libroRepository.findById(id);
    assertTrue(resultado.isEmpty(), 
        "El libro eliminado no debería encontrarse en la BD");
}

@Test
@DisplayName("findById() debería retornar el libro correcto")
void findByIdDeberiaRetornarLibroCorrecto() {
    // GIVEN - Insertamos un libro y capturamos su ID
    Libro libro = entityManager.persistAndFlush(
        new Libro(null, "El Principito", "Saint-Exupéry", "ISBN-EP", 1943)
    );

    // WHEN
    Optional<Libro> resultado = libroRepository.findById(libro.getId());

    // THEN
    assertTrue(resultado.isPresent(), "Debería encontrar el libro por su ID");
    assertEquals("El Principito", resultado.get().getTitulo());
}
```

#### 🔍 ¿Por qué `flush()` y `clear()`?

- **`flush()`**: Fuerza a Hibernate a ejecutar las sentencias SQL pendientes contra la base de datos. Sin esto, Hibernate podría estar guardando los cambios solo en su caché interna.
- **`clear()`**: Limpia la caché de primer nivel de Hibernate. Sin esto, `findById()` podría devolver el objeto desde la caché y no desde la base de datos real.

---

### Actividad 4 — Probar con Entidades Relacionadas (Avanzado)

Si tu proyecto de la Semana 4 tiene relaciones (Libro → Editorial), necesitas adaptar tus tests:

```java
@DataJpaTest
class LibroRepositoryConRelacionesTest {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Editorial editorialPlaneta;

    @BeforeEach
    void setUp() {
        // PRIMERO: Crear y persistir la Editorial (es la dependencia)
        editorialPlaneta = new Editorial("Planeta", "Barcelona", "España", 1949);
        entityManager.persistAndFlush(editorialPlaneta);

        // DESPUÉS: Crear libros asociados a esa editorial
        Libro libro1 = new Libro("La sombra del viento", "Carlos Ruiz Zafón", 
            "ISBN-R01", LocalDate.of(2001, 4, 1), 48000.0, editorialPlaneta);
        libro1.setDisponible(true);
        entityManager.persistAndFlush(libro1);

        Libro libro2 = new Libro("Los pilares de la Tierra", "Ken Follett", 
            "ISBN-R02", LocalDate.of(1989, 1, 1), 58000.0, editorialPlaneta);
        libro2.setDisponible(true);
        entityManager.persistAndFlush(libro2);
    }

    @Test
    @DisplayName("Debería encontrar libros por editorial")
    void buscarLibrosPorEditorial() {
        // WHEN
        List<Libro> libros = libroRepository.findAll();

        // THEN
        assertFalse(libros.isEmpty());
        libros.forEach(libro -> 
            assertEquals("Planeta", libro.getEditorial().getNombre())
        );
    }
}
```

> ⚠️ **Importante sobre `@SQLRestriction`**: Si tu entidad `Libro` tiene `@SQLRestriction("disponible = true")`, recuerda que JPA solo devolverá libros donde `disponible = true`. En tus datos de prueba, asegúrate de poner `setDisponible(true)` para que los libros sean "visibles".

---

### Actividad 5 — Configuración de Test con `application-test.properties`

Para que tus tests usen una configuración separada (sin afectar tu base de datos de desarrollo), crea el archivo:

`src/test/resources/application-test.properties`

```properties
# Base de datos H2 en memoria para tests
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Hibernate crea las tablas automáticamente en tests
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Desactivar Flyway en tests (usamos ddl-auto en su lugar)
spring.flyway.enabled=false
```

> 💡 **`create-drop`** significa: crear las tablas al empezar y destruirlas al terminar. Perfecto para tests donde empezamos de cero cada vez.

Si usas Flyway en tu proyecto, la línea `spring.flyway.enabled=false` es **crucial** — sin ella, Flyway intentará ejecutar tus scripts de migración en la base de datos de test y podría causar conflictos.

---

### Actividad 6 — Práctica Autónoma

1. **Crea `EditorialRepositoryTest`**: Prueba que puedes guardar, buscar y eliminar editoriales.
2. **Agrega una consulta derivada nueva**: Añade `findByTituloContaining(String fragmento)` a tu `LibroRepository` y escribe un test que verifique que buscar por "soledad" retorna "Cien años de soledad".
3. **Prueba el caso de ISBN duplicado**: Intenta guardar dos libros con el mismo ISBN (si tienes `@Column(unique = true)`) y verifica que lanza una excepción.

> 💡 **Pista para el punto 3**: Usa `assertThrows()`:
> ```java
> assertThrows(DataIntegrityViolationException.class, () -> {
>     libroRepository.save(libroConIsbnDuplicado);
>     entityManager.flush(); // Fuerza la ejecución del SQL
> });
> ```

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Se usa `@DataJpaTest` correctamente | ☐ |
| Se usa `TestEntityManager` para insertar datos de prueba | ☐ |
| Se prueban consultas derivadas (`findByAutor`, etc.) | ☐ |
| Se prueban operaciones CRUD (save, findById, delete) | ☐ |
| Se creó `application-test.properties` con configuración de H2 | ☐ |
| Todos los tests pasan exitosamente | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Por qué `@DataJpaTest` no carga los `@Service` ni los `@RestController`? ¿Qué beneficio tiene esta limitación?
2. ¿Qué pasaría si dos tests insertan datos con el mismo ISBN? ¿Se afectarían entre sí? ¿Por qué sí o por qué no?
3. ¿Cuál es la diferencia entre `@DataJpaTest` y `@SpringBootTest` (que veremos en la Fase 5)?
4. Si tu consulta `findByAutor()` tuviera un bug (por ejemplo, retornara libros de TODOS los autores), ¿este test lo detectaría?

---

## ➡️ Siguiente Fase

Ahora que probaste la capa de datos, avanza a **[Fase 4 — Testing del Controlador con MockMvc](./04-Fase4-Testing-Controlador.md)**, donde probarás tus endpoints REST sin necesidad de levantar un servidor.
