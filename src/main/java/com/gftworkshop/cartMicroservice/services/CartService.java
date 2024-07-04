package com.gftworkshop.cartMicroservice.services;

import com.gftworkshop.cartMicroservice.api.dto.CartDto;
import com.gftworkshop.cartMicroservice.model.CartProduct;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CartService {
    void addProductToCart(CartProduct cartProduct);

    BigDecimal calculateCartTotal(Long cartId, Long userId);

    void emptyCart(Long cartId);

    List<CartDto> identifyAbandonedCarts(LocalDate thresholdDate);

    CartDto createCart(Long userId);

    CartDto fetchValidatedCart(Long cartId);
}
