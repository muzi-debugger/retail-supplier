package com.sneakerstreet.supplier.service;

import com.sneakerstreet.supplier.dto.CreateProductRequest;
import com.sneakerstreet.supplier.dto.ProductResponse;
import com.sneakerstreet.supplier.entity.Product;
import com.sneakerstreet.supplier.exception.DuplicateProductException;
import com.sneakerstreet.supplier.exception.ProductNotFoundException;
import com.sneakerstreet.supplier.repository.ProductRepository;
import com.sneakerstreet.supplier.repository.ProductVariantRepository;
import java.math.BigDecimal;
import java.util.List;

import com.sneakerstreet.supplier.dto.CreateVariantRequest;
import com.sneakerstreet.supplier.dto.ProductVariantResponse;
import com.sneakerstreet.supplier.entity.ProductVariant;
import com.sneakerstreet.supplier.exception.DuplicateVariantException;
import com.sneakerstreet.supplier.exception.ProductVariationNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

        @Mock
        private ProductRepository productRepository;

        @Mock
        private ProductVariantRepository productVariantRepository;

        @InjectMocks
        private ProductService productService;

        private CreateProductRequest createProductRequest;

        @BeforeEach
        void setup() {
                createProductRequest = new CreateProductRequest("NIKE-AF1-001",
                                "Nike",
                                "Air Force 1",
                                "Nike Air Force 1 sneaker collection");
        }

        @Test
        void shouldCreateProductSuccessfully() {

                when(productRepository.save(any(Product.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                ProductResponse response = productService.createProduct(createProductRequest);

                assertNotNull(response.id());
                assertEquals("NIKE-AF1-001", response.productCode());
                assertEquals("Nike", response.brand());
                assertEquals("Air Force 1", response.name());

                ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

                verify(productRepository).save(captor.capture());

                Product savedProduct = captor.getValue();

                assertEquals("NIKE-AF1-001", savedProduct.getProductCode());
                assertEquals("Nike", savedProduct.getBrand());
                assertEquals("Air Force 1", savedProduct.getName());
                assertEquals(
                                "Nike Air Force 1 sneaker collection",
                                savedProduct.getDescription());
        }

        @Test
        void shouldRejectDuplicateProductCode() {

                when(productRepository.existsByProductCode(
                                createProductRequest.productCode())).thenReturn(true);

                assertThrows(
                                DuplicateProductException.class,
                                () -> productService.createProduct(createProductRequest));

                verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        void shouldRetrieveExistingProduct() {

                Product product = new Product(
                                "NIKE-AF1-001",
                                "Nike",
                                "Air Force 1");

                UUID productId = product.getId();

                when(productRepository.findById(productId))
                                .thenReturn(Optional.of(product));

                ProductResponse response = productService.getProductByUUID(productId);

                assertEquals(productId, response.id());
                assertEquals("Air Force 1", response.name());

                verify(productRepository).findById(productId);
        }

        @Test
        void shouldRejectNonexistentProduct() {

                UUID productId = UUID.randomUUID();

                when(productRepository.findById(productId))
                                .thenReturn(Optional.empty());

                assertThrows(
                                ProductNotFoundException.class,
                                () -> productService.getProductByUUID(productId));
        }

        @Test
        void shouldCreateProductVariantSuccessfully() {

                Product product = new Product(
                                "NIKE-AF1-001",
                                "Nike",
                                "Air Force 1");

                UUID productId = product.getId();

                CreateVariantRequest request = new CreateVariantRequest(
                                "AF1-WHT-UK7",
                                "White",
                                "UK7",
                                new BigDecimal("2100.00"),
                                "ZAR");

                when(productRepository.findById(productId))
                                .thenReturn(Optional.of(product));

                when(productVariantRepository.save(any(ProductVariant.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                ProductVariantResponse response = productService.createProductVariant(productId, request);

                assertNotNull(response.id());
                assertEquals(productId, response.productId());
                assertEquals("AF1-WHT-UK7", response.sku());
                assertEquals("White", response.colour());
                assertEquals("UK7", response.size());
                assertEquals(new BigDecimal("2100.00"), response.wholesalePrice());
                assertEquals("ZAR", response.currency());
                assertTrue(response.active());

                verify(productVariantRepository).save(any(ProductVariant.class));
        }

        @Test
        void shouldRejectDuplicateVariantSku() {

                Product product = new Product(
                                "NIKE-AF1-001",
                                "Nike",
                                "Air Force 1");

                UUID productId = product.getId();

                CreateVariantRequest request = new CreateVariantRequest(
                                "AF1-WHT-UK7",
                                "White",
                                "UK7",
                                new BigDecimal("2100.00"),
                                "ZAR");

                when(productRepository.findById(productId))
                                .thenReturn(Optional.of(product));

                when(productVariantRepository.existsBySku(request.sku()))
                                .thenReturn(true);

                assertThrows(
                                DuplicateVariantException.class,
                                () -> productService.createProductVariant(productId, request));

                verify(productVariantRepository, never())
                                .save(any(ProductVariant.class));
        }

        @Test
        void shouldRejectDuplicateColourAndSize() {

                Product product = new Product(
                                "NIKE-AF1-001",
                                "Nike",
                                "Air Force 1");

                UUID productId = product.getId();

                CreateVariantRequest request = new CreateVariantRequest(
                                "AF1-WHT-UK7-NEW",
                                "White",
                                "UK7",
                                new BigDecimal("2100.00"),
                                "ZAR");

                when(productRepository.findById(productId))
                                .thenReturn(Optional.of(product));

                when(productVariantRepository.existsByProductIdAndColourAndSize(
                                productId, "White", "UK7"))
                                .thenReturn(true);

                assertThrows(
                                DuplicateVariantException.class,
                                () -> productService.createProductVariant(productId, request));

                verify(productVariantRepository, never())
                                .save(any(ProductVariant.class));
        }

        @Test
        void shouldRejectVariantCreationForNonexistentProduct() {

                UUID productId = UUID.randomUUID();

                CreateVariantRequest request = new CreateVariantRequest(
                                "AF1-WHT-UK7",
                                "White",
                                "UK7",
                                new BigDecimal("2100.00"),
                                "ZAR");

                when(productRepository.findById(productId))
                                .thenReturn(Optional.empty());

                assertThrows(
                                ProductNotFoundException.class,
                                () -> productService.createProductVariant(productId, request));

                verify(productVariantRepository, never())
                                .save(any(ProductVariant.class));
        }

        @Test
        void shouldRejectVariantBelongingToAnotherProduct() {

                UUID productId = UUID.randomUUID();
                UUID variantId = UUID.randomUUID();

                when(productVariantRepository.findByIdAndProductId(
                                variantId, productId))
                                .thenReturn(Optional.empty());

                assertThrows(
                                ProductVariationNotFoundException.class,
                                () -> productService.getProductVariantById(productId, variantId));
        }

        @Test
        void shouldListProductVariantsSuccessfully() {

                Product product = new Product(
                                "NIKE-AF1-001",
                                "Nike",
                                "Air Force 1");

                UUID productId = product.getId();

                ProductVariant whiteUK7 = new ProductVariant(
                                product,
                                "AF1-WHT-UK7",
                                "White",
                                "UK7",
                                new BigDecimal("2100.00"),
                                "ZAR");

                ProductVariant whiteUK8 = new ProductVariant(
                                product,
                                "AF1-WHT-UK8",
                                "White",
                                "UK8",
                                new BigDecimal("2100.00"),
                                "ZAR");

                when(productRepository.existsById(productId))
                                .thenReturn(true);

                when(productVariantRepository.findByProductId(productId))
                                .thenReturn(List.of(whiteUK7, whiteUK8));

                List<ProductVariantResponse> response = productService.getProductVariants(productId);

                assertEquals(2, response.size());
                assertEquals("AF1-WHT-UK7", response.get(0).sku());
                assertEquals("AF1-WHT-UK8", response.get(1).sku());

                verify(productVariantRepository).findByProductId(productId);
        }

}
