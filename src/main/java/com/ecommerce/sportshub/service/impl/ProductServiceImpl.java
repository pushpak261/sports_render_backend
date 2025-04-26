package com.ecommerce.sportshub.service.impl;

import com.ecommerce.sportshub.dto.ProductDto;
import com.ecommerce.sportshub.dto.Response;
import com.ecommerce.sportshub.entity.Category;
import com.ecommerce.sportshub.entity.Product;
import com.ecommerce.sportshub.exception.NotFoundException;
import com.ecommerce.sportshub.mapper.EntityDtoMapper;
import com.ecommerce.sportshub.repository.CategoryRepo;
import com.ecommerce.sportshub.repository.ProductRepo;
import com.ecommerce.sportshub.service.interf.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final EntityDtoMapper entityDtoMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    private String saveImageLocally(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            return null;
        }

        // Create full path for upload directory
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created directory: {}", uploadPath);
        }

        // Generate unique filename
        String uniqueFileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);

        // Save the file
        Files.copy(image.getInputStream(), filePath);
        log.info("Saved image at: {}", filePath);

        // Return relative path for serving
        return baseUrl + "/" + uploadDir + "/" + uniqueFileName;
    }

    @Override
    public Response createProduct(Long categoryId, MultipartFile image, String name, String description, BigDecimal price) throws IOException {
        if (categoryId == null || name == null || price == null) {
            return Response.builder()
                    .status(400)
                    .message("Category, name, and price are required")
                    .build();
        }

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        String productImageUrl = saveImageLocally(image);

        Product product = new Product();
        product.setCategory(category);
        product.setPrice(price);
        product.setName(name);
        product.setDescription(description);
        product.setImageUrl(productImageUrl);

        Product savedProduct = productRepo.save(product);
        log.info("Created product with ID: {}", savedProduct.getId());

        return Response.builder()
                .status(200)
                .message("Product successfully created")
                .build();
    }

    @Override
    public Response updateProduct(Long productId, Long categoryId, MultipartFile image, String name, String description, BigDecimal price) throws IOException {
        if (productId == null) {
            return Response.builder()
                    .status(400)
                    .message("Product ID is required")
                    .build();
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        if (categoryId != null) {
            Category category = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            product.setCategory(category);
        }

        if (image != null && !image.isEmpty()) {
            // Delete old image if exists
            String oldImageUrl = product.getImageUrl();
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                // Extract filename from URL
                String oldFilename = oldImageUrl.substring(oldImageUrl.lastIndexOf('/') + 1);
                Path oldFilePath = Paths.get(uploadDir, oldFilename);
                try {
                    Files.deleteIfExists(oldFilePath);
                    log.info("Deleted old image: {}", oldFilePath);
                } catch (IOException e) {
                    log.warn("Failed to delete old image: {}", e.getMessage());
                }
            }

            product.setImageUrl(saveImageLocally(image));
        }

        if (name != null && !name.isBlank()) product.setName(name);
        if (description != null) product.setDescription(description);
        if (price != null) product.setPrice(price);

        productRepo.save(product);
        log.info("Updated product with ID: {}", productId);

        return Response.builder()
                .status(200)
                .message("Product updated successfully")
                .build();
    }

    @Override
    public Response deleteProduct(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        // Delete image file
        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Extract filename from URL
            String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadDir, filename);
            try {
                Files.deleteIfExists(filePath);
                log.info("Deleted image file: {}", filePath);
            } catch (IOException e) {
                log.warn("Failed to delete image file: {}", e.getMessage());
            }
        }

        productRepo.delete(product);
        log.info("Deleted product with ID: {}", productId);

        return Response.builder()
                .status(200)
                .message("Product deleted successfully")
                .build();
    }

    @Override
    public Response getProductById(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        ProductDto productDto = entityDtoMapper.mapProductToDtoBasic(product);

        return Response.builder()
                .status(200)
                .product(productDto)
                .build();
    }

    @Override
    public Response getAllProducts() {
        List<ProductDto> productList = productRepo.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .productList(productList)
                .build();
    }

    @Override
    public Response getProductsByCategory(Long categoryId) {
        List<Product> products = productRepo.findByCategoryId(categoryId);
        List<ProductDto> productDtos = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            log.info("No products found for category ID: {}", categoryId);
            return Response.builder()
                    .status(404)
                    .message("No products found for this category")
                    .build();
        }

        return Response.builder()
                .status(200)
                .productList(productDtos)
                .build();
    }

    @Override
    public Response searchProduct(String searchValue) {
        if (searchValue == null || searchValue.isBlank()) {
            return Response.builder()
                    .status(400)
                    .message("Search value cannot be empty")
                    .build();
        }

        List<Product> products = productRepo.findByNameContainingOrDescriptionContaining(searchValue, searchValue);
        List<ProductDto> productDtos = products.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            log.info("No products found for search: '{}'", searchValue);
            return Response.builder()
                    .status(404)
                    .message("No products found matching your search")
                    .build();
        }

        return Response.builder()
                .status(200)
                .productList(productDtos)
                .build();
    }
}