package com.riwi.thymeleaf_test.model;

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

    public Editorial() {
    }

    public Editorial(String nombre, String direccion, String pais, Integer fundadoEn) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.pais = pais;
        this.fundadoEn = fundadoEn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public Integer getFundadoEn() {
        return fundadoEn;
    }

    public void setFundadoEn(Integer fundadoEn) {
        this.fundadoEn = fundadoEn;
    }

    public List<Libro> getLibros() {
        return libros;
    }

    public void setLibros(List<Libro> libros) {
        this.libros = libros;
    }
}
