package com.sneakerstreet.supplier.exception;

import java.util.UUID;

public class ProductVariationNotFoundException extends RuntimeException {

    public ProductVariationNotFoundException(UUID variantId) {
        super("Product variation not found with ID: " + variantId);
    }
    
}
