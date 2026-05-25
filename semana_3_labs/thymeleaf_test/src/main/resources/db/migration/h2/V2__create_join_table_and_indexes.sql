-- V2: Tabla intermedia Many-to-Many e índices de rendimiento

CREATE TABLE libros_generos (
    libro_id BIGINT NOT NULL,
    genero_id BIGINT NOT NULL,
    PRIMARY KEY (libro_id, genero_id),
    CONSTRAINT fk_lg_libro FOREIGN KEY (libro_id) REFERENCES libros(id),
    CONSTRAINT fk_lg_genero FOREIGN KEY (genero_id) REFERENCES generos(id)
);

-- Índices para optimizar consultas frecuentes
CREATE INDEX idx_libros_anio ON libros(anio_publicacion);
CREATE INDEX idx_libros_editorial ON libros(editorial_id);
CREATE INDEX idx_editoriales_pais ON editoriales(pais);