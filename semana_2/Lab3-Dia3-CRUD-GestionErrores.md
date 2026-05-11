# 🧪 LABORATORIO DÍA 3 — CRUD Completo y Gestión de Errores con ResponseEntity

**Módulo 6.1 · Semana 2 · Proyecto LibroTech 📚**  
**Duración estimada:** 2 horas  
**Nivel:** Intermedio  

---

## 📋 Objetivo del Laboratorio

Al finalizar esta práctica, el estudiante será capaz de:

1. Completar las operaciones CRUD implementando **Actualización (PUT)** y **Eliminación (DELETE)**.
2. Utilizar la clase `ResponseEntity` para controlar los códigos de estado HTTP (200, 201, 204, 404).
3. Implementar lógica de validación para manejar el escenario de "Recurso No Encontrado".
4. Manejar parámetros en la URL mediante `@PathVariable`.

---

## 📖 Contexto de Negocio — LibroTech

Para que la gestión del catálogo de **LibroTech** sea efectiva, el sistema debe permitir corregir errores en los registros de libros (actualizar título, autor, etc.) o dar de baja libros dañados o perdidos. Además, es vital que si un bibliotecario busca un libro que no existe, el sistema responda de forma profesional con un error claro.

---

## 🧠 Contexto Conceptual: Códigos HTTP y ResponseEntity

En una API REST bien diseñada, no basta con devolver datos; debemos devolver el **Código de Estado** adecuado para que el cliente sepa qué ocurrió:

| Código | Significado         | Cuándo usarlo |
|--------|---------------------|---------------|
| **200 OK** | Éxito               | GET exitoso o PUT exitoso. |
| **201 Created** | Creado             | Tras un POST exitoso. |
| **204 No Content** | Sin contenido    | Tras un DELETE exitoso. |
| **404 Not Found** | No encontrado     | Cuando el ID solicitado no existe. |

`ResponseEntity<T>` es una clase de Spring que nos permite envolver nuestra respuesta y asignar manualmente estos códigos.

---

## 📝 Actividades

### Actividad 1 — Expandir el Servicio `LibroService`

Añada los métodos para buscar por ID, actualizar y eliminar:

```java
@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    // ... métodos anteriores ...

    public Optional<Libro> obtenerPorId(Long id) {
        return libroRepository.findById(id);
    }

    public Optional<Libro> actualizar(Long id, Libro libroActualizado) {
        return libroRepository.findById(id).map(libroExistente -> {
            libroExistente.setTitulo(libroActualizado.getTitulo());
            libroExistente.setAutor(libroActualizado.getAutor());
            libroExistente.setIsbn(libroActualizado.getIsbn());
            libroExistente.setAnioPublicacion(libroActualizado.getAnioPublicacion());
            return libroRepository.save(libroExistente);
        });
    }

    public boolean eliminar(Long id) {
        if (libroRepository.existsById(id)) {
            libroRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
```

---

### Actividad 2 — Refactorizar el Controlador con `ResponseEntity`

Implemente el CRUD completo en `LibroController` manejando los códigos de respuesta:

```java
@RestController
@RequestMapping("/api/libros")
public class LibroController {

    @Autowired
    private LibroService libroService;

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Optional<Libro> libro = libroService.obtenerPorId(id);
        if (libro.isPresent()) {
            return ResponseEntity.ok(libro.get()); // 200 OK
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Libro con ID " + id + " no encontrado."); // 404
    }

    @PostMapping
    public ResponseEntity<Libro> crear(@RequestBody Libro libro) {
        Libro nuevoLibro = libroService.guardar(libro);
        return new ResponseEntity<>(nuevoLibro, HttpStatus.CREATED); // 201
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Libro libro) {
        Optional<Libro> actualizado = libroService.actualizar(id, libro);
        if (actualizado.isPresent()) {
            return ResponseEntity.ok(actualizado.get()); // 200
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No se pudo actualizar. Libro no encontrado."); // 404
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (libroService.eliminar(id)) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.notFound().build(); // 404
    }
}
```

---

### Actividad 3 — Pruebas de Calidad en Postman

Realice las siguientes pruebas y observe los códigos HTTP en la respuesta de Postman:

1. **GET a un ID que no existe:** ¿Recibe un 404 y el mensaje personalizado?
2. **PUT a un ID existente:** ¿Los datos se actualizan correctamente?
3. **DELETE a un ID existente:** ¿Recibe un 204 (sin cuerpo)?
4. **DELETE a un ID inexistente:** ¿Recibe un 404?

---

### Actividad 4 — Práctica Autónoma: CRUD para `Categoria`

Aplique la misma lógica para el modelo `Categoria`:
1. Implemente los métodos `actualizar` y `eliminar` en su servicio.
2. Use `ResponseEntity` en el controlador de categorías para manejar los códigos 204 y 404.

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Se implementaron los 4 métodos HTTP básicos (CRUD) | ☐ |
| Se utiliza `ResponseEntity` en todos los endpoints | ☐ |
| El borrado exitoso devuelve `204 No Content` | ☐ |
| Se maneja correctamente el escenario de ID no existente (404) | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Por qué el método DELETE suele devolver `204 No Content` en lugar de un mensaje de texto?
2. ¿Qué diferencia hay entre usar `ResponseEntity.ok(objeto)` y `ResponseEntity.status(HttpStatus.OK).body(objeto)`?
3. ¿Por qué es importante validar la existencia de un recurso antes de intentar actualizarlo?
