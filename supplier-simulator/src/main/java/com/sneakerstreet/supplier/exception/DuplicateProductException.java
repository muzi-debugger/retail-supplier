package com.sneakerstreet.supplier.exception;

public class DuplicateProductException extends RuntimeException {

    public DuplicateProductException(String productCode) {
        super("Product with code " + productCode + " already exists.");
    }
    
}
