# 🧪 LABORATORIO DÍA 1 — Modelado Relacional, Soft Delete y Migraciones con Flyway

**Módulo 6.1 · Semana 4 · Proyecto LibroTech 📚**  
**Duración estimada:** 3 horas  
**Nivel:** Avanzado  

---

## 📋 Objetivo del Laboratorio

Al finalizar esta práctica, el estudiante será capaz de:

1. Diseñar un **modelo relacional complejo** con relaciones `@ManyToOne` y `@ManyToMany` en JPA.
2. Implementar **borrado lógico (Soft Delete)** transparente mediante `@SQLRestriction`.
3. Configurar **Flyway** como motor de migraciones versionadas para el control total del esquema de base de datos.
4. Crear **scripts SQL secuenciales** (`V1`, `V2`, `V3`) que evolucionen el esquema y siembren datos masivos.
5. Habilitar la **auditoría SQL en consola** para observar las sentencias generadas por Hibernate.

---

## 📖 Contexto de Negocio — LibroTech

**LibroTech** ha crecido. Ya no basta con tener libros sueltos: el negocio necesita que cada libro esté **vinculado obligatoriamente a una editorial (Editorial)** y **clasificado en múltiples géneros literarios (Genero)**. Además, por normativas de inventario, queda **prohibida la eliminación física de registros**: los libros descatalogados deben trazarse mediante un sistema de borrado lógico. Finalmente, la gestión del esquema de base de datos ya no puede depender de `ddl-auto=update`; necesitamos migraciones versionadas y profesionales con **Flyway**.

---

## 🧠 Contexto Conceptual

### Relaciones JPA — Repaso Rápido

| Relación | Ejemplo en LibroTech | Anotación |
|---|---|---|
| **Many-to-One** | Muchos `Libro` pertenecen a una `Editorial` | `@ManyToOne` en Libro |
| **One-to-Many** | Una `Editorial` tiene muchos `Libro` | `@OneToMany(mappedBy)` en Editorial |
| **Many-to-Many** | Un `Libro` tiene muchos `Genero` y viceversa | `@ManyToMany` + `@JoinTable` |

### Soft Delete con `@SQLRestriction`

En lugar de borrar físicamente un registro con `DELETE FROM`, marcamos un campo `disponible = false`. La anotación `@SQLRestriction("disponible = true")` actúa como un **filtro global automático**: Hibernate añade esa condición a **todas** las consultas SELECT sobre esa entidad, de modo que los registros "descatalogados" nunca aparecen en los resultados.

```java
// Hibernate 6.x / Spring Boot 3.x
@SQLRestriction("disponible = true")
public class Libro { ... }
```

> ⚠️ **Nota:** `@SQLRestriction` reemplaza a la antigua `@Where` (deprecada en Hibernate 6.3+).

### Flyway — Migraciones Versionadas

Flyway es una herramienta que gestiona la evolución del esquema de BD mediante **scripts SQL numerados**. Al arrancar la aplicación, Flyway detecta qué scripts ya se ejecutaron (tabla `flyway_schema_history`) y aplica solo los pendientes.

**Convención de nombres:** `V{número}__{descripción}.sql`  
Ejemplo: `V1__create_initial_tables.sql`

---

## 📝 Actividades

### Actividad 1 — Configurar Flyway y Dependencias

**1.1 Agregar la dependencia de Flyway** en su `pom.xml`:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<!-- Si usa MySQL, añada también: -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

**1.2 Configurar `application.properties`** para delegar el control del esquema a Flyway:

