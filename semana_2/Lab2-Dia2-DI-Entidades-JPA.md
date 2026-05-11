# 🧪 LABORATORIO DÍA 2 — Capas del MVC: Inyección de Dependencias, Servicios y Repositorios

**Módulo 6.1 · Semana 2 · Proyecto LibroTech 📚**  
**Duración estimada:** 2 horas  
**Nivel:** Intermedio  

---

## 📋 Objetivo del Laboratorio

Al finalizar esta práctica, el estudiante será capaz de:

1. Evolucionar la arquitectura MVC hacia una **Arquitectura por Capas** (Controller -> Service -> Repository).
2. Implementar la **Inyección de Dependencias** usando la anotación `@Autowired`.
3. Configurar la persistencia de datos mediante **Spring Data JPA** e **Hibernate**.
4. Definir **Entidades** y crear **Repositorios** para el acceso a base de datos.

---

## 📖 Contexto de Negocio — LibroTech

La gestión de memoria del Día 1 no es suficiente. El negocio de **LibroTech** exige que la información persista aunque el servidor se apague. Además, se requiere separar la lógica de registro (validaciones) de la gestión de la base de datos para facilitar el mantenimiento futuro.

---

## 🧠 Contexto Conceptual: Capas y DI

En una aplicación Spring Boot profesional, el patrón MVC se organiza en capas para separar responsabilidades:

1. **Controlador (`@RestController`):** Maneja las peticiones HTTP (Entrada/Salida).
2. **Servicio (`@Service`):** Contiene la lógica de negocio y las reglas de la aplicación.
3. **Repositorio (`@Repository`):** Se encarga de la comunicación con la base de datos.
4. **Entidad (`@Entity`):** Es el Modelo que representa una tabla en la base de datos.

### Inyección de Dependencias (DI):
Es el mecanismo por el cual Spring "inyecta" (proporciona) una instancia de una clase a otra automáticamente. Usamos la anotación `@Autowired` para esto.

---

## 🔧 Prerrequisitos

- Proyecto del Día 1 funcionando.
- Dependencias en `pom.xml`: `Spring Data JPA` y `H2 Database`.

---

## 📝 Actividades

### Actividad 1 — Configurar la Persistencia (H2)

Configure el acceso a datos en `src/main/resources/application.properties`:

```properties
# Conexión a Base de Datos H2 persistente en archivo
spring.datasource.url=jdbc:h2:file:./data/librotech_db
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Configuración Hibernate / JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Consola H2 para visualización
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

### Actividad 2 — Transformar `Libro` en una Entidad JPA (Model)

Modifique la clase `Libro` en `com.librotech.model` para mapearla a la base de datos:

```java
package com.librotech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 100)
    private String autor;

    @Column(unique = true, length = 20)
    private String isbn;

    private int anioPublicacion;

    public Libro() {}

    // Getters y Setters...
}
```

---

### Actividad 3 — Crear la Capa de Acceso a Datos (Repository)

Cree el paquete `com.librotech.repository` y defina la interfaz `LibroRepository`:

```java
package com.librotech.repository;

import com.librotech.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    // Consulta derivada: buscar libros por autor
    List<Libro> findByAutor(String autor);
}
```

---

### Actividad 4 — Crear la Capa de Lógica de Negocio (Service)

Cree el paquete `com.librotech.service` y la clase `LibroService`:

```java
package com.librotech.service;

import com.librotech.model.Libro;
import com.librotech.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    public List<Libro> obtenerTodos() {
        return libroRepository.findAll();
    }

    public Libro guardar(Libro libro) {
        // Aquí podríamos añadir lógica de negocio (ej. validar el ISBN)
        return libroRepository.save(libro);
    }
}
```

---

### Actividad 5 — Inyectar el Servicio en el Controlador

Refactorice el `LibroController` para eliminar la lista en memoria y usar el servicio:

```java
@RestController
@RequestMapping("/api/libros")
public class LibroController {

    @Autowired
    private LibroService libroService;

    @GetMapping
    public List<Libro> listar() {
        return libroService.obtenerTodos();
    }

    @PostMapping
    public Libro crear(@RequestBody Libro libro) {
        return libroService.guardar(libro);
    }
}
```

---

### Actividad 6 — Verificar la Persistencia

1. Inicie la aplicación.
2. Cree 2 libros mediante Postman (POST).
3. Inicie la consola H2 (`http://localhost:8080/h2-console`) y ejecute `SELECT * FROM libros`.
4. Detenga la aplicación y vuelva a iniciarla. Ejecute un GET en Postman → **Los datos deben seguir ahí**.

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| El modelo `Libro` está anotado correctamente como `@Entity` | ☐ |
| Se implementó la interfaz `LibroRepository` | ☐ |
| Se usa `@Service` y la lógica está separada del controlador | ☐ |
| Se inyectan las dependencias mediante `@Autowired` | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Qué pasaría si intentamos usar el repositorio directamente en el controlador sin pasar por el servicio?
2. ¿Por qué es importante que el repositorio sea una interfaz y no una clase?
3. Explique el flujo de una petición GET desde que llega al controlador hasta que obtiene el dato de la base de datos.
