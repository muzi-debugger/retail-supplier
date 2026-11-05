package com.sneakerstreet.supplier.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductVariantResponse(
        UUID id,
        UUID productId,
        String sku,
        String colour,
        String size,
        BigDecimal wholesalePrice,
        String currency,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