```properties
# ===== FLYWAY =====
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# ===== HIBERNATE: Delegamos el esquema a Flyway =====
spring.jpa.hibernate.ddl-auto=validate

# ===== AUDITORÍA SQL EN CONSOLA =====
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

> 💡 **`ddl-auto=validate`** hace que Hibernate **verifique** que las entidades coincidan con el esquema, pero **nunca modifica** las tablas. Todo cambio debe venir de un script Flyway.

**1.3 Crear la carpeta de migraciones:**

```
src/main/resources/db/migration/
```

---

### Actividad 2 — Crear las Entidades del Modelo Relacional

**2.1 Entidad `Editorial` (Editorial/Sello editorial)**

Cree o actualice la entidad `Editorial` en el paquete `model` (o `entity`):

```java
package com.librotech.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "editoriales")
public class Editorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    // Nuevo campo: país de la editorial
    @Column(nullable = false)
    private String pais;

    private Integer fundadoEn;

    // Lado inverso de la relación: una Editorial tiene muchos Libros
    @OneToMany(mappedBy = "editorial")
    private List<Libro> libros = new ArrayList<>();

    // === Constructores ===
    public Editorial() {}

    public Editorial(String nombre, String direccion, String pais, Integer fundadoEn) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.pais = pais;
        this.fundadoEn = fundadoEn;
    }

    // === Getters y Setters (genere todos) ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public Integer getFundadoEn() { return fundadoEn; }
    public void setFundadoEn(Integer fundadoEn) { this.fundadoEn = fundadoEn; }

    public List<Libro> getLibros() { return libros; }
    public void setLibros(List<Libro> libros) { this.libros = libros; }
}
```

**2.2 Entidad `Genero` (Género Literario)**

```java
package com.librotech.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "generos")
public class Genero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    // Lado inverso de Many-to-Many
    @ManyToMany(mappedBy = "generos")
    private Set<Libro> libros = new HashSet<>();

    // === Constructores ===
    public Genero() {}

    public Genero(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // === Getters y Setters (genere todos) ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Set<Libro> getLibros() { return libros; }
    public void setLibros(Set<Libro> libros) { this.libros = libros; }
}
```

**2.3 Entidad `Libro` (Libro) — Con Soft Delete y Relaciones**

```java
package com.librotech.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "libros")
@SQLRestriction("disponible = true")  // ← FILTRO GLOBAL: Solo registros disponibles
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(nullable = false)
    private LocalDate fechaPublicacion;

    private Double precio;

    // === SOFT DELETE ===
    @Column(nullable = false)
    private Boolean disponible = true;

    // === RELACIÓN MANY-TO-ONE: Cada libro pertenece a UNA editorial ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editorial_id", nullable = false)
    private Editorial editorial;

    // === RELACIÓN MANY-TO-MANY: Un libro tiene MUCHOS géneros ===
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "libros_generos",                                // Nombre explícito de tabla intermedia
        joinColumns = @JoinColumn(name = "libro_id"),           // FK hacia Libro
        inverseJoinColumns = @JoinColumn(name = "genero_id")    // FK hacia Genero
    )
    private Set<Genero> generos = new HashSet<>();

    // === Constructores ===
    public Libro() {}

    public Libro(String titulo, String autor, String isbn, LocalDate fechaPublicacion, Double precio, Editorial editorial) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.fechaPublicacion = fechaPublicacion;
        this.precio = precio;
        this.editorial = editorial;
        this.disponible = true;
    }

    // === Método para "descatalogar" lógicamente ===
    public void softDelete() {
        this.disponible = false;
    }

    // === Getters y Setters (genere todos) ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public Editorial getEditorial() { return editorial; }
    public void setEditorial(Editorial editorial) { this.editorial = editorial; }

    public Set<Genero> getGeneros() { return generos; }
    public void setGeneros(Set<Genero> generos) { this.generos = generos; }
}
```

> 🔑 **Puntos clave a observar:**
> - `@SQLRestriction("disponible = true")` en la clase `Libro` filtra automáticamente los descatalogados.
> - `@JoinTable` define explícitamente la tabla intermedia `libros_generos`.
> - `FetchType.LAZY` en ambas relaciones — evitamos cargar datos innecesarios.

---

### Actividad 3 — Crear los Scripts de Migración Flyway

Cree los siguientes archivos dentro de `src/main/resources/db/migration/`:

**3.1 `V1__create_initial_tables.sql`** — Estructura base

```sql
-- V1: Creación de las tablas principales del sistema LibroTech

