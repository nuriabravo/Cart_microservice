package com.gftworkshop.cartMicroservice.services.impl;

import com.gftworkshop.cartMicroservice.api.dto.CartDto;
import com.gftworkshop.cartMicroservice.cartmanagement.CartCalculator;
import com.gftworkshop.cartMicroservice.cartmanagement.CartManager;
import com.gftworkshop.cartMicroservice.cartmanagement.CartValidator;
import com.gftworkshop.cartMicroservice.entitymapper.EntityMapper;
import com.gftworkshop.cartMicroservice.model.Cart;
import com.gftworkshop.cartMicroservice.model.CartProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("CartServiceImpl Unit Tests")
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartManager cartManager;

    @Mock
    private CartValidator cartValidator;

    @Mock
    private CartCalculator cartCalculator;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    @DisplayName("Add Product To Cart Test")
    void addProductToCartTest() {
        // Given
        CartProduct cartProduct = CartProduct.builder().build();

        // When
        cartService.addProductToCart(cartProduct);

        // Then
        verify(cartManager, times(1)).checkForAbandonedCarts();
        verify(cartValidator, times(1)).validateProductStock(cartProduct);
        verify(cartManager, times(1)).handleCartProduct(cartProduct);
    }

    @Test
    @DisplayName("Calculate Cart Total Test")
    void calculateCartTotalTest() {
        // Given
        Long cartId = 1L;
        Long userId = 1L;
        BigDecimal expectedTotal = new BigDecimal("50.00");
        when(cartCalculator.calculateCartTotal(cartId, userId)).thenReturn(expectedTotal);

        // When
        BigDecimal total = cartService.calculateCartTotal(cartId, userId);

        // Then
        assertEquals(expectedTotal, total);
    }

    @Test
    @DisplayName("Empty Cart Test")
    void emptyCartTest() {
        // Given
        Long cartId = 1L;
        Cart cart = Cart.builder().build();
        when(cartManager.fetchCartById(cartId)).thenReturn(cart);

        // When
        cartService.emptyCart(cartId);

        // Then
        verify(cartManager, times(1)).checkForAbandonedCarts();
        verify(cartManager, times(1)).clearCartProducts(cartId, cart);
        verify(cartManager, times(1)).updateCartTimestamp(cart);
        verify(cartManager, times(1)).saveCart(cart);
    }

    @Test
    @DisplayName("Identify Abandoned Carts Test")
    void identifyAbandonedCartsTest() {
        // Given
        LocalDate thresholdDate = LocalDate.now();
        List<Cart> abandonedCarts = Collections.singletonList(Cart.builder().build());
        List<CartDto> expectedDtoList = Collections.singletonList(CartDto.builder().build());
        when(cartManager.fetchAbandonedCarts(thresholdDate)).thenReturn(abandonedCarts);
        when(cartManager.convertCartsToDto(abandonedCarts)).thenReturn(expectedDtoList);

        // When
        List<CartDto> result = cartService.identifyAbandonedCarts(thresholdDate);

        // Then
        assertEquals(expectedDtoList, result);
        verify(cartManager, times(1)).logAbandonedCartsInfo(abandonedCarts, thresholdDate);
    }

    @Test
    @DisplayName("Create Cart Test")
    void createCartTest() {
        // Given
        Long userId = 1L;
        Cart cart = Cart.builder().build();
        when(cartManager.buildAndSaveCart(userId)).thenReturn(cart);

        // When
        CartDto createdCartDto = cartService.createCart(userId);

        // Then
        assertEquals(EntityMapper.convertCartToDto(cart), createdCartDto);
        verify(cartManager, times(1)).ensureUserDoesNotAlreadyHaveCart(userId);
    }

    @Test
    @DisplayName("Fetch Validated Cart Test")
    void fetchValidatedCartTest() {
        // Given
        Long cartId = 1L;
        Cart cart = Cart.builder().build();
        CartDto expectedCartDto = CartDto.builder().build();
        when(cartManager.fetchCartById(cartId)).thenReturn(cart);
        when(cartManager.prepareCartDto(cart)).thenReturn(expectedCartDto);

        // When
        CartDto result = cartService.fetchValidatedCart(cartId);

        // Then
        assertEquals(expectedCartDto, result);
        verify(cartManager, times(1)).checkForAbandonedCarts();
        verify(cartValidator, times(1)).validateCartProductsStock(cart);
        verify(cartManager, times(1)).updateAndSaveCartProductInfo(cart);
    }

    @Test
    @DisplayName("Fetch All Carts Test")
    void fetchAllCartsTest() {
        // Given
        List<Cart> expectedCarts = Collections.singletonList(Cart.builder().build());
        when(cartManager.fetchAllCarts()).thenReturn(expectedCarts);

        // When
        List<Cart> result = cartService.fetchAllCarts();

        // Then
        assertEquals(expectedCarts, result);
    }

    @Test
    @DisplayName("Add Product To Cart - Product Stock Validation Failure")
    void addProductToCart_ProductStockValidationFailure() {
        // Given
        CartProduct cartProduct = CartProduct.builder().build();
        doThrow(new RuntimeException("Product stock validation failed")).when(cartValidator).validateProductStock(cartProduct);

        // Then
        assertThrows(RuntimeException.class, () -> cartService.addProductToCart(cartProduct));

        // Verify
        verify(cartManager, never()).handleCartProduct(cartProduct);
    }


    @Test
    @DisplayName("Calculate Cart Total - Cart Not Found")
    void calculateCartTotal_CartNotFound() {
        // Given
        Long cartId = 1L;
        Long userId = 1L;
        when(cartCalculator.calculateCartTotal(cartId, userId)).thenThrow(new RuntimeException("Cart not found"));

        // Then
        assertThrows(RuntimeException.class, () -> cartService.calculateCartTotal(cartId, userId));

        // Verify
        verify(cartManager, never()).checkForAbandonedCarts();
    }

    @Test
    @DisplayName("Identify Abandoned Carts - No Abandoned Carts")
    void identifyAbandonedCarts_NoAbandonedCarts() {
        // Given
        LocalDate thresholdDate = LocalDate.now();
        when(cartManager.fetchAbandonedCarts(thresholdDate)).thenReturn(Collections.emptyList());

        // When
        List<CartDto> result = cartService.identifyAbandonedCarts(thresholdDate);

        // Then
        assert (result.isEmpty());
        verify(cartManager, times(1)).logAbandonedCartsInfo(Collections.emptyList(), thresholdDate);
    }
}
