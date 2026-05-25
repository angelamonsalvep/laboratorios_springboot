# 🧪 LABORATORIO DÍA 2 — Ingeniería de Rendimiento: Records, Slice y EntityGraph

**Módulo 6.1 · Semana 4 · Proyecto LibroTech 📚**  
**Duración estimada:** 3 horas  
**Nivel:** Avanzado  

---

## 📋 Objetivo del Laboratorio

Al finalizar esta práctica, el estudiante será capaz de:

1. Crear **Java Records** como DTOs livianos para optimizar la transferencia de datos.
2. Implementar **proyecciones JPQL con constructor** para "aplanar" relaciones en consultas eficientes.
3. Utilizar **`Slice<T>`** como alternativa a `Page<T>` para paginación de alto rendimiento.
4. Resolver el **Problema N+1** usando `@EntityGraph` y `JOIN FETCH`.
5. Interpretar los **logs SQL** de Hibernate para diagnosticar problemas de rendimiento.

---

## 📖 Contexto de Negocio — LibroTech

LibroTech ahora maneja un catálogo de **más de 200 libros**. Los bibliotecarios se quejan de que la página del catálogo tarda en cargar. Al revisar los logs SQL descubrimos el **Problema N+1**: por cada libro se disparan consultas adicionales para traer la Editorial y los géneros. Necesitamos optimizar el acceso a datos de nivel empresarial sin sacrificar funcionalidad.

---

## 🧠 Contexto Conceptual

### Java Records como DTOs

Un **Record** en Java (desde Java 14+) es una clase inmutable compacta, ideal para transferir datos:

```java
public record LibroResumenDTO(
    String titulo,
    LocalDate fechaPublicacion,
    String editorialNombre,
    String pais
) {}
```

**Ventajas sobre una entidad JPA:**
- ❌ No tiene proxies de Hibernate (más liviano en memoria).
- ❌ No dispara lazy loading inesperado.
- ✅ Inmutable por diseño (thread-safe).
- ✅ Genera `equals()`, `hashCode()`, `toString()` automáticamente.

### El Problema N+1

Cuando cargamos una lista de `Libro` y cada uno tiene una `Editorial` con `FetchType.LAZY`:

```
SELECT * FROM libros;                  -- 1 consulta (trae 200 libros)
SELECT * FROM editoriales WHERE id=1;  -- +1 consulta por cada libro
SELECT * FROM editoriales WHERE id=2;  -- +1
SELECT * FROM editoriales WHERE id=3;  -- +1
...                                    -- = 201 consultas totales 💀
```

**Soluciones:**
- **`@EntityGraph`**: Declara qué relaciones cargar ansiosamente a nivel de método del repositorio.
- **`JOIN FETCH`**: Fuerza un JOIN en JPQL para traer todo en una sola consulta SQL.

### `Slice<T>` vs `Page<T>`

| Característica | `Page<T>` | `Slice<T>` |
|---|---|---|
| Conteo total de registros | ✅ Sí (`SELECT COUNT(*)`) | ❌ No |
| Sabe si hay página siguiente | ✅ Sí | ✅ Sí |
| Rendimiento | 🐌 Más lento (2 queries) | ⚡ Más rápido (1 query) |
| Caso de uso ideal | Mostrar "Página 3 de 15" | Botones "Anterior / Siguiente" |

`Slice` pide **N+1 registros** para saber si hay más páginas, pero **no ejecuta COUNT**, lo que es ideal para catálogos masivos.

---

## 📝 Actividades

### Actividad 1 — Crear el Record `LibroResumenDTO`

Cree el paquete `dto` y dentro el Record que "aplana" la información del libro:

```java
package com.librotech.dto;

import java.time.LocalDate;

/**
 * DTO liviano para listados masivos.
 * "Aplana" la relación Libro → Editorial extrayendo solo los campos necesarios.
 * Al ser un Record, es inmutable y no genera proxies de Hibernate.
 */
public record LibroResumenDTO(
    Long id,
    String titulo,
    LocalDate fechaPublicacion,
    Double precio,
    String editorialNombre,
    String pais
) {}
```

