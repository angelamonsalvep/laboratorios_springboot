# 🧪 LABORATORIO DÍA 3 — Motor de Búsqueda Avanzado, Swagger y Navegación Paginada

**Módulo 6.1 · Semana 4 · Proyecto LibroTech 📚**  
**Duración estimada:** 3 horas  
**Nivel:** Avanzado  

---

## 📋 Objetivo del Laboratorio

Al finalizar esta práctica, el estudiante será capaz de:

1. Implementar un **motor de consultas filtradas** con métodos derivados y JPQL personalizados.
2. Crear búsquedas **parciales e insensibles a mayúsculas** usando `LIKE` y `LOWER()`.
3. Evolucionar la documentación **Swagger/OpenAPI** para reflejar los nuevos Records y filtros.
4. Construir una **interfaz Thymeleaf con navegación paginada** que mantenga los filtros activos en la URL.
5. Implementar **formularios relacionales dinámicos** con desplegables y checkboxes.

---

## 📖 Contexto de Negocio — LibroTech

Con 200+ libros en el catálogo, los bibliotecarios necesitan **encontrar libros rápidamente** filtrando por país de la editorial, género literario, autor o rango de fechas de publicación. Además, la documentación Swagger debe reflejar los cambios arquitectónicos (uso de Records y nuevos filtros) y el panel administrativo debe ofrecer una navegación paginada inteligente que no pierda los filtros al cambiar de página.

---

## 🧠 Contexto Conceptual

### Query Methods Derivados en Spring Data JPA

Spring Data genera consultas SQL automáticamente a partir del **nombre del método**:

| Método | SQL generado |
|---|---|
| `findByPais(String pais)` | `WHERE pais = ?` |
| `findByFechaPublicacionBetween(LocalDate s, LocalDate e)` | `WHERE fecha_publicacion BETWEEN ? AND ?` |
| `findByPaisContainingIgnoreCase(String pais)` | `WHERE LOWER(pais) LIKE LOWER('%?%')` |
| `findByEditorialFundadoEnGreaterThanEqual(int a)` | `WHERE editorial.fundado_en >= ?` |

### Swagger con Records y Filtros

Las anotaciones de OpenAPI (`@Operation`, `@Parameter`, `@Schema`) permiten documentar:
- Los **nuevos esquemas** de Records (ej: `LibroResumenDTO`).
- Los **parámetros de filtro** y su impacto en el resultado.
- Las **reglas de negocio** como el soft delete.

### Navegación Paginada Persistente

En Thymeleaf, los filtros activos se pasan como **parámetros de URL**. Al navegar entre páginas, estos parámetros se deben **preservar** para que la búsqueda no se pierda:

```
/admin/libros?pais=España&generoId=1&page=2
```

---

## 📝 Actividades

### Actividad 1 — Motor de Búsqueda en el Repositorio

Agregue los siguientes métodos a `LibroRepository`:

