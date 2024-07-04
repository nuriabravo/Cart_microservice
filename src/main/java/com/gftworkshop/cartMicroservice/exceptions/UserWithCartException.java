package com.gftworkshop.cartMicroservice.exceptions;

public class UserWithCartException extends RuntimeException {

    public UserWithCartException(String message) {
        super(message);
    }
}
