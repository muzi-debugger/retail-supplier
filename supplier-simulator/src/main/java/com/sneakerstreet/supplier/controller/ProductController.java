package com.sneakerstreet.supplier.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sneakerstreet.supplier.dto.CreateProductRequest;
import com.sneakerstreet.supplier.dto.CreateVariantRequest;
import com.sneakerstreet.supplier.dto.ProductResponse;
import com.sneakerstreet.supplier.dto.ProductVariantResponse;
import com.sneakerstreet.supplier.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product Catalogue", description = "Manage supplier products and their variants")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Registers a new product in the supplier catalogue.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product request"),
            @ApiResponse(responseCode = "409", description = "Product code already exists")
    })
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {

        ProductResponse createdProduct = productService.createProduct(request);
        URI location = URI.create("/api/v1/products/" + createdProduct.id());
        return ResponseEntity.created(location).body(createdProduct);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product", description = "Retrieves a product using its UUID.")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID productId) {
        ProductResponse product = productService.getProductByUUID(productId);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{productId}/variants")
    @Operation(summary = "Create product variant", description = "Registers a sellable SKU with colour, size and wholesale price.")
    public ResponseEntity<ProductVariantResponse> createProductVariant(@PathVariable UUID productId,
            @Valid @RequestBody CreateVariantRequest request) {
        ProductVariantResponse createdVariant = productService.createProductVariant(productId, request);
        URI location = URI.create("/api/v1/products/" + productId + "/variants/" + createdVariant.id());
        return ResponseEntity.created(location).body(createdVariant);
    }

    @GetMapping("/{productId}/variants/{variantId}")
    @Operation(summary = "Get product variant", description = "Retrieves a variant belonging to the specified product.")
    public ResponseEntity<ProductVariantResponse> getProductVariantById(@PathVariable UUID productId,
            @PathVariable UUID variantId) {
        ProductVariantResponse variant = productService.getProductVariantById(productId, variantId);
        return ResponseEntity.ok(variant);
    }

    @GetMapping("/{productId}/variants")
    @Operation(summary = "List product variants", description = "Retrieves all variants belonging to a product.")
    public ResponseEntity<List<ProductVariantResponse>> getProductVariants(@PathVariable UUID productId) {
        List<ProductVariantResponse> variants = productService.getProductVariants(productId);
        return ResponseEntity.ok(variants);
    }

}