```java
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

// ... dentro de LibroRepository:

    // ===== BÚSQUEDA POR PAÍS DE EDITORIAL (parcial, insensible a mayúsculas) =====
    @Query("""
        SELECT new com.librotech.dto.LibroResumenDTO(
            l.id, l.titulo, l.fechaPublicacion, l.precio, l.editorial.nombre, l.editorial.pais
        )
        FROM Libro l JOIN l.editorial e
        WHERE LOWER(e.pais) LIKE LOWER(CONCAT('%', :pais, '%'))
        ORDER BY l.fechaPublicacion DESC
        """)
    Slice<LibroResumenDTO> findByPais(@Param("pais") String pais, Pageable pageable);

    // ===== BÚSQUEDA POR GÉNERO LITERARIO =====
    @Query("""
        SELECT new com.librotech.dto.LibroResumenDTO(
            l.id, l.titulo, l.fechaPublicacion, l.precio, l.editorial.nombre, l.editorial.pais
        )
        FROM Libro l JOIN l.editorial JOIN l.generos g
        WHERE g.id = :generoId
        ORDER BY l.fechaPublicacion DESC
        """)
    Slice<LibroResumenDTO> findByGeneroId(@Param("generoId") Long generoId, Pageable pageable);

    // ===== BÚSQUEDA POR RANGO DE FECHAS DE PUBLICACIÓN =====
    @Query("""
        SELECT new com.librotech.dto.LibroResumenDTO(
            l.id, l.titulo, l.fechaPublicacion, l.precio, l.editorial.nombre, l.editorial.pais
        )
        FROM Libro l JOIN l.editorial
        WHERE l.fechaPublicacion BETWEEN :startDate AND :endDate
        ORDER BY l.fechaPublicacion DESC
        """)
    Slice<LibroResumenDTO> findByFechaPublicacionBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    // ===== BÚSQUEDA COMBINADA (país + género) =====
    @Query("""
        SELECT new com.librotech.dto.LibroResumenDTO(
            l.id, l.titulo, l.fechaPublicacion, l.precio, l.editorial.nombre, l.editorial.pais
        )
        FROM Libro l JOIN l.editorial e JOIN l.generos g
        WHERE (:pais IS NULL OR LOWER(e.pais) LIKE LOWER(CONCAT('%', :pais, '%')))
        AND (:generoId IS NULL OR g.id = :generoId)
        ORDER BY l.fechaPublicacion DESC
        """)
    Slice<LibroResumenDTO> searchLibros(
        @Param("pais") String pais,
        @Param("generoId") Long generoId,
        Pageable pageable
    );
```

> 🔑 **Observe:** Todas las consultas usan `ORDER BY l.fechaPublicacion DESC` para garantizar ordenamiento cronológico consistente.

---

### Actividad 2 — Servicio de Búsqueda

Agregue los métodos de búsqueda al `LibroService`:

```java
// Agregar a LibroService.java

import com.librotech.dto.LibroResumenDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

    public Slice<LibroResumenDTO> searchLibros(String pais, Long generoId, int page) {
        PageRequest pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);

        // Si ambos filtros están presentes, búsqueda combinada
        if (pais != null && !pais.isBlank() && generoId != null) {
            return libroRepository.searchLibros(pais, generoId, pageable);
        }
        // Solo país
        if (pais != null && !pais.isBlank()) {
            return libroRepository.findByPais(pais, pageable);
        }
        // Solo género
        if (generoId != null) {
            return libroRepository.findByGeneroId(generoId, pageable);
        }
        // Sin filtros: catálogo completo
        return libroRepository.findAllLibroResumenes(pageable);
    }
```

---

### Actividad 3 — Evolucionar Swagger/OpenAPI

Actualice el controlador REST con anotaciones de documentación:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/libros")
@Tag(name = "Libros", description = "Gestión del catálogo de libros de LibroTech")
public class LibroController {

    @Autowired
    private LibroService libroService;

