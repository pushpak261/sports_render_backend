package com.ecommerce.sportshub.repository;

import com.ecommerce.sportshub.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository<Category, Long> {
}
