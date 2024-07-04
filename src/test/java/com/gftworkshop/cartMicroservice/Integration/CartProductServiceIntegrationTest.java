package com.gftworkshop.cartMicroservice.Integration;

import com.gftworkshop.cartMicroservice.api.dto.CartProductDto;
import com.gftworkshop.cartMicroservice.exceptions.CartProductInvalidQuantityException;
import com.gftworkshop.cartMicroservice.exceptions.CartProductNotFoundException;
import com.gftworkshop.cartMicroservice.model.CartProduct;
import com.gftworkshop.cartMicroservice.repositories.CartProductRepository;
import com.gftworkshop.cartMicroservice.repositories.CartRepository;
import com.gftworkshop.cartMicroservice.services.impl.CartProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("dev")
public class CartProductServiceIntegrationTest {


    private CartProductServiceImpl cartProductService;


    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartProductRepository cartProductRepository;

    @BeforeEach
    void setUp() throws IOException {
        cartProductService = new CartProductServiceImpl(cartProductRepository, cartRepository);
    }

    @Test
    public void whenUpdateQuantityWithValidIdThenReturnUpdatedQuantity() {
        Long cartProductId = 4L;
        int newQuantity = 5;
        int expectedResponse = 1;

        int actualResponse = cartProductService.updateQuantity(cartProductId, newQuantity);

        assertEquals(actualResponse, expectedResponse);
    }

    @Test
    public void whenUpdateQuantityWithInvalidQuantityThenThrowException() {
        Long CartProductId = 1L;
        int newQuantity = 0;

        assertThrows(CartProductInvalidQuantityException.class, () -> {
            cartProductService.updateQuantity(CartProductId, newQuantity);
        });
    }

    @Test
    public void whenUpdateQuantityForNonExistingCartProductThenThrowException() {
        Long CartProductId = 999L;
        int newQuantity = 5;

        assertThrows(CartProductNotFoundException.class, () -> {
            cartProductService.updateQuantity(CartProductId, newQuantity);
        });
    }

    @Test
    public void whenRemoveProductWithValidIdThenReturnCartProductDto() {
        Long cartProductId = 4L;
        CartProduct mockCartProduct = new CartProduct();
        mockCartProduct.setId(cartProductId);

        CartProductDto result = cartProductService.removeProduct(cartProductId);

        assertNotNull(result);
    }

    @Test
    public void whenRemoveProductWithInvalidIdThenThrowException() {
        Long cartProductId = 999L;

        assertThrows(CartProductNotFoundException.class, () -> {
            cartProductService.removeProduct(cartProductId);
        });
    }
}
