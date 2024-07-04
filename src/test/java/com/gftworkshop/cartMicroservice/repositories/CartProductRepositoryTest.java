package com.gftworkshop.cartMicroservice.repositories;

import com.gftworkshop.cartMicroservice.model.CartProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


public class CartProductRepositoryTest {

    private CartProductRepository cartProductRepository;
    private CartProduct cartProduct;

    @BeforeEach
    void setUp() {
        cartProductRepository = mock(CartProductRepository.class);
        cartProduct= mock(CartProduct.class);
    }

    @Test
    @DisplayName("When adding a product to the cart, " +
            "then the product should be saved")
    void addProductToCartTest() {
        when(cartProductRepository.save(cartProduct)).thenReturn(cartProduct);
        CartProduct savedCartProduct = cartProductRepository.save(cartProduct);
        verify(cartProductRepository, times(1)).save(cartProduct);
        assertNotNull(savedCartProduct);
    }

    @Test
    @DisplayName("When updating the quantity of a product in the cart, " +
            "then the quantity should be updated and return the number of affected rows")
    void updateProductQuantityTest() {
        long productId = 1L;
        int newQuantity = 5;

        when(cartProductRepository.updateQuantity(productId, newQuantity)).thenReturn(1);
        int updatedRows = cartProductRepository.updateQuantity(productId, newQuantity);
        verify(cartProductRepository, times(1)).updateQuantity(productId, newQuantity);
        assertEquals(1, updatedRows);
    }

    @Test
    @DisplayName("When passing an id, " +
            "then removing a product from cart")
    void removeProductTest() {
        long productId = 1L;
        doNothing().when(cartProductRepository).deleteById(productId);
        cartProductRepository.deleteById(productId);
        verify(cartProductRepository, times(1)).deleteById(productId);
    }
}
