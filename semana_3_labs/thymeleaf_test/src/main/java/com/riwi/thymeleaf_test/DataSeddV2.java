package com.riwi.thymeleaf_test;

import com.riwi.thymeleaf_test.model.Editorial;
import com.riwi.thymeleaf_test.model.Libro;
import com.riwi.thymeleaf_test.repository.EditorialRepository;
import com.riwi.thymeleaf_test.repository.LibroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Component
public class DataSeddV2 implements CommandLineRunner {

    private final LibroRepository libroRepository;
    private final EditorialRepository editorialRepository;

    public DataSeddV2(LibroRepository libroRepository, EditorialRepository editorialRepository) {
        this.libroRepository = libroRepository;
        this.editorialRepository = editorialRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (libroRepository.count() == 0) {

            Editorial editorial = new Editorial();
            editorial.setNombre("Editorial Prueba");
            editorial.setDireccion("Dirección de prueba");
            editorial.setPais("Colombia");
            editorial.setFundadoEn(2000);

            editorial = editorialRepository.save(editorial);

            for (int i = 1; i <= 50; i++) {
                libroRepository.save(
                        new Libro(
                                "Libro de Prueba " + i,
                                "Autor " + (i % 5),
                                "ISBN-" + i,
                                2020 + (i % 5),
                                editorial
                        )
                );
            }

            System.out.println("✅ Datos de prueba insertados.");
        }



    }
}
