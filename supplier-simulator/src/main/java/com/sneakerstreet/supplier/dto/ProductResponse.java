package com.sneakerstreet.supplier.dto;

import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String productCode,
        String brand,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {

}
