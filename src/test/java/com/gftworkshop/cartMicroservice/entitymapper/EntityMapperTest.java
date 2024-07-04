package com.gftworkshop.cartMicroservice.entitymapper;

import com.gftworkshop.cartMicroservice.api.dto.CartDto;
import com.gftworkshop.cartMicroservice.api.dto.CartProductDto;
import com.gftworkshop.cartMicroservice.model.Cart;
import com.gftworkshop.cartMicroservice.model.CartProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("EntityMapper Tests")
class EntityMapperTest {

    @Test
    @DisplayName("Convert Cart To Dto Test")
    void convertCartToDtoTest() {
        // Given
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(123L);

        // When
        CartDto cartDto = EntityMapper.convertCartToDto(cart);

        // Then
        assertEquals(cart.getId(), cartDto.getId());
        assertEquals(cart.getUserId(), cartDto.getUserId());
    }

    @Test
    @DisplayName("Convert CartProduct To Dto Test")
    void convertCartProductToDtoTest() {
        // Given
        CartProduct cartProduct = new CartProduct();
        cartProduct.setId(1L);
        cartProduct.setProductId(456L);
        cartProduct.setQuantity(2);

        // When
        CartProductDto cartProductDto = EntityMapper.convertCartProductToDto(cartProduct);

        // Then
        assertEquals(cartProduct.getId(), cartProductDto.getId());
        assertEquals(cartProduct.getProductId(), cartProductDto.getProductId());
        assertEquals(cartProduct.getQuantity(), cartProductDto.getQuantity());
    }

    @Test
    @DisplayName("Convert To Dto List Test")
    void convertToDtoListTest() {
        // Given
        CartProduct cartProduct1 = new CartProduct();
        cartProduct1.setId(1L);
        cartProduct1.setProductId(456L);
        cartProduct1.setQuantity(2);

        CartProduct cartProduct2 = new CartProduct();
        cartProduct2.setId(2L);
        cartProduct2.setProductId(789L);
        cartProduct2.setQuantity(3);

        List<CartProduct> cartProducts = Arrays.asList(cartProduct1, cartProduct2);

        // When
        List<CartProductDto> cartProductDtos = EntityMapper.convertToDtoList(cartProducts);

        // Then
        assertEquals(cartProducts.size(), cartProductDtos.size());
        assertEquals(cartProducts.get(0).getId(), cartProductDtos.get(0).getId());
        assertEquals(cartProducts.get(1).getId(), cartProductDtos.get(1).getId());
    }
}
