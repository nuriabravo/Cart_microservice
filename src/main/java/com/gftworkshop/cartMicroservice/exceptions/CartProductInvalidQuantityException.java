package com.gftworkshop.cartMicroservice.exceptions;

public class CartProductInvalidQuantityException extends RuntimeException {
    public CartProductInvalidQuantityException(String message) {
        super(message);
    }
}