CREATE TABLE editoriales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    direccion VARCHAR(500) NOT NULL,
    pais VARCHAR(100) NOT NULL,
    fundado_en INT
);

CREATE TABLE generos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500)
);

CREATE TABLE libros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    fecha_publicacion DATE NOT NULL,
    precio DOUBLE,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    editorial_id BIGINT NOT NULL,
    CONSTRAINT fk_libro_editorial FOREIGN KEY (editorial_id) REFERENCES editoriales(id)
);
```

**3.2 `V2__create_join_table_and_indexes.sql`** — Tabla intermedia e índices

```sql
-- V2: Tabla intermedia Many-to-Many e índices de rendimiento

CREATE TABLE libros_generos (
    libro_id BIGINT NOT NULL,
    genero_id BIGINT NOT NULL,
    PRIMARY KEY (libro_id, genero_id),
    CONSTRAINT fk_lg_libro FOREIGN KEY (libro_id) REFERENCES libros(id),
    CONSTRAINT fk_lg_genero FOREIGN KEY (genero_id) REFERENCES generos(id)
);

-- Índices para optimizar consultas frecuentes
CREATE INDEX idx_libros_fecha ON libros(fecha_publicacion);
CREATE INDEX idx_libros_disponible ON libros(disponible);
CREATE INDEX idx_libros_editorial ON libros(editorial_id);
CREATE INDEX idx_editoriales_pais ON editoriales(pais);
```

**3.3 `V3__seed_massive_data.sql`** — Semilla de datos masiva

> ⚠️ Este script debe insertar **al menos 200 registros** de libros. A continuación se muestra una versión reducida como ejemplo. **Su reto es completarlo hasta llegar a 200+**.

```sql
-- V3: Seed de datos masivo para LibroTech

-- Editoriales (10 editoriales de distintos países)
INSERT INTO editoriales (nombre, direccion, pais, fundado_en) VALUES
('Alfaguara', 'Calle Gran Vía 32', 'España', 1964),
('Planeta', 'Av. Diagonal 662-664', 'España', 1949),
('Penguin Random House', '1745 Broadway', 'Estados Unidos', 2013),
('Fondo de Cultura Económica', 'Carretera Picacho-Ajusco 227', 'México', 1934),
('Norma Editorial', 'Calle 10 #24-60', 'Colombia', 1960),
('Anagrama', 'Pedró de la Creu 58', 'España', 1969),
('Siglo XXI Editores', 'Cerro del Agua 248', 'México', 1965),
('Editorial Oveja Negra', 'Cra 14 #79-17', 'Colombia', 1968),
('Tusquets Editores', 'Av. Diagonal 662-664', 'España', 1969),
('Seix Barral', 'Av. Diagonal 662-664', 'Argentina', 1911);

-- Géneros literarios (7 géneros)
INSERT INTO generos (nombre, descripcion) VALUES
('Ficción', 'Narrativa de invención, novelas y cuentos imaginativos'),
('No Ficción', 'Textos basados en hechos reales, ensayos y crónicas'),
('Ciencia Ficción', 'Narrativa especulativa con elementos científicos y tecnológicos'),
('Fantasía', 'Historias en mundos imaginarios con elementos mágicos o sobrenaturales'),
('Terror', 'Obras diseñadas para provocar miedo y tensión en el lector'),
('Historia', 'Textos sobre eventos y personajes históricos documentados'),
('Desarrollo Personal', 'Guías de crecimiento personal, productividad y bienestar');