> 💡 **¿Por qué "aplanar"?** En lugar de devolver un `Libro` con un objeto `Editorial` anidado (que podría disparar lazy loading), extraemos `editorialNombre` y `pais` directamente en la consulta.

---

### Actividad 2 — Implementar la Proyección JPQL con Constructor

En su `LibroRepository`, cree un método que construya el DTO directamente desde JPQL:

```java
package com.librotech.repository;

import com.librotech.dto.LibroResumenDTO;
import com.librotech.model.Libro;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    /**
     * Proyección JPQL con constructor de Record.
     * - Usa "new" para construir el DTO directamente en la consulta.
     * - JOIN con Editorial para obtener nombre y país en UNA sola query.
     * - Ordena por fecha de publicación descendente.
     * - Retorna Slice para evitar COUNT innecesario.
     */
    @Query("""
        SELECT new com.librotech.dto.LibroResumenDTO(
            l.id,
            l.titulo,
            l.fechaPublicacion,
            l.precio,
            l.editorial.nombre,
            l.editorial.pais
        )
        FROM Libro l
        JOIN l.editorial
        ORDER BY l.fechaPublicacion DESC
        """)
    Slice<LibroResumenDTO> findAllLibroResumenes(Pageable pageable);
}
```

> ⚠️ **Regla técnica:** Se **prohíbe** el uso de entidades pesadas en los listados. Siempre use el DTO.

---

### Actividad 3 — Resolver el Problema N+1 con `@EntityGraph`

Para los casos donde SÍ necesitamos la entidad completa (ej: editar un libro), usamos `@EntityGraph` para cargar Editorial y Géneros en una sola consulta:

```java
// Agregar estos imports y métodos a LibroRepository

import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;
import java.util.List;

// ... dentro de la interfaz LibroRepository:

    /**
     * Carga un libro con sus relaciones (Editorial + Géneros) en UNA sola query.
     * Sin @EntityGraph, acceder a libro.getEditorial() o libro.getGeneros()
     * dispararía consultas adicionales (N+1).
     */
    @EntityGraph(attributePaths = {"editorial", "generos"})
    Optional<Libro> findById(Long id);

    /**
     * Lista completa con carga ansiosa de relaciones.
     * Útil para el panel de administración donde se necesitan todos los datos.
     */
    @EntityGraph(attributePaths = {"editorial", "generos"})
    @Query("SELECT l FROM Libro l ORDER BY l.fechaPublicacion DESC")
    List<Libro> findAllWithRelations();
```

> 🔑 **`@EntityGraph`** le dice a Hibernate: "Cuando ejecutes esta consulta, trae también estas relaciones con un LEFT JOIN, sin esperar a que el código las solicite".

---

### Actividad 4 — Crear el Servicio con Paginación Slice

```java
package com.librotech.service;

import com.librotech.dto.LibroResumenDTO;
import com.librotech.model.Libro;
import com.librotech.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Obtiene un "slice" (fragmento) del catálogo de libros.
     * No ejecuta COUNT → más rápido que Page para catálogos masivos.
     *
     * @param page número de página (0-indexed)
     * @return Slice con los DTOs de resumen y metadatos de navegación
     */
    public Slice<LibroResumenDTO> getCatalogo(int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        return libroRepository.findAllLibroResumenes(pageable);
    }

    /**
     * Obtiene un libro con TODAS sus relaciones cargadas (para edición/detalle).
     */
    public Libro getLibroWithRelations(Long id) {
        return libroRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Libro no encontrado: " + id));
    }

    // ... otros métodos del servicio (save, delete, etc.)
}
```

---

### Actividad 5 — Crear el Controlador REST con Paginación

```java
package com.librotech.controller;

import com.librotech.dto.LibroResumenDTO;
import com.librotech.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    @Autowired
    private LibroService libroService;

    /**
     * GET /api/libros?page=0
     * Retorna un fragmento (Slice) del catálogo con metadatos de navegación.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCatalogo(
            @RequestParam(defaultValue = "0") int page) {

        Slice<LibroResumenDTO> slice = libroService.getCatalogo(page);

        // Construimos una respuesta con metadatos de navegación
        Map<String, Object> response = new HashMap<>();
        response.put("libros", slice.getContent());
        response.put("currentPage", slice.getNumber());
        response.put("pageSize", slice.getSize());
        response.put("hasNext", slice.hasNext());
        response.put("hasPrevious", slice.hasPrevious());

        return ResponseEntity.ok(response);
    }
}
```

