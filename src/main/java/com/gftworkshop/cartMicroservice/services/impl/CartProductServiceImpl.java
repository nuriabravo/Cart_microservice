package com.gftworkshop.cartMicroservice.services.impl;

import com.gftworkshop.cartMicroservice.exceptions.CartNotFoundException;
import com.gftworkshop.cartMicroservice.api.dto.CartProductDto;
import com.gftworkshop.cartMicroservice.exceptions.CartProductInvalidQuantityException;
import com.gftworkshop.cartMicroservice.exceptions.CartProductNotFoundException;
import com.gftworkshop.cartMicroservice.model.Cart;
import com.gftworkshop.cartMicroservice.model.CartProduct;
import com.gftworkshop.cartMicroservice.repositories.CartProductRepository;
import com.gftworkshop.cartMicroservice.repositories.CartRepository;
import com.gftworkshop.cartMicroservice.services.CartProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class CartProductServiceImpl implements CartProductService {

    private final CartProductRepository cartProductRepository;
    private final CartRepository cartRepository;

    public CartProductServiceImpl(CartProductRepository cartProductRepository, CartRepository cartRepository) {
        this.cartProductRepository = cartProductRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public int updateQuantity(Long id, int quantity) {
        if (quantity <= 0) {
            throw new CartProductInvalidQuantityException("The quantity must be higher than 0");
        }

        Optional<CartProduct> cartProduct = cartProductRepository.findById(id);
        if(cartProduct.isEmpty()){
            throw new CartProductNotFoundException("CartProduct with ID " + id + " not found");
        }
        long cartId = cartProduct.get().getCart().getId();

        Optional<Cart> optionalCart = cartRepository.findById(cartId);
        if (optionalCart.isEmpty()) {
            throw new CartNotFoundException("Cart with ID " + cartId + " not found");
        }
        log.info("Updating quantity for CartProduct with ID {} to {}", id, quantity);
        int updatedQuantity = cartProductRepository.updateQuantity(id, quantity);
        log.info("Quantity updated successfully for CartProduct with ID {} to {}", id, quantity);
        return updatedQuantity;
    }

    @Override
    public CartProductDto removeProduct(Long id) {
        log.info("Removing CartProduct with ID {}", id);
        return cartProductRepository.findById(id)
                .map(cartProduct -> {
                    cartProductRepository.deleteById(id);
                    return entityToDto(cartProduct);
                })
                .orElseThrow(() -> new CartProductNotFoundException("No se encontró el CartProduct con ID: " + id));
    }

    private CartProductDto entityToDto(CartProduct cartProduct) {
        CartProductDto cartProductDto = CartProductDto.builder().build();
        BeanUtils.copyProperties(cartProduct, cartProductDto);
        return cartProductDto;
    }
}