-- Libros (primeros 20 de ejemplo - COMPLETE HASTA 200)
INSERT INTO libros (titulo, autor, isbn, fecha_publicacion, precio, disponible, editorial_id) VALUES
('Cien años de soledad', 'Gabriel García Márquez', 'ISBN-0001', '1967-06-05', 45000.0, true, 1),
('El amor en los tiempos del cólera', 'Gabriel García Márquez', 'ISBN-0002', '1985-09-05', 42000.0, true, 5),
('1984', 'George Orwell', 'ISBN-0003', '1949-06-08', 38000.0, true, 3),
('Crónica de una muerte anunciada', 'Gabriel García Márquez', 'ISBN-0004', '1981-04-01', 35000.0, true, 8),
('Don Quijote de la Mancha', 'Miguel de Cervantes', 'ISBN-0005', '1605-01-16', 55000.0, true, 2),
('El Principito', 'Antoine de Saint-Exupéry', 'ISBN-0006', '1943-04-06', 28000.0, true, 3),
('Rayuela', 'Julio Cortázar', 'ISBN-0007', '1963-06-28', 40000.0, true, 1),
('La sombra del viento', 'Carlos Ruiz Zafón', 'ISBN-0008', '2001-04-01', 48000.0, true, 2),
('Fahrenheit 451', 'Ray Bradbury', 'ISBN-0009', '1953-10-19', 36000.0, true, 3),
('Dune', 'Frank Herbert', 'ISBN-0010', '1965-08-01', 52000.0, true, 3),
('El nombre de la rosa', 'Umberto Eco', 'ISBN-0011', '1980-01-01', 47000.0, true, 6),
('Los pilares de la Tierra', 'Ken Follett', 'ISBN-0012', '1989-01-01', 58000.0, true, 2),
('It', 'Stephen King', 'ISBN-0013', '1986-09-15', 50000.0, true, 3),
('Sapiens', 'Yuval Noah Harari', 'ISBN-0014', '2011-01-01', 62000.0, true, 4),
('Hábitos atómicos', 'James Clear', 'ISBN-0015', '2018-10-16', 55000.0, true, 3),
('La casa de los espíritus', 'Isabel Allende', 'ISBN-0016', '1982-01-01', 41000.0, true, 9),
('Pedro Páramo', 'Juan Rulfo', 'ISBN-0017', '1955-03-01', 33000.0, true, 4),
('El laberinto de la soledad', 'Octavio Paz', 'ISBN-0018', '1950-01-01', 37000.0, true, 7),
('Frankenstein', 'Mary Shelley', 'ISBN-0019', '1818-01-01', 30000.0, true, 6),
('El resplandor', 'Stephen King', 'ISBN-0020', '1977-01-28', 44000.0, true, 3);

-- Relaciones Libro-Género (asigne géneros a los libros)
INSERT INTO libros_generos (libro_id, genero_id) VALUES
(1, 1), (1, 4),       -- Cien años de soledad: Ficción + Fantasía
(2, 1),               -- El amor en los tiempos del cólera: Ficción
(3, 1), (3, 3),       -- 1984: Ficción + Ciencia Ficción
(4, 1),               -- Crónica de una muerte anunciada: Ficción
(5, 1),               -- Don Quijote: Ficción
(6, 1), (6, 4),       -- El Principito: Ficción + Fantasía
(7, 1),               -- Rayuela: Ficción
(8, 1), (8, 5),       -- La sombra del viento: Ficción + Terror
(9, 1), (9, 3),       -- Fahrenheit 451: Ficción + Ciencia Ficción
(10, 3), (10, 4),     -- Dune: Ciencia Ficción + Fantasía
(11, 1), (11, 6),     -- El nombre de la rosa: Ficción + Historia
(12, 1), (12, 6),     -- Los pilares de la Tierra: Ficción + Historia
(13, 5),              -- It: Terror
(14, 2), (14, 6),     -- Sapiens: No Ficción + Historia
(15, 2), (15, 7),     -- Hábitos atómicos: No Ficción + Desarrollo Personal
(16, 1), (16, 4),     -- La casa de los espíritus: Ficción + Fantasía
(17, 1),              -- Pedro Páramo: Ficción
(18, 2), (18, 6),     -- El laberinto de la soledad: No Ficción + Historia
(19, 1), (19, 5),     -- Frankenstein: Ficción + Terror
(20, 5);              -- El resplandor: Terror
```

---

### Actividad 4 — Actualizar el Servicio con Soft Delete

Modifique su `LibroService` para implementar la descatalogación lógica en lugar de la eliminación física:

```java
// En LibroService.java

