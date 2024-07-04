package com.gftworkshop.cartMicroservice.exceptions;

public class CartProductNotFoundException extends RuntimeException{

    public CartProductNotFoundException(String message) {
        super(message);
    }
}
