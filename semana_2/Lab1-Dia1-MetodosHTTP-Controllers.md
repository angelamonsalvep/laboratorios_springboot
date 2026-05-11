# 🧪 LABORATORIO DÍA 1 — Arquitectura MVC: Controladores y Métodos HTTP

**Módulo 6.1 · Semana 2 · Proyecto LibroTech 📚**  
**Duración estimada:** 2 horas  
**Nivel:** Intermedio  

---

## 📋 Objetivo del Laboratorio

Al finalizar esta práctica, el estudiante será capaz de:

1. Comprender el patrón **MVC (Model-View-Controller)** aplicado al desarrollo de APIs REST con Spring Boot.
2. Implementar un **Controlador** (`@RestController`) para manejar peticiones externas.
3. Definir un **Modelo** (clase Java) para representar los datos de la aplicación.
4. Mapear métodos HTTP (**GET** y **POST**) a acciones específicas del controlador.

---

## 📖 Contexto de Negocio — LibroTech

**LibroTech** es un nuevo sistema de gestión para una red de bibliotecas comunitarias. En esta primera fase, el negocio requiere una herramienta que permita catalogar libros y consultar el inventario actual. El objetivo es que los bibliotecarios puedan registrar títulos nuevos y ver la lista completa de libros disponibles.

---

## 🧠 Contexto Conceptual: El Patrón MVC en REST

En Spring Boot, cuando desarrollamos APIs REST, el patrón **MVC** se manifiesta de la siguiente manera:

- **Modelo (Model):** Son las clases Java que representan los datos (ej. `Libro`).
- **Vista (View):** En REST, la "vista" es la representación en **JSON** que el controlador devuelve automáticamente al cliente.
- **Controlador (Controller):** Es la clase que recibe la petición HTTP, procesa los datos y decide qué responder.

### Anotaciones Clave:
- `@RestController`: Indica que la clase es un controlador donde cada método devuelve un objeto convertido automáticamente a JSON.
- `@RequestMapping`: Define la ruta base (URL) para todos los métodos del controlador.
- `@GetMapping`: Mapea peticiones de consulta (lectura).
- `@PostMapping`: Mapea peticiones de creación (envío de datos).
- `@RequestBody`: Convierte el JSON recibido en el cuerpo de la petición en un objeto Java.

---

## 📝 Actividades

### Actividad 1 — Crear el Modelo `Libro`

Cree el paquete `com.librotech.model` y dentro de él la clase `Libro`. Esta clase representa nuestro **Modelo** de datos.

```java
package com.librotech.model;

public class Libro {

    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private int anioPublicacion;

    // Constructor vacío
    public Libro() {}

    // Constructor con parámetros
    public Libro(Long id, String titulo, String autor, String isbn, int anioPublicacion) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.anioPublicacion = anioPublicacion;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getAnioPublicacion() { return anioPublicacion; }
    public void setAnioPublicacion(int anioPublicacion) { this.anioPublicacion = anioPublicacion; }
}
```

---

### Actividad 2 — Implementar el Controlador `LibroController`

Cree el paquete `com.librotech.controller` y la clase `LibroController`. Este será nuestro mediador en el patrón **MVC**.

```java
package com.librotech.controller;

import com.librotech.model.Libro;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    // Almacenamiento temporal en memoria (Solo para el Día 1)
    private List<Libro> libros = new ArrayList<>();
    private Long nextId = 1L;

    // ──────────────────────────────────────────────
    // GET — Listar todos los libros (Lectura)
    // ──────────────────────────────────────────────
    @GetMapping
    public List<Libro> listarLibros() {
        return libros;
    }

    // ──────────────────────────────────────────────
    // POST — Registrar un nuevo libro (Creación)
    // ──────────────────────────────────────────────
    @PostMapping
    public Libro crearLibro(@RequestBody Libro libro) {
        libro.setId(nextId++);
        libros.add(libro);
        return libro; // Spring lo convierte a JSON automáticamente (Vista)
    }
}
```

---

### Actividad 3 — Pruebas con Postman

Inicie su aplicación y realice las siguientes pruebas:

**3.1 — Registrar un libro (POST)**
- **URL:** `http://localhost:8080/api/libros`
- **Body (JSON):**
```json
{
    "titulo": "Cien años de soledad",
    "autor": "Gabriel García Márquez",
    "isbn": "978-0307474728",
    "anioPublicacion": 1967
}
```
✅ **Resultado:** Debe recibir el mismo JSON con un `id` asignado.

**3.2 — Listar libros (GET)**
- **URL:** `http://localhost:8080/api/libros`
✅ **Resultado:** Un array JSON con los libros que haya creado.

---

### Actividad 4 — Práctica Autónoma: Modelo `Categoria`

Para fortalecer el patrón MVC, implemente la gestión de categorías:

1. Cree el modelo `Categoria` con `id` y `nombre`.
2. Cree `CategoriaController` en la ruta `/api/categorias`.
3. Implemente los métodos **GET** y **POST**.
4. Pruebe el registro de categorías como "Ficción", "Terror" o "Historia".

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Se diferencia claramente entre el Modelo y el Controlador | ☐ |
| El controlador usa `@RestController` y `@RequestMapping` | ☐ |
| El método POST recibe datos mediante `@RequestBody` | ☐ |
| Se pudo registrar y listar datos desde Postman | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Por qué en una arquitectura MVC el controlador no debería contener la lista de datos a largo plazo? (Anticipación al Día 2).
2. ¿Qué ventaja tiene que Spring convierta automáticamente los objetos Java a JSON?
3. Si quitamos la anotación `@RequestBody`, ¿qué sucede cuando intentamos enviar datos desde Postman?