public void descatalogarLibro(Long id) {
    Libro libro = libroRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Libro no encontrado con id: " + id));
    
    // En lugar de: libroRepository.delete(libro);
    // Hacemos Soft Delete:
    libro.softDelete();
    libroRepository.save(libro);
}
```

> 💡 Después de hacer `softDelete()`, gracias a `@SQLRestriction`, ese libro **desaparecerá automáticamente** de todas las consultas `findAll()`, `findById()`, etc.

---

### Actividad 5 — Verificar la Migración y la Auditoría SQL

1. **Elimine** o **limpie** su base de datos actual (Flyway necesita partir de cero o usar baseline).
2. **Arranque** la aplicación Spring Boot.
3. **Observe la consola** — debe ver:
   - Flyway ejecutando `V1`, `V2`, `V3` secuencialmente.
   - Las sentencias SQL formateadas con los valores reales de los parámetros.
4. Verifique en su cliente de BD que existen las tablas `editoriales`, `generos`, `libros`, `libros_generos` y `flyway_schema_history`.

---

## 🏋️ Retos para el Coder

### Reto 1 — Completar el Seed Masivo (Obligatorio)
Complete el script `V3` hasta tener **al menos 200 libros** con distribución variada entre editoriales, géneros, fechas de publicación y precios. Use creatividad para los títulos y autores.

### Reto 2 — Implementar el Repositorio de Genero
Cree `GeneroRepository` extendiendo `JpaRepository<Genero, Long>` y un servicio `GeneroService` con operaciones CRUD básicas.

### Reto 3 — Endpoint REST para Soft Delete
Cree un endpoint `DELETE /api/libros/{id}` que invoque el método `descatalogarLibro` del servicio. Después de "descatalogar" un libro, haga `GET /api/libros` y compruebe que ya no aparece en el listado, pero que **sigue existiendo** en la tabla de la base de datos (con `disponible = false`).

### Reto 4 — Crear un Script V4 (Extra)
Cree un nuevo script `V4__add_libro_paginas.sql` que agregue una columna `paginas INT` a la tabla `libros`. Actualice la entidad `Libro` para reflejar el cambio. Observe cómo Flyway aplica solo el script nuevo sin repetir los anteriores.

---

## ✅ Criterios de Evaluación

| Criterio | Cumple |
|----------|--------|
| Flyway está configurado y `ddl-auto` NO es `update` ni `create` | ☐ |
| Los scripts V1, V2 y V3 se ejecutan sin errores al arrancar | ☐ |
| La entidad `Libro` tiene `@SQLRestriction("disponible = true")` | ☐ |
| La relación Libro-Editorial es `@ManyToOne` con `@JoinColumn` explícito | ☐ |
| La relación Libro-Genero es `@ManyToMany` con `@JoinTable` explícito | ☐ |
| La consola muestra SQL formateado con parámetros reales (TRACE) | ☐ |
| El seed contiene al menos 200 registros de libros | ☐ |

---

## 🔍 Preguntas de Reflexión

1. ¿Qué ventaja tiene usar Flyway con `ddl-auto=validate` frente a `ddl-auto=update` en un entorno de producción?
2. Si un libro tiene `disponible = false` y se ejecuta `libroRepository.findById(id)`, ¿qué retornará? ¿Por qué? ¿Cómo podría consultar registros descatalogados si fuera necesario?
3. ¿Qué pasaría si modificara un script Flyway ya ejecutado (por ejemplo, cambiar algo en `V1`)? ¿Cómo manejaría esa situación?
4. ¿Por qué usamos `Set<Genero>` en lugar de `List<Genero>` para la relación Many-to-Many?
