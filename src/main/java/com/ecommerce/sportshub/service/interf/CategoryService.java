package com.ecommerce.sportshub.service.interf;


import com.ecommerce.sportshub.dto.CategoryDto;
import com.ecommerce.sportshub.dto.Response;

public interface CategoryService {

    Response createCategory(CategoryDto categoryRequest);
    Response updateCategory(Long categoryId, CategoryDto categoryRequest);
    Response getAllCategories();
    Response getCategoryById(Long categoryId);
    Response deleteCategory(Long categoryId);
}
