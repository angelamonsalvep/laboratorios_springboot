-- V1: Creación de las tablas principales del sistema LibroTech

CREATE TABLE editoriales (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    direccion VARCHAR(500) NOT NULL,
    pais VARCHAR(100) NOT NULL,
    fundado_en INT
);

CREATE TABLE generos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500)
);

CREATE TABLE libros (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    anio_publicacion INT NOT NULL,
    editorial_id BIGINT NOT NULL,

    CONSTRAINT fk_libro_editorial
        FOREIGN KEY (editorial_id)
        REFERENCES editoriales(id)
);