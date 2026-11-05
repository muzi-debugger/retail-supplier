package com.sneakerstreet.supplier.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler (ProductNotFoundException.class)
    public ProblemDetail handleProductNotFoundException(ProductNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Product Not Found");
        return problemDetail;
    }

    @ExceptionHandler (DuplicateProductException.class)
    public ProblemDetail handleDuplicateProductException(DuplicateProductException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Duplicate Product");
        return problemDetail;
    }
    @ExceptionHandler (DuplicateVariantException.class)
    public ProblemDetail handleDuplicateVariantException(DuplicateVariantException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Duplicate Product Variant");
        return problemDetail;
    }
    @ExceptionHandler (ProductVariationNotFoundException.class)
    public ProblemDetail handleProductVariationNotFoundException(ProductVariationNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Product Variation Not Found");
        return problemDetail;
    }
    
}
