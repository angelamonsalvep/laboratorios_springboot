# 🧪 LABORATORIO DÍA 5 — Calidad en el MVC: Pruebas de Integración con @DataJpaTest

**Módulo 6.1 · Semana 2 · Proyecto LibroTech 📚**  
**Duración estimada:** 2 horas  
**Nivel:** Intermedio-Avanzado  

---

## 📋 Objetivo del Laboratorio

Al finalizar esta práctica, el estudiante será capaz de:

1. Implementar **Pruebas de Integración** para la capa de persistencia (Repositorios).
2. Utilizar la anotación `@DataJpaTest` para aislar las pruebas de la base de datos.
3. Validar que las consultas derivadas (Derived Queries) funcionan correctamente.
4. Aplicar los principios de pruebas automatizadas para garantizar que el Modelo se comporta según lo esperado.

---

## 📖 Contexto de Negocio — LibroTech

La fiabilidad del catálogo es la máxima prioridad de **LibroTech**. Si un error en el código impide guardar libros o buscar por autor, la biblioteca se detiene. Por ello, implementaremos una suite de pruebas que verifique automáticamente que nuestro sistema de persistencia funciona perfectamente antes de que el código llegue a producción.

---

## 🧠 Contexto Conceptual: Testing en JPA

Para probar nuestro **Modelo** y su persistencia, Spring Boot nos ofrece la anotación `@DataJpaTest`. Esta anotación:
- No carga toda la aplicación (solo la capa de datos), lo que la hace muy rápida.
- Usa una base de datos en memoria (H2) que se limpia después de cada prueba.
- Aplica un **Rollback automático**: lo que guardes en una prueba no afecta a la siguiente ni a la base de datos real.

---

## 📝 Actividades

### Actividad 1 — Crear el Test para `LibroRepository`

Cree el archivo de prueba en `src/test/java/com/librotech/repository/LibroRepositoryTest.java`:

```java
package com.librotech.repository;

import com.librotech.model.Libro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LibroRepositoryTest {

    @Autowired
    private LibroRepository libroRepository;

    @Test
    @DisplayName("Debería guardar un libro y asignarle un ID autoincremental")
    void guardarLibroTest() {
        // GIVEN
        Libro libro = new Libro(null, "Prueba de Test", "Autor Test", "123-TEST", 2024);

        // WHEN
        Libro guardado = libroRepository.save(libro);

        // THEN
        assertNotNull(guardado.getId());
        assertEquals("Prueba de Test", guardado.getTitulo());
    }

    @Test
    @DisplayName("Debería encontrar libros por el autor usando la consulta derivada")
    void buscarPorAutorTest() {
        // GIVEN
        libroRepository.save(new Libro(null, "Libro 1", "Gabriel Garcia", "ISBN-1", 1980));
        libroRepository.save(new Libro(null, "Libro 2", "Miguel Cervantes", "ISBN-2", 1605));

        // WHEN
        List<List<Libro>> resultados = List.of(libroRepository.findByAutor("Gabriel Garcia"));

        // THEN
        assertFalse(resultados.get(0).isEmpty());
        assertEquals(1, resultados.get(0).size());
        assertEquals("Libro 1", resultados.get(0).get(0).getTitulo());
    }

    @Test
    @DisplayName("Debería eliminar un libro correctamente")
    void eliminarLibroTest() {
        // GIVEN
        Libro libro = libroRepository.save(new Libro(null, "A eliminar", "Autor", "ISBN-X", 2000));
        Long id = libro.getId();

        // WHEN
        libroRepository.deleteById(id);
        Optional<Libro> resultado = libroRepository.findById(id);

        // THEN
        assertTrue(resultado.isEmpty());
    }
}
```

---

### Actividad 2 — Ejecutar y Analizar las Pruebas

Ejecute las pruebas desde su IDE o usando el comando:
```bash
mvn test
```
- Verifique que todos los tests pasen (barra verde).
- Observe los logs de la consola: Spring Boot inicia una instancia de H2 solo para la prueba y luego la cierra.

---

### Actividad 3 — Práctica Autónoma: Test para `CategoriaRepository`

1. Cree la clase `CategoriaRepositoryTest`.
2. Implemente un test para validar que se puede guardar una categoría.
3. Implemente un test para validar la búsqueda de una categoría por su nombre (necesitará crear este método en la interfaz del repositorio).

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Se utiliza `@DataJpaTest` para aislar la capa de datos | ☐ |
| Se implementaron al menos 3 casos de prueba para el repositorio | ☐ |
| Los tests validan la persistencia real (guardado y borrado) | ☐ |
| Se usan aserciones (`assertNotNull`, `assertEquals`, `assertTrue`) | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Qué pasaría si intentamos probar el repositorio con `@SpringBootTest` en lugar de `@DataJpaTest`? ¿Cuál sería la diferencia en tiempo de ejecución?
2. ¿Por qué es una buena práctica que los tests de repositorio sean transaccionales y hagan rollback automático?
3. ¿Cómo ayudan las pruebas automáticas a cumplir con los **Criterios de Aceptación** definidos en la Historia de Usuario?
