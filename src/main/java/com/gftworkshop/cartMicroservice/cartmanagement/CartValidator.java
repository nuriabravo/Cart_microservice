package com.gftworkshop.cartMicroservice.cartmanagement;

import com.gftworkshop.cartMicroservice.api.dto.Product;
import com.gftworkshop.cartMicroservice.exceptions.CartProductInvalidQuantityException;
import com.gftworkshop.cartMicroservice.model.Cart;
import com.gftworkshop.cartMicroservice.model.CartProduct;
import com.gftworkshop.cartMicroservice.repositories.CartProductRepository;
import com.gftworkshop.cartMicroservice.services.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CartValidator {

    private final ProductService productService;
    private final CartProductRepository cartProductRepository;

    public void validateProductStock(CartProduct cartProduct) {
        int totalDesiredQuantity = calculateTotalDesiredQuantity(cartProduct);
        int availableStock = getAvailableStock(cartProduct);

        if (totalDesiredQuantity > availableStock) {
            throw new CartProductInvalidQuantityException(
                    CartErrorMessages.NOT_ENOUGH_STOCK + totalDesiredQuantity + CartErrorMessages.ACTUAL_STOCK + availableStock);
        }
    }

    public int calculateTotalDesiredQuantity(CartProduct cartProduct) {
        return getCurrentQuantity(cartProduct) + cartProduct.getQuantity();
    }

    public int getCurrentQuantity(CartProduct cartProduct) {
        return cartProductRepository.findByCartIdAndProductId(cartProduct.getCart().getId(), cartProduct.getProductId())
                .map(CartProduct::getQuantity)
                .orElse(0);
    }

    public int getAvailableStock(CartProduct cartProduct) {
        return productService.getProductById(cartProduct.getProductId()).getCurrentStock();
    }

    public void validateCartProductsStock(Cart cart) {
        Map<Long, Product> productMap = getProductMap(cart);
        checkStockForCartProducts(cart, productMap);
    }

    public void checkStockForCartProducts(Cart cart, Map<Long, Product> productMap) {
        cart.getCartProducts().forEach(cartProduct -> {
            Product product = productMap.get(cartProduct.getProductId());
            if (cartProduct.getQuantity() > product.getCurrentStock()) {
                throw new CartProductInvalidQuantityException(
                        CartErrorMessages.NOT_ENOUGH_STOCK + cartProduct.getQuantity() +
                                CartErrorMessages.ACTUAL_STOCK + product.getCurrentStock());
            }
        });
    }

    public Map<Long, Product> getProductMap(Cart cart) {
        List<Long> productIds = cart.getCartProducts().stream()
                .map(CartProduct::getProductId)
                .toList();
        return productService.findProductsByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }
}
