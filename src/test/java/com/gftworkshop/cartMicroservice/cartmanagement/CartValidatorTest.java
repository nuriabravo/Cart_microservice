package com.gftworkshop.cartMicroservice.cartmanagement;

import com.gftworkshop.cartMicroservice.api.dto.Product;
import com.gftworkshop.cartMicroservice.exceptions.CartProductInvalidQuantityException;
import com.gftworkshop.cartMicroservice.model.Cart;
import com.gftworkshop.cartMicroservice.model.CartProduct;
import com.gftworkshop.cartMicroservice.repositories.CartProductRepository;
import com.gftworkshop.cartMicroservice.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CartValidator Unit Tests")
class CartValidatorTest {

    @Mock
    private ProductService productService;

    @Mock
    private CartProductRepository cartProductRepository;

    @InjectMocks
    private CartValidator cartValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test validateProductStock - enough stock")
    void testValidateProductStockEnoughStock() {
        // Given
        Cart cart = Cart.builder().id(1L).build();
        CartProduct cartProduct = CartProduct.builder()
                .cart(cart)
                .productId(1L)
                .quantity(2)
                .build();

        // Mock
        when(cartProductRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(new CartProduct()));
        when(productService.getProductById(1L)).thenReturn(new Product(1L, "Product", "Description", null, 5, null));

        // When / Then
        assertDoesNotThrow(() -> cartValidator.validateProductStock(cartProduct));
    }

    @Test
    @DisplayName("Test validateProductStock - not enough stock")
    void testValidateProductStockNotEnoughStock() {
        // Given
        Cart cart = Cart.builder().id(1L).build();
        CartProduct cartProduct = CartProduct.builder()
                .cart(cart)
                .productId(1L)
                .quantity(10)
                .build();

        // Mock
        when(cartProductRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(new CartProduct()));
        when(productService.getProductById(1L)).thenReturn(new Product(1L, "Product", "Description", null, 5, null));

        // When / Then
        assertThrows(CartProductInvalidQuantityException.class, () -> cartValidator.validateProductStock(cartProduct));
    }

    @Test
    @DisplayName("Test calculateTotalDesiredQuantity")
    void testCalculateTotalDesiredQuantity() {
        // Given
        Cart cart = Cart.builder().id(1L).build();
        CartProduct existingCartProduct = CartProduct.builder()
                .cart(cart)
                .productId(1L)
                .quantity(3)
                .build();

        CartProduct newCartProduct = CartProduct.builder()
                .cart(cart)
                .productId(1L)
                .quantity(2)
                .build();

        // Mock
        when(cartProductRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(existingCartProduct));

        // When
        int totalDesiredQuantity = cartValidator.calculateTotalDesiredQuantity(newCartProduct);

        // Then
        assertEquals(5, totalDesiredQuantity);
    }

    @Test
    @DisplayName("Test getCurrentQuantity - cart product found")
    void testGetCurrentQuantityCartProductFound() {
        // Given
        Cart cart = Cart.builder().id(1L).build();
        CartProduct cartProductInRepo = CartProduct.builder()
                .cart(cart)
                .productId(1L)
                .quantity(2)
                .build();

        CartProduct cartProductToCheck = CartProduct.builder()
                .cart(cart)
                .productId(1L)
                .build();

        // Mock
        when(cartProductRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartProductInRepo));

        // When
        int currentQuantity = cartValidator.getCurrentQuantity(cartProductToCheck);

        // Then
        assertEquals(2, currentQuantity);
    }

    @Test
    @DisplayName("Test getCurrentQuantity - cart product not found")
    void testGetCurrentQuantityCartProductNotFound() {
        // Given
        CartProduct cartProduct = CartProduct.builder()
                .cart(Cart.builder().id(1L).build())
                .productId(1L)
                .build();

        when(cartProductRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());

        // When
        int currentQuantity = cartValidator.getCurrentQuantity(cartProduct);

        // Then
        assertEquals(0, currentQuantity);
    }

    @Test
    @DisplayName("Test getAvailableStock")
    void testGetAvailableStock() {
        // Given
        CartProduct cartProduct = CartProduct.builder().productId(1L).build();
        when(productService.getProductById(anyLong())).thenReturn(new Product(1L, "Product", "Description", null, 5, null));

        // When
        int availableStock = cartValidator.getAvailableStock(cartProduct);

        // Then
        assertEquals(5, availableStock);
    }

    @Test
    @DisplayName("Test validateCartProductsStock - enough stock")
    void testValidateCartProductsStockEnoughStock() {
        // Given
        CartProduct cartProduct = CartProduct.builder()
                .productId(1L)
                .quantity(2)
                .build();
        Cart cart = Cart.builder()
                .cartProducts(Collections.singletonList(cartProduct))
                .build();
        Product product = new Product(1L, "Product", "Description", null, 3, null);
        when(productService.findProductsByIds(anyList())).thenReturn(Collections.singletonList(product));

        // When / Then
        assertDoesNotThrow(() -> cartValidator.validateCartProductsStock(cart));
    }

    @Test
    @DisplayName("Test validateCartProductsStock - not enough stock")
    void testValidateCartProductsStockNotEnoughStock() {
        // Given
        CartProduct cartProduct = CartProduct.builder()
                .productId(1L)
                .quantity(5)
                .build();
        Cart cart = Cart.builder()
                .cartProducts(Collections.singletonList(cartProduct))
                .build();
        Product product = new Product(1L, "Product", "Description", null, 3, null);
        when(productService.findProductsByIds(anyList())).thenReturn(Collections.singletonList(product));

        // When / Then
        assertThrows(CartProductInvalidQuantityException.class, () -> cartValidator.validateCartProductsStock(cart));
    }

    @Test
    @DisplayName("Test getProductMap")
    void testGetProductMap() {
        // Given
        CartProduct cartProduct1 = CartProduct.builder().productId(1L).build();
        CartProduct cartProduct2 = CartProduct.builder().productId(2L).build();
        Cart cart = Cart.builder()
                .cartProducts(Arrays.asList(cartProduct1, cartProduct2))
                .build();
        Product product1 = new Product(1L, "Product 1", "Description 1", null, 5, null);
        Product product2 = new Product(2L, "Product 2", "Description 2", null, 10, null);
        when(productService.findProductsByIds(anyList())).thenReturn(Arrays.asList(product1, product2));

        // When
        Map<Long, Product> productMap = cartValidator.getProductMap(cart);

        // Then
        assertEquals(2, productMap.size());
        assertTrue(productMap.containsKey(1L));
        assertTrue(productMap.containsKey(2L));
        assertEquals(product1, productMap.get(1L));
        assertEquals(product2, productMap.get(2L));
    }
}
