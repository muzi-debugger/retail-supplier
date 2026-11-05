
package com.sneakerstreet.supplier.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sneakerstreet.supplier.dto.ProductResponse;
import com.sneakerstreet.supplier.dto.ProductVariantResponse;
import com.sneakerstreet.supplier.exception.DuplicateProductException;
import com.sneakerstreet.supplier.exception.DuplicateVariantException;
import com.sneakerstreet.supplier.exception.GlobalExceptionHandler;
import com.sneakerstreet.supplier.exception.ProductNotFoundException;
import com.sneakerstreet.supplier.exception.ProductVariationNotFoundException;
import com.sneakerstreet.supplier.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/***
 * 
 * ProductControllerTest
 * 
 * - standaloneSetup() tests the controller without loading the entire Spring
 * application.
 * - LocalValidatorFactoryBean enables Jakarta Bean Validation so @Valid is
 * actually exercised.
 * - GlobalExceptionHandler is registered explicitly to test our API error
 * responses.
 */
class ProductControllerTest {

        private MockMvc mockMvc;
        private ProductService productService;
        private ObjectMapper objectMapper;

        private UUID productId;
        private ProductResponse productResponse;

        @BeforeEach
        void setUp() {

                productService = mock(ProductService.class);
                objectMapper = new ObjectMapper();

                LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
                validator.afterPropertiesSet();

                mockMvc = MockMvcBuilders
                                .standaloneSetup(new ProductController(productService))
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .setValidator(validator)
                                .build();

                productId = UUID.randomUUID();

                productResponse = new ProductResponse(
                                productId,
                                "NIKE-AF1-001",
                                "Nike",
                                "Air Force 1",
                                "Nike Air Force 1 sneaker collection",
                                true,
                                Instant.parse("2026-11-05T10:00:00Z"),
                                Instant.parse("2026-11-05T10:00:00Z"));
        }

        @Test
        void shouldCreateProductAndReturn201() throws Exception {

                when(productService.createProduct(any()))
                                .thenReturn(productResponse);

                String requestJson = """
                                {
                                  "productCode": "NIKE-AF1-001",
                                  "brand": "Nike",
                                  "name": "Air Force 1",
                                  "description": "Nike Air Force 1 sneaker collection"
                                }
                                """;

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isCreated())
                                .andExpect(header().string(
                                                "Location",
                                                "/api/v1/products/" + productId))
                                .andExpect(jsonPath("$.productCode")
                                                .value("NIKE-AF1-001"))
                                .andExpect(jsonPath("$.brand").value("Nike"));

                verify(productService).createProduct(any());
        }

        @Test
        void shouldRetrieveProductAndReturn200() throws Exception {

                when(productService.getProductByUUID(productId))
                                .thenReturn(productResponse);

                mockMvc.perform(get("/api/v1/products/{productId}", productId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id")
                                                .value(productId.toString()))
                                .andExpect(jsonPath("$.name")
                                                .value("Air Force 1"));

                verify(productService).getProductByUUID(productId);
        }

        @Test
        void shouldRejectInvalidProductRequestWith400() throws Exception {

                String invalidJson = """
                                {
                                  "productCode": "",
                                  "brand": "Nike",
                                  "name": "Air Force 1"
                                }
                                """;

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                                .andExpect(status().isBadRequest());

                verifyNoInteractions(productService);
        }

        @Test
        void shouldReturn409ForDuplicateProduct() throws Exception {

                when(productService.createProduct(any()))
                                .thenThrow(new DuplicateProductException("NIKE-AF1-001"));

                String requestJson = """
                                {
                                  "productCode": "NIKE-AF1-001",
                                  "brand": "Nike",
                                  "name": "Air Force 1"
                                }
                                """;

                mockMvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.title").value("Duplicate Product"))
                                .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        void shouldReturn404ForUnknownProduct() throws Exception {

                when(productService.getProductByUUID(productId))
                                .thenThrow(new ProductNotFoundException(productId));

                mockMvc.perform(get("/api/v1/products/{productId}", productId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Product Not Found"))
                                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        void shouldCreateVariantAndReturn201() throws Exception {

                UUID variantId = UUID.randomUUID();

                ProductVariantResponse response = new ProductVariantResponse(
                                variantId,
                                productId,
                                "AF1-WHT-UK7",
                                "White",
                                "UK7",
                                new BigDecimal("2100.00"),
                                "ZAR",
                                true,
                                Instant.parse("2026-11-05T10:00:00Z"),
                                Instant.parse("2026-11-05T10:00:00Z"));

                when(productService.createProductVariant(eq(productId), any()))
                                .thenReturn(response);

                String requestJson = """
                                {
                                  "sku": "AF1-WHT-UK7",
                                  "colour": "White",
                                  "size": "UK7",
                                  "wholesalePrice": 2100.00,
                                  "currency": "ZAR"
                                }
                                """;

                mockMvc.perform(post("/api/v1/products/{productId}/variants", productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isCreated())
                                .andExpect(header().string(
                                                "Location",
                                                "/api/v1/products/" + productId + "/variants/" + variantId))
                                .andExpect(jsonPath("$.sku").value("AF1-WHT-UK7"))
                                .andExpect(jsonPath("$.currency").value("ZAR"));
        }

        @Test
        void shouldRetrieveVariantAndReturn200() throws Exception {

                UUID variantId = UUID.randomUUID();

                ProductVariantResponse response = new ProductVariantResponse(
                                variantId,
                                productId,
                                "AF1-WHT-UK7",
                                "White",
                                "UK7",
                                new BigDecimal("2100.00"),
                                "ZAR",
                                true,
                                Instant.now(),
                                Instant.now());

                when(productService.getProductVariantById(productId, variantId))
                                .thenReturn(response);

                mockMvc.perform(get(
                                "/api/v1/products/{productId}/variants/{variantId}",
                                productId, variantId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(variantId.toString()))
                                .andExpect(jsonPath("$.sku").value("AF1-WHT-UK7"));
        }

        @Test
        void shouldListVariantsAndReturn200() throws Exception {

                ProductVariantResponse variant = new ProductVariantResponse(
                                UUID.randomUUID(),
                                productId,
                                "AF1-WHT-UK7",
                                "White",
                                "UK7",
                                new BigDecimal("2100.00"),
                                "ZAR",
                                true,
                                Instant.now(),
                                Instant.now());

                when(productService.getProductVariants(productId))
                                .thenReturn(List.of(variant));

                mockMvc.perform(get("/api/v1/products/{productId}/variants", productId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].sku").value("AF1-WHT-UK7"));
        }

        @Test
        void shouldReturn409ForDuplicateVariant() throws Exception {

                when(productService.createProductVariant(eq(productId), any()))
                                .thenThrow(new DuplicateVariantException("Duplicate SKU"));

                String requestJson = """
                                {
                                  "sku": "AF1-WHT-UK7",
                                  "colour": "White",
                                  "size": "UK7",
                                  "wholesalePrice": 2100.00,
                                  "currency": "ZAR"
                                }
                                """;

                mockMvc.perform(post("/api/v1/products/{productId}/variants", productId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.title").value("Duplicate Product Variant"))
                                .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        void shouldReturn404ForUnknownVariant() throws Exception {

                UUID variantId = UUID.randomUUID();

                when(productService.getProductVariantById(productId, variantId))
                                .thenThrow(new ProductVariationNotFoundException(variantId));

                mockMvc.perform(get(
                                "/api/v1/products/{productId}/variants/{variantId}",
                                productId, variantId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title")
                                                .value("Product Variation Not Found"))
                                .andExpect(jsonPath("$.status").value(404));
        }
}