---

### Actividad 6 — Diagnosticar con los Logs SQL

Con la configuración del Día 1 (`logging.level.org.hibernate.orm.jdbc.bind=TRACE`), ejecute la aplicación y visite `GET /api/libros?page=0`.

**Observe la consola.** Debería ver **UNA sola consulta SQL** como esta:

```sql
select
    l1_0.id,
    l1_0.titulo,
    l1_0.fecha_publicacion,
    l1_0.precio,
    e1_0.nombre,
    e1_0.pais
from
    libros l1_0
    join editoriales e1_0 on e1_0.id = l1_0.editorial_id
where
    l1_0.disponible = true
order by
    l1_0.fecha_publicacion desc
limit ?, ?

-- binding parameter (1:INTEGER) <- [0]
-- binding parameter (2:INTEGER) <- [11]   ← Pide 11 registros (10+1 para saber si hay más)
```

> 🎯 **Evidencia de optimización:** UNA sola query con JOIN, sin N+1, sin COUNT.

---

## 🏋️ Retos para el Coder

### Reto 1 — Comparar Page vs Slice (Obligatorio)
Cree un segundo método en el repositorio que retorne `Page<LibroResumenDTO>` en lugar de `Slice`. Compare los logs SQL de ambos endpoints. Documente en un comentario: ¿cuántas queries ejecuta cada uno? ¿Cuál es más eficiente para un catálogo de 200+ registros?

### Reto 2 — DTO con Géneros
Cree un nuevo Record `LibroDetalleDTO` que incluya además la lista de nombres de géneros. Investigue cómo podría lograrlo (pista: puede que necesite un enfoque de post-procesamiento con `@EntityGraph` en lugar de proyección JPQL pura).

### Reto 3 — JOIN FETCH como Alternativa
Implemente la misma consulta del `findAllWithRelations()` pero usando `JOIN FETCH` en JPQL en lugar de `@EntityGraph`. Compare ambas aproximaciones:

```java
@Query("SELECT DISTINCT l FROM Libro l JOIN FETCH l.editorial JOIN FETCH l.generos ORDER BY l.fechaPublicacion DESC")
List<Libro> findAllWithRelationsJPQL();
```

¿Hay diferencia en el SQL generado? ¿Cuándo usaría uno vs el otro?

### Reto 4 — Paginación Configurable
Modifique el endpoint para que el tamaño de página sea configurable mediante un parámetro `size` con un **máximo de 50**:

```
GET /api/libros?page=0&size=20
```

Implemente la validación en el servicio para que si `size > 50`, se use 50 como máximo.

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Existe el Record `LibroResumenDTO` con campos "aplanados" | ☐ |
| La consulta JPQL usa `SELECT new ...` para construir el DTO | ☐ |
| El repositorio retorna `Slice<LibroResumenDTO>` (no `Page` ni `List`) | ☐ |
| El método `findById` usa `@EntityGraph` para cargar relaciones | ☐ |
| Los logs SQL muestran UNA sola consulta con JOIN (no N+1) | ☐ |
| El endpoint REST retorna metadatos de navegación (hasNext, hasPrevious) | ☐ |
| La consola muestra SQL formateado con parámetros reales | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Por qué usamos un **Record** (`LibroResumenDTO`) en lugar de simplemente devolver la entidad `Libro` en los listados? ¿Qué problemas podría causar exponer entidades JPA directamente en la API?
2. Si el `Slice` pide 11 registros pero solo hay 8 en la base de datos, ¿qué valor tendrá `hasNext()`? ¿Cómo utiliza Hibernate ese registro extra?
3. ¿Qué diferencia hay entre usar `@EntityGraph` a nivel de repositorio vs configurar `FetchType.EAGER` directamente en la entidad? ¿Cuál es más flexible y por qué?
4. Imagine que tiene 1 millón de libros. ¿Seguiría siendo adecuado `Slice`? ¿Qué otras estrategias de paginación conoce (ej. cursor-based pagination)?
