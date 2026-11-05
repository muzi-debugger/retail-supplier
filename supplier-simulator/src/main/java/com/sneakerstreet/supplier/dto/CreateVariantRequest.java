package com.sneakerstreet.supplier.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateVariantRequest(

        @NotBlank @Size(max = 100) String sku,
        @NotBlank @Size(max = 100) String colour,
        @NotBlank @Size(max = 20) String size,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal wholesalePrice,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency) {

}
