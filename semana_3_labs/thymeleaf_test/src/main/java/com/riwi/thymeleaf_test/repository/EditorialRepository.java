package com.riwi.thymeleaf_test.repository;

import com.riwi.thymeleaf_test.model.Editorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EditorialRepository extends JpaRepository<Editorial, Long> {
}
