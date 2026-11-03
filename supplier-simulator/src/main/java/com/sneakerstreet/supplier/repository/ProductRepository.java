package com.sneakerstreet.supplier.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository; 
import com.sneakerstreet.supplier.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByProductCode(String productCode);
    boolean existsByProductCode(String productCode);
    
}
