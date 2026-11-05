package com.sneakerstreet.supplier.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sneakerstreet.supplier.dto.CreateProductRequest;
import com.sneakerstreet.supplier.dto.CreateVariantRequest;
import com.sneakerstreet.supplier.dto.ProductResponse;
import com.sneakerstreet.supplier.dto.ProductVariantResponse;
import com.sneakerstreet.supplier.entity.Product;
import com.sneakerstreet.supplier.entity.ProductVariant;
import com.sneakerstreet.supplier.exception.DuplicateProductException;
import com.sneakerstreet.supplier.exception.DuplicateVariantException;
import com.sneakerstreet.supplier.exception.ProductNotFoundException;
import com.sneakerstreet.supplier.exception.ProductVariationNotFoundException;
import com.sneakerstreet.supplier.repository.ProductRepository;
import com.sneakerstreet.supplier.repository.ProductVariantRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public ProductService(ProductRepository productRepository, ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByProductCode(request.productCode())) {
            throw new DuplicateProductException(request.productCode());
        }
        Product product = new Product(request.productCode(), request.brand(), request.name());
        product.updateDesctiprion(request.description());

        Product savedProduct = productRepository.save(product);
        return toProductResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByUUID(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return toProductResponse(product);
    }

    private ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductCode(),
                product.getBrand(),
                product.getName(),
                product.getDescription(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    @Transactional
    public ProductVariantResponse createProductVariant(UUID productId, CreateVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (productVariantRepository.existsBySku(request.sku())) {
            throw new DuplicateVariantException("Variant SKU already exists: " + request.sku());
        }

        if (productVariantRepository
            .existsByProductIdAndColourAndSize(productId, request.colour(), request.size())) {
            throw new DuplicateVariantException("A variant already exists for colour: " + request.colour() + " and size: " + request.size());
        }
        ProductVariant variant = new ProductVariant(product, request.sku(), request.colour(), request.size(),
                request.wholesalePrice(), request.currency());
                
        ProductVariant savedVariant = productVariantRepository.save(variant);
        return toProductVariantResponse(savedVariant);

    }

    private ProductVariantResponse toProductVariantResponse(ProductVariant savedVariant) {
        return new ProductVariantResponse(
                savedVariant.getId(),
                savedVariant.getProduct().getId(),
                savedVariant.getSku(),
                savedVariant.getColour(),
                savedVariant.getSize(),
                savedVariant.getWholesalePrice(),
                savedVariant.getCurrency(),
                savedVariant.isActive(),
                savedVariant.getCreatedAt(),
                savedVariant.getUpdatedAt());
    }
    @Transactional (readOnly = true)
    public ProductVariantResponse getProductVariantById(UUID productId, UUID variantId) {
        
        ProductVariant variant = productVariantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new ProductVariationNotFoundException(variantId));
        return toProductVariantResponse(variant);
    }

    @Transactional (readOnly = true)
    public List<ProductVariantResponse> getProductVariants(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        return productVariantRepository.findByProductId(productId).stream()
                .map(this::toProductVariantResponse)
                .toList();
        
    }

}
