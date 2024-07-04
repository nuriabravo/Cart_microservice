package com.gftworkshop.cartMicroservice.services.impl;

import com.gftworkshop.cartMicroservice.exceptions.CartNotFoundException;
import com.gftworkshop.cartMicroservice.api.dto.CartProductDto;
import com.gftworkshop.cartMicroservice.exceptions.CartProductInvalidQuantityException;
import com.gftworkshop.cartMicroservice.exceptions.CartProductNotFoundException;
import com.gftworkshop.cartMicroservice.model.Cart;
import com.gftworkshop.cartMicroservice.model.CartProduct;
import com.gftworkshop.cartMicroservice.repositories.CartProductRepository;
import com.gftworkshop.cartMicroservice.repositories.CartRepository;
import com.gftworkshop.cartMicroservice.services.CartService;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Ignore
class CartProductServiceImplTest {

    @Mock
    private CartProductRepository cartProductRepository;
    @Mock private CartRepository cartRepository;
    @Mock
    private CartService cartService;
    @InjectMocks
    private CartProductServiceImpl cartProductService;

    @InjectMocks
    private CartServiceImpl cartServiceImpl;
    private CartProduct cartProduct;
    private Long id;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        id = 123L;
        cartProduct = mock(CartProduct.class);

    }

    @Nested
    @DisplayName("Update CartProduct Quantity")
    class UpdateCartProductQuantityTests {
        @Test
        @DisplayName("Given Product ID and New Quantity When Updated Then Return Rows Affected")
        void updateQuantityTest() {
            Long id = 123L;
            int newQuantity = 5;
            CartProduct cartProduct = new CartProduct();
            cartProduct.setId(id);
            cartProduct.setQuantity(3);
            Cart cart = new Cart();
            cart.setId(456L);
            cartProduct.setCart(cart);

            when(cartProductRepository.findById(id)).thenReturn(Optional.of(cartProduct));
            when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
            when(cartProductRepository.updateQuantity(id, newQuantity)).thenReturn(1);

            int rowsAffected = cartProductService.updateQuantity(id, newQuantity);

            assertEquals(1, rowsAffected);
            verify(cartProductRepository).updateQuantity(id, newQuantity);
        }


        @Test
        @DisplayName("Given Invalid Quantity " +
                "Then Throws Exception")
        void testUpdateQuantityWithInvalidQuantity() {
            int quantity = -5;

            CartProductInvalidQuantityException exception = assertThrows(CartProductInvalidQuantityException.class, () -> {
                cartProductService.updateQuantity(id, quantity);
            });

            assertEquals("The quantity must be higher than 0", exception.getMessage());

            verifyNoInteractions(cartProductRepository);
        }

        @Test
        @DisplayName("Same Quantity When Updated Then Return 0 Rows Affected")
        void updateQuantityNoChangesTest() {
            Long cartProductId = 123L;
            int currentQuantity = 5;

            CartProduct cartProduct = new CartProduct();
            cartProduct.setId(cartProductId);
            cartProduct.setQuantity(currentQuantity);
            Cart cart = new Cart();
            cart.setId(456L);
            cartProduct.setCart(cart);

            when(cartProductRepository.findById(cartProductId)).thenReturn(Optional.of(cartProduct));
            when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));

            when(cartProductRepository.updateQuantity(cartProductId, currentQuantity)).thenReturn(0);

            int rowsAffected = cartProductService.updateQuantity(cartProductId, currentQuantity);

            assertEquals(0, rowsAffected);
            verify(cartProductRepository).updateQuantity(cartProductId, currentQuantity);
        }


        @Test
        @DisplayName("Given Valid Quantity When Updated Then Return Updated Quantity")
        void testUpdateQuantity_ValidQuantity_Success() {
            Long cartProductId = 1L;
            int quantity = 5;

            // Mock the Cart and CartProduct as they would be related in the service method
            Cart cart = new Cart();
            cart.setId(1L);
            CartProduct cartProduct = new CartProduct();
            cartProduct.setId(cartProductId);
            cartProduct.setCart(cart);
            cartProduct.setQuantity(3);

            when(cartProductRepository.findById(cartProductId)).thenReturn(Optional.of(cartProduct));
            when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));

            when(cartProductRepository.updateQuantity(cartProductId, quantity)).thenReturn(quantity);

            int updatedQuantity = cartProductService.updateQuantity(cartProductId, quantity);

            assertEquals(quantity, updatedQuantity);
            verify(cartProductRepository).updateQuantity(cartProductId, quantity);
            verify(cartRepository).findById(cart.getId());
        }


        @Test
        @DisplayName("Given Invalid Quantity Zero " +
                "Then Throws Exception")
        void testUpdateQuantity_InvalidQuantity_Zero() {
            Long id = 1L;
            int quantity = 0;

            assertThrows(CartProductInvalidQuantityException.class, () -> {
                cartProductService.updateQuantity(id, quantity);
            });

            verify(cartRepository, never()).findById(anyLong());
            verify(cartProductRepository, never()).updateQuantity(anyLong(), anyInt());
        }

        @Test
        @DisplayName("Given Invalid Quantity Negative " +
                "Then Throws Exception")
        void testUpdateQuantity_InvalidQuantity_Negative() {
            Long id = 1L;
            int quantity = -5;

            assertThrows(CartProductInvalidQuantityException.class, () -> {
                cartProductService.updateQuantity(id, quantity);
            });

            verify(cartRepository, never()).findById(anyLong());
            verify(cartProductRepository, never()).updateQuantity(anyLong(), anyInt());
        }

        @Test
        @DisplayName("Given Nonexistent Cart " +
                "Then Throws Exception")
        void testUpdateQuantity_CartNotFound() {
            Long id = 999L;
            int quantity = 5;

            Cart cart = Cart.builder().id(888L).build();
            CartProduct cartProduct = CartProduct.builder().cart(cart).build();

            when(cartProductRepository.findById(id)).thenReturn(Optional.of(cartProduct));

            when(cartRepository.findById(cart.getId())).thenReturn(Optional.empty());

            assertThrows(CartNotFoundException.class, () -> {
                cartProductService.updateQuantity(id, quantity);
            });

            verify(cartProductRepository, never()).updateQuantity(anyLong(), anyInt());
        }


        @Test
        @DisplayName("Given Nonexistent Cart Product " +
                "Then Throws Exception")
        void testUpdateQuantity_CartProductNotFound() {
            Long id = 999L;
            int quantity = 5;

            when(cartRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(CartProductNotFoundException.class, () -> {
                cartProductService.updateQuantity(id, quantity);
            });

            verify(cartProductRepository, never()).updateQuantity(anyLong(), anyInt());
        }
    }


    @Nested
    @DisplayName("Remove CartProduct")
    class RemoveCartProductTests {
        @Test
        @DisplayName("When removing existing CartProduct, then verify deletion and returned value")
        void removeProductTest() {
            CartProduct cartProductToRemove = new CartProduct();
            cartProductToRemove.setId(id);
            when(cartProductRepository.findById(id)).thenReturn(Optional.of(cartProductToRemove));

            CartProductDto removedProduct = cartProductService.removeProduct(id);

            verify(cartProductRepository, times(1)).deleteById(id);
            assertEquals(cartProductToRemove.getId(), removedProduct.getId());
        }

        @Test
        @DisplayName("When removing non-existent CartProduct - Then verify exception")
        void removeNonExistentProductTest() {

            when(cartProductRepository.findById(id)).thenReturn(Optional.empty());

            CartProductNotFoundException exception = assertThrows(CartProductNotFoundException.class, () -> {
                cartProductService.removeProduct(id);
            });

            assertEquals("No se encontró el CartProduct con ID: " + id, exception.getMessage());
        }
    }


}
