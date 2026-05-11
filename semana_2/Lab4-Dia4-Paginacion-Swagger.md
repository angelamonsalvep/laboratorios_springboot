# 🧪 LABORATORIO DÍA 4 — Paginación, Ordenamiento y Documentación con Swagger

**Módulo 6.1 · Semana 2 · Proyecto LibroTech 📚**  
**Duración estimada:** 2 horas  
**Nivel:** Intermedio-Avanzado  

---

## 📋 Objetivo del Laboratorio

Al finalizar esta práctica, el estudiante será capaz de:

1. Implementar **Paginación** y **Ordenamiento** en los listados para mejorar el rendimiento de la API.
2. Utilizar las interfaces `Pageable` y `Sort` de Spring Data JPA.
3. Configurar **Swagger UI** (OpenAPI) para documentar y probar la API desde el navegador.
4. Generar datos de prueba automáticamente al iniciar la aplicación con `CommandLineRunner`.

---

## 📖 Contexto de Negocio — LibroTech

El catálogo de **LibroTech** está creciendo rápidamente. Cargar 500 libros en una sola petición satura la red y la memoria del navegador. El negocio requiere que los libros se entreguen en "páginas" de 10 en 10, y que se puedan ordenar alfabéticamente por título para facilitar la navegación del usuario.

---

## 🧠 Contexto Conceptual: Paginación y Swagger

### Paginación:
Consiste en dividir un conjunto de resultados en bloques más pequeños. Spring Data JPA maneja esto mediante:
- `Pageable`: Interfaz que define qué página y qué tamaño se solicita.
- `Page<T>`: Objeto de retorno que contiene los datos y los metadatos de la página (total de páginas, total de elementos).

### Swagger (OpenAPI):
Es una herramienta que escanea nuestro código y genera una página web interactiva donde cualquier persona puede ver qué endpoints existen, qué parámetros reciben y probarlos sin necesidad de Postman.

---

## 🔧 Prerrequisitos

- Proyecto del Día 3 funcionando.
- Añada esta dependencia en su `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

---

## 📝 Actividades

### Actividad 1 — Poblar Datos con `CommandLineRunner`

Cree una clase `DataSeed` para insertar 50 libros automáticamente al arrancar la aplicación:

```java
@Component
public class DataSeed implements CommandLineRunner {

    @Autowired
    private LibroRepository libroRepository;

    @Override
    public void run(String... args) {
        if (libroRepository.count() == 0) {
            for (int i = 1; i <= 50; i++) {
                libroRepository.save(new Libro(null, "Libro de Prueba " + i, "Autor " + (i % 5), "ISBN-" + i, 2020 + (i % 5)));
            }
            System.out.println("✅ Datos de prueba insertados.");
        }
    }
}
```

---

### Actividad 2 — Implementar Paginación en el Servicio

Actualice `LibroService` para manejar objetos `Pageable`:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// ...
public Page<Libro> listarPaginado(Pageable pageable) {
    return libroRepository.findAll(pageable);
}
```

---

### Actividad 3 — Endpoint Paginado en el Controlador

Modifique el método `listar` para recibir los parámetros de paginación automáticamente:

```java
@GetMapping
public ResponseEntity<Page<Libro>> listar(
        @PageableDefault(size = 10, sort = "titulo") Pageable pageable) {
    Page<Libro> libros = libroService.listarPaginado(pageable);
    return ResponseEntity.ok(libros);
}
```

---

### Actividad 4 — Pruebas de Paginación y Ordenamiento

Pruebe estas URLs en su navegador o Postman:
1. **Listar página 0 con 5 libros:** `http://localhost:8080/api/libros?page=0&size=5`
2. **Ordenar por autor descendente:** `http://localhost:8080/api/libros?sort=autor,desc`

Observe el JSON de respuesta. Contiene campos como `totalElements`, `totalPages`, `last`, etc.

---

### Actividad 5 — Explorar Swagger UI

Inicie su aplicación y acceda a:  
`http://localhost:8080/swagger-ui.html`

- Observe cómo se listan todos sus controladores.
- Pruebe a realizar un POST desde Swagger.
- Verifique que la documentación de los parámetros `page` y `size` aparece automáticamente.

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| El listado devuelve un objeto `Page` con metadatos | ☐ |
| Funcionan los parámetros `page`, `size` y `sort` en la URL | ☐ |
| Se cargan 50 libros automáticamente al iniciar | ☐ |
| Swagger UI está configurado y accesible | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Qué campo del JSON devuelto por la paginación nos indica cuántos registros hay en total en la base de datos?
2. ¿Por qué es útil la paginación para una aplicación móvil con conexión lenta?
3. ¿Cómo ayuda Swagger a mejorar la comunicación entre el desarrollador Backend y el desarrollador Frontend?
