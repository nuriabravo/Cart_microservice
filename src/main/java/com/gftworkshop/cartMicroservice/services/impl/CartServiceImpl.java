package com.gftworkshop.cartMicroservice.services.impl;

import com.gftworkshop.cartMicroservice.api.dto.CartDto;
import com.gftworkshop.cartMicroservice.cartmanagement.CartCalculator;
import com.gftworkshop.cartMicroservice.cartmanagement.CartManager;
import com.gftworkshop.cartMicroservice.cartmanagement.CartValidator;
import com.gftworkshop.cartMicroservice.entitymapper.EntityMapper;
import com.gftworkshop.cartMicroservice.model.Cart;
import com.gftworkshop.cartMicroservice.model.CartProduct;
import com.gftworkshop.cartMicroservice.services.CartService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartManager cartManager;
    private final CartValidator cartValidator;
    private final CartCalculator cartCalculator;

    public CartServiceImpl(CartManager cartManager, CartValidator cartValidator, CartCalculator cartCalculator) {
        this.cartManager = cartManager;
        this.cartValidator = cartValidator;
        this.cartCalculator = cartCalculator;
    }


    @Override
    public void addProductToCart(CartProduct cartProduct) {
        cartManager.checkForAbandonedCarts();
        cartValidator.validateProductStock(cartProduct);
        cartManager.handleCartProduct(cartProduct);
    }

    @Override
    public BigDecimal calculateCartTotal(Long cartId, Long userId) {
        return cartCalculator.calculateCartTotal(cartId, userId);
    }

    @Override
    @Transactional
    public void emptyCart(Long cartId) {
        cartManager.checkForAbandonedCarts();
        Cart cart = cartManager.fetchCartById(cartId);
        cartManager.clearCartProducts(cartId, cart);
        cartManager.updateCartTimestamp(cart);
        cartManager.saveCart(cart);
    }

    @Override
    public List<CartDto> identifyAbandonedCarts(LocalDate thresholdDate) {
        List<Cart> abandonedCarts = cartManager.fetchAbandonedCarts(thresholdDate);
        cartManager.logAbandonedCartsInfo(abandonedCarts, thresholdDate);
        return cartManager.convertCartsToDto(abandonedCarts);
    }

    @Override
    public CartDto createCart(Long userId) {
        cartManager.checkForAbandonedCarts();
        cartManager.ensureUserDoesNotAlreadyHaveCart(userId);
        Cart cart = cartManager.buildAndSaveCart(userId);
        return EntityMapper.convertCartToDto(cart);
    }

    @Override
    public CartDto fetchValidatedCart(Long cartId) {
        cartManager.checkForAbandonedCarts();
        Cart cart = cartManager.fetchCartById(cartId);
        cartValidator.validateCartProductsStock(cart);
        cartManager.updateAndSaveCartProductInfo(cart);
        return cartManager.prepareCartDto(cart);
    }

    public List<Cart> fetchAllCarts() {
        return cartManager.fetchAllCarts();
    }
}
