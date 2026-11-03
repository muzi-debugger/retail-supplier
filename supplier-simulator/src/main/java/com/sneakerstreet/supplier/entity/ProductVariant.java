package com.sneakerstreet.supplier.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
public class ProductVariant {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String colour;

    @Column(nullable = false)
    private String size;

    @Column(name = "wholesale_price", nullable = false)
    private BigDecimal wholesalePrice;

    @JdbcTypeCode (SqlTypes.CHAR)
    @Column(nullable = false, length = 3)
    private String currency = "ZAR";

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProductVariant(
            Product product,
            String sku,
            String colour,
            String size,
            BigDecimal wholesalePrice) {

        this.id = UUID.randomUUID();
        this.product = product;
        this.sku = sku;
        this.colour = colour;
        this.size = size;
        this.wholesalePrice = wholesalePrice;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

}
