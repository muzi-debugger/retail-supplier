package com.sneakerstreet.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank @Size(max = 64) String productCode,
        @NotBlank @Size(max = 100) String brand,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description) {

}