    @Operation(
        summary = "Buscar libros con filtros opcionales",
        description = """
            Retorna un Slice paginado de libros en formato DTO liviano (LibroResumenDTO).
            Los filtros son opcionales y combinables. Los resultados están ordenados por
            fecha de publicación descendente. Los registros con borrado lógico (disponible=false)
            son excluidos automáticamente por @SQLRestriction y NUNCA aparecen en los resultados.
            """
    )
    @GetMapping
    public ResponseEntity<Map<String, Object>> searchLibros(
            @Parameter(description = "Filtro parcial por país de editorial (insensible a mayúsculas). Ej: 'esp' encuentra 'España'")
            @RequestParam(required = false) String pais,

            @Parameter(description = "ID de género para filtrar. Ej: 1=Ficción, 2=No Ficción")
            @RequestParam(required = false) Long generoId,

            @Parameter(description = "Número de página (0-indexed)")
            @RequestParam(defaultValue = "0") int page) {

        Slice<LibroResumenDTO> slice = libroService.searchLibros(pais, generoId, page);

        Map<String, Object> response = new HashMap<>();
        response.put("libros", slice.getContent());
        response.put("currentPage", slice.getNumber());
        response.put("hasNext", slice.hasNext());
        response.put("hasPrevious", slice.hasPrevious());
        response.put("filters", Map.of(
            "pais", pais != null ? pais : "",
            "generoId", generoId != null ? generoId : ""
        ));

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Descatalogar libro (borrado lógico)",
        description = """
            Realiza un soft delete: marca el libro como disponible=false.
            El registro permanece en la base de datos para trazabilidad,
            pero desaparece de todas las consultas del catálogo.
            """
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLibro(@PathVariable Long id) {
        libroService.descatalogarLibro(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

### Actividad 4 — Controlador UI con Navegación Paginada

Cree o actualice el controlador de interfaz de usuario:

```java
package com.librotech.controller.ui;

import com.librotech.dto.LibroResumenDTO;
import com.librotech.model.Genero;
import com.librotech.service.GeneroService;
import com.librotech.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/libros")
public class LibroUIController {

    @Autowired
    private LibroService libroService;

    @Autowired
    private GeneroService generoService;

    @GetMapping
    public String listarLibros(
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) Long generoId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // Obtener libros filtrados y paginados
        Slice<LibroResumenDTO> slice = libroService.searchLibros(pais, generoId, page);

        // Cargar géneros para el dropdown de filtro
        List<Genero> generos = generoService.findAll();

        // Pasar datos al modelo
        model.addAttribute("libros", slice.getContent());
        model.addAttribute("currentPage", slice.getNumber());
        model.addAttribute("hasNext", slice.hasNext());
        model.addAttribute("hasPrevious", slice.hasPrevious());
        model.addAttribute("generos", generos);

        // Mantener los filtros activos en el modelo
        model.addAttribute("filterPais", pais != null ? pais : "");
        model.addAttribute("filterGeneroId", generoId);

        return "libros/catalogo";
    }
}
```

---

### Actividad 5 — Vista Thymeleaf con Filtros y Paginación

Cree `src/main/resources/templates/libros/catalogo.html`:

```html
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Catálogo de Libros - LibroTech</title>
    <style>
        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; background: #f5f7fa; }
        .header { background: linear-gradient(135deg, #2c3e50 0%, #3498db 100%);
                   color: white; padding: 20px 40px; }
        .header h1 { margin: 0; }
        .container { max-width: 1100px; margin: 20px auto; padding: 0 20px; }

        /* Filtros */
        .filters { background: white; padding: 20px; border-radius: 8px;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.08); margin-bottom: 20px;
                    display: flex; gap: 15px; align-items: flex-end; flex-wrap: wrap; }
        .filter-group { display: flex; flex-direction: column; }
        .filter-group label { font-weight: 600; margin-bottom: 5px; font-size: 0.9em; color: #555; }
        .filter-group input, .filter-group select {
            padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 0.95em; }
        .btn-search { padding: 8px 20px; background: #3498db; color: white;
                       border: none; border-radius: 6px; cursor: pointer; font-size: 0.95em; }
        .btn-clear { padding: 8px 20px; background: #e0e0e0; color: #333;
                      border: none; border-radius: 6px; cursor: pointer; text-decoration: none;
                      font-size: 0.95em; display: inline-block; }

        /* Tabla */
        table { width: 100%; border-collapse: collapse; background: white;
                border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.08); }
        th { background: #2c3e50; color: white; padding: 12px 15px; text-align: left; }
        td { padding: 12px 15px; border-bottom: 1px solid #eee; }
        tr:hover { background: #f0f7ff; }

        /* Paginación */
        .pagination { display: flex; justify-content: center; gap: 10px; margin-top: 20px; }
        .pagination a { padding: 10px 20px; background: #3498db; color: white;
                         border-radius: 6px; text-decoration: none; }
        .pagination a.disabled { background: #ccc; pointer-events: none; }
        .empty-state { text-align: center; padding: 60px; color: #888; }
    </style>
</head>
<body>

    <div class="header">
        <h1>📚 LibroTech — Catálogo de Libros</h1>
    </div>

    <div class="container">

        <!-- ===== BARRA DE FILTROS ===== -->
        <form class="filters" th:action="@{/admin/libros}" method="get">
            <div class="filter-group">
                <label for="pais">País de Editorial</label>
                <input type="text" id="pais" name="pais"
                       th:value="${filterPais}" placeholder="Ej: España" />
            </div>

            <div class="filter-group">
                <label for="generoId">Género Literario</label>
                <select id="generoId" name="generoId">
                    <option value="">— Todos —</option>
                    <option th:each="gen : ${generos}"
                            th:value="${gen.id}"
                            th:text="${gen.nombre}"
                            th:selected="${filterGeneroId != null AND filterGeneroId == gen.id}">
                    </option>
                </select>
            </div>

            <button type="submit" class="btn-search">🔍 Buscar</button>
            <a th:href="@{/admin/libros}" class="btn-clear">Limpiar</a>
        </form>

        <!-- ===== TABLA DE LIBROS ===== -->
        <div th:if="${#lists.isEmpty(libros)}" class="empty-state">
            <h2>No se encontraron libros</h2>
            <p>Intente con otros filtros de búsqueda.</p>
        </div>

        <table th:unless="${#lists.isEmpty(libros)}">
            <thead>
                <tr>
                    <th>Título</th>
                    <th>Fecha Publicación</th>
                    <th>Precio</th>
                    <th>Editorial</th>
                    <th>País</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="libro : ${libros}">
                    <td th:text="${libro.titulo()}">Título del libro</td>
                    <td th:text="${libro.fechaPublicacion()}">2026-01-01</td>
                    <td th:text="${'$' + #numbers.formatDecimal(libro.precio(), 0, 'COMMA', 0, 'POINT')}">$0</td>
                    <td th:text="${libro.editorialNombre()}">Editorial</td>
                    <td th:text="${libro.pais()}">País</td>
                </tr>
            </tbody>
        </table>

        <!-- ===== PAGINACIÓN CON FILTROS PERSISTENTES ===== -->
        <div class="pagination">
            <!--
                CLAVE: Los enlaces de paginación MANTIENEN los filtros activos.
                Si el usuario buscó pais=España, al pasar a la página 2
                la URL será: /admin/libros?pais=España&page=1
            -->
            <a th:href="@{/admin/libros(
                    pais=${filterPais},
                    generoId=${filterGeneroId},
                    page=${currentPage - 1})}"
               th:classappend="${!hasPrevious} ? 'disabled' : ''">
                ← Anterior
            </a>

            <span style="padding: 10px; font-weight: bold;"
                  th:text="'Página ' + (${currentPage} + 1)">Página 1</span>

            <a th:href="@{/admin/libros(
                    pais=${filterPais},
                    generoId=${filterGeneroId},
                    page=${currentPage + 1})}"
               th:classappend="${!hasNext} ? 'disabled' : ''">
                Siguiente →
            </a>
        </div>

    </div>

</body>
</html>
```

> 🔑 **Observe los enlaces de paginación:** Incluyen `pais=${filterPais}` y `generoId=${filterGeneroId}` para que los filtros se **mantengan al cambiar de página**.

---

### Actividad 6 — Verificación Integral

1. **Arranque la aplicación** y visite `http://localhost:8080/admin/libros`.
2. **Busque por país** escribiendo "esp" → debería encontrar libros de editoriales españolas.
3. **Filtre por género** seleccionando "Ficción" → solo libros de ficción.
4. **Navegue entre páginas** usando Anterior/Siguiente → verifique que los filtros se mantienen en la URL.
5. **Visite Swagger** en `http://localhost:8080/swagger-ui.html` → verifique que los endpoints documentan los filtros y el soft delete.
6. **Revise la consola SQL** → cada búsqueda debe generar UNA sola query con WHERE y JOIN.

---

## 🏋️ Retos para el Coder

### Reto 1 — Filtro por Rango de Fechas (Obligatorio)
Agregue dos campos `<input type="date">` al formulario de filtros para buscar por rango de fechas de publicación (`startDate` y `endDate`). Conecte con el método `findByFechaPublicacionBetween` del repositorio. Los filtros de fecha también deben persistir al paginar.

### Reto 2 — Formulario Relacional de Creación
Cree una vista `libros/formulario.html` para registrar nuevos libros que incluya:
- Un `<select>` (desplegable) para seleccionar la **Editorial** asignada.
- Un grupo de `<input type="checkbox">` para seleccionar los **Géneros**.

Implemente el controlador POST que procese el formulario y guarde el libro con sus relaciones.

### Reto 3 — Búsqueda por Año de Fundación Mínimo
Agregue un filtro numérico "Año mínimo de fundación de editorial" al motor de búsqueda. Cree el método en el repositorio:

```java
@Query("""
    SELECT new com.librotech.dto.LibroResumenDTO(...)
    FROM Libro l JOIN l.editorial e
    WHERE e.fundadoEn >= :minFundadoEn
    ORDER BY l.fechaPublicacion DESC
    """)
Slice<LibroResumenDTO> findByMinFundadoEn(@Param("minFundadoEn") Integer minFundadoEn, Pageable pageable);
```

### Reto 4 — Búsqueda Combinada Completa (Avanzado)
Implemente un **único método JPQL dinámico** que acepte TODOS los filtros opcionales simultáneamente (país, género, rango de fechas, año de fundación):

```java
@Query("""
    SELECT new com.librotech.dto.LibroResumenDTO(...)
    FROM Libro l JOIN l.editorial e LEFT JOIN l.generos g
    WHERE (:pais IS NULL OR LOWER(e.pais) LIKE LOWER(CONCAT('%', :pais, '%')))
    AND (:generoId IS NULL OR g.id = :generoId)
    AND (:startDate IS NULL OR l.fechaPublicacion >= :startDate)
    AND (:endDate IS NULL OR l.fechaPublicacion <= :endDate)
    AND (:minFundadoEn IS NULL OR e.fundadoEn >= :minFundadoEn)
    ORDER BY l.fechaPublicacion DESC
    """)
Slice<LibroResumenDTO> advancedSearch(...);
```

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| El repositorio tiene métodos de búsqueda por país, género y fechas | ☐ |
| Las búsquedas son parciales e insensibles a mayúsculas (`LIKE` + `LOWER`) | ☐ |
| Swagger documenta los filtros con `@Parameter` y describe el soft delete | ☐ |
| Los esquemas de Swagger reflejan `LibroResumenDTO` (Records) | ☐ |
| La vista Thymeleaf tiene barra de filtros funcional | ☐ |
| La paginación mantiene los filtros activos en la URL al navegar | ☐ |
| Los botones Anterior/Siguiente se deshabilitan según el estado del Slice | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Por qué es importante que la búsqueda por país sea **parcial** (con `LIKE '%esp%'`) en lugar de **exacta** (`= 'España'`)? ¿Qué impacto tiene en la experiencia de usuario?
2. ¿Qué pasaría si no propagáramos los filtros en los enlaces de paginación? Describa el comportamiento que vería el usuario al hacer clic en "Siguiente".
3. ¿Por qué Swagger debe reflejar que usamos `LibroResumenDTO` (Record) y no la entidad `Libro`? ¿Qué información sensible podría exponer la entidad?
4. En el Reto 4, la consulta con múltiples `IS NULL OR ...` puede tener problemas de rendimiento con millones de registros. Investigue: ¿qué es **Criteria API** o **Specification** en Spring Data JPA y cómo ayuda a construir consultas dinámicas de forma más eficiente?
