package com.gftworkshop.cartMicroservice.cartmanagement;

import com.gftworkshop.cartMicroservice.api.dto.CartDto;
import com.gftworkshop.cartMicroservice.api.dto.Product;
import com.gftworkshop.cartMicroservice.entitymapper.EntityMapper;
import com.gftworkshop.cartMicroservice.exceptions.CartNotFoundException;
import com.gftworkshop.cartMicroservice.exceptions.UserWithCartException;
import com.gftworkshop.cartMicroservice.model.Cart;
import com.gftworkshop.cartMicroservice.model.CartProduct;
import com.gftworkshop.cartMicroservice.repositories.CartProductRepository;
import com.gftworkshop.cartMicroservice.repositories.CartRepository;
import com.gftworkshop.cartMicroservice.services.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class CartManager {

    private final CartRepository cartRepository;
    private final CartProductRepository cartProductRepository;
    private final ProductService productService;
    private final CartCalculator cartCalculator;

    public void handleCartProduct(CartProduct cartProduct) {
        Optional<CartProduct> existingCartProduct = findExistingCartProduct(cartProduct);
        if (existingCartProduct.isPresent()) {
            updateExistingCartProduct(existingCartProduct.get(), cartProduct);
        } else {
            addNewCartProduct(cartProduct);
        }
    }

    public void saveCart(Cart cart) {
        cartRepository.save(cart);
    }

    public void checkForAbandonedCarts() {
        LocalDate thresholdDate = LocalDate.now().minusDays(1);
        identifyAbandonedCarts(thresholdDate);
    }

    public Optional<CartProduct> findExistingCartProduct(CartProduct cartProduct) {
        return cartProductRepository.findByCartIdAndProductId(cartProduct.getCart().getId(), cartProduct.getProductId());
    }

    public void updateExistingCartProduct(CartProduct existingCartProduct, CartProduct newCartProduct) {
        existingCartProduct.setQuantity(existingCartProduct.getQuantity() + newCartProduct.getQuantity());
        cartProductRepository.save(existingCartProduct);
    }

    public void addNewCartProduct(CartProduct cartProduct) {
        Cart cart = fetchCartById(cartProduct.getCart().getId());
        addCartProduct(cart, cartProduct);
    }

    public void addCartProduct(Cart cart, CartProduct cartProduct) {
        cart.getCartProducts().add(cartProduct);
        cartProduct.setCart(cart);
        cartProductRepository.save(cartProduct);
    }

    public Cart fetchCartById(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));
    }

    public List<CartDto> convertCartsToDto(List<Cart> abandonedCarts) {
        return abandonedCarts.stream()
                .map(EntityMapper::convertCartToDto)
                .toList();
    }

    public void logAbandonedCartsInfo(List<Cart> abandonedCarts, LocalDate thresholdDate) {
        if (abandonedCarts.isEmpty()) {
            log.info(CartErrorMessages.NO_ABANDONED_CARTS_FOUND + "{}", thresholdDate);
        } else {
            log.info(CartErrorMessages.FOUND_ABANDONED_CARTS + "{}", abandonedCarts.size(), thresholdDate);
            abandonedCarts.forEach(cart -> log.debug(CartErrorMessages.ABANDONED_CART + "{}", cart.getId(), cart.getUpdatedAt()));
        }
    }

    public List<Cart> fetchAbandonedCarts(LocalDate thresholdDate) {
        return cartRepository.identifyAbandonedCarts(thresholdDate);
    }

    public void ensureUserDoesNotAlreadyHaveCart(Long userId) {
        cartRepository.findByUserId(userId)
                .ifPresent(cart -> {
                    throw new UserWithCartException(CartErrorMessages.USER_ALREADY_HAS_CART + " ID " + userId + " already has a cart.");
                });
    }

    public Cart buildAndSaveCart(Long userId) {
        Cart cart = Cart.builder()
                .updatedAt(LocalDate.now())
                .userId(userId)
                .build();
        return cartRepository.save(cart);
    }

    public void updateAndSaveCartProductInfo(Cart cart) {
        Map<Long, Product> productMap = getProductMap(cart);
        updateCartProductsInfo(cart, productMap);
        updateCartTimestamp(cart);
    }

    public void updateCartProductsInfo(Cart cart, Map<Long, Product> productMap) {
        cart.getCartProducts().forEach(cartProduct -> {
            Product product = productMap.get(cartProduct.getProductId());
            if (product != null) {
                setCartProductInfo(cartProduct, product);
            }
        });
        cartProductRepository.saveAll(cart.getCartProducts());
    }

    public void setCartProductInfo(CartProduct cartProduct, Product product) {
        cartProduct.setPrice(product.getPrice());
        cartProduct.setProductName(product.getName());
        cartProduct.setProductDescription(product.getDescription());
    }

    public void clearCartProducts(Long cartId, Cart cart) {
        cartProductRepository.removeAllByCartId(cartId);
        cart.getCartProducts().clear();
    }

    public void updateCartTimestamp(Cart cart) {
        cart.setUpdatedAt(LocalDate.now());
    }

    public Map<Long, Product> getProductMap(Cart cart) {
        List<Long> productIds = cart.getCartProducts().stream()
                .map(CartProduct::getProductId)
                .toList();
        return productService.findProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    public CartDto prepareCartDto(Cart cart) {
        CartDto cartDto = EntityMapper.convertCartToDto(cart);
        cartDto.setTotalPrice(cartCalculator.calculateCartTotal(cart.getId(), cart.getUserId()));
        return cartDto;
    }

    public void identifyAbandonedCarts(LocalDate thresholdDate) {
        List<Cart> abandonedCarts = fetchAbandonedCarts(thresholdDate);
        logAbandonedCartsInfo(abandonedCarts, thresholdDate);
        convertCartsToDto(abandonedCarts);
    }

    public List<Cart> fetchAllCarts() {
        return cartRepository.findAll();
    }
}
