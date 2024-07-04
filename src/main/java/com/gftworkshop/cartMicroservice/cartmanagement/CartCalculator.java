package com.gftworkshop.cartMicroservice.cartmanagement;

import com.gftworkshop.cartMicroservice.api.dto.CartProductDto;
import com.gftworkshop.cartMicroservice.api.dto.Product;
import com.gftworkshop.cartMicroservice.api.dto.User;
import com.gftworkshop.cartMicroservice.entitymapper.EntityMapper;
import com.gftworkshop.cartMicroservice.exceptions.CartNotFoundException;
import com.gftworkshop.cartMicroservice.model.Cart;
import com.gftworkshop.cartMicroservice.repositories.CartRepository;
import com.gftworkshop.cartMicroservice.services.ProductService;
import com.gftworkshop.cartMicroservice.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class CartCalculator {

    private final ProductService productService;
    private final UserService userService;
    private final CartRepository cartRepository;

    public BigDecimal calculateCartTotal(Long cartId, Long userId) {
        User user = userService.getUserById(userId);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException(CartErrorMessages.CART_NOT_FOUND + cartId));

        List<CartProductDto> cartProductDtos = EntityMapper.convertToDtoList(cart.getCartProducts());
        List<Product> products = productService.getProductByIdWithDiscountedPrice(cartProductDtos);

        BigDecimal totalProductCost = computeProductTotal(products);
        BigDecimal tax = computeTax(totalProductCost, user);
        BigDecimal shippingCost = computeShippingCost(computeTotalWeight(products));

        return totalProductCost.add(tax).add(shippingCost);
    }

    public BigDecimal computeProductTotal(List<Product> products) {
        return products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal computeTax(BigDecimal total, User user) {
        return total.multiply(BigDecimal.valueOf(user.getCountry().getTax() / 100.0));
    }

    public BigDecimal computeShippingCost(double totalWeight) {
        List<SimpleEntry<Double, BigDecimal>> weightCosts = createWeightCostList();
        return findShippingCostForWeight(totalWeight, weightCosts);
    }

    public List<SimpleEntry<Double, BigDecimal>> createWeightCostList() {
        return List.of(
                new SimpleEntry<>(5.0, new BigDecimal("5")),
                new SimpleEntry<>(10.0, new BigDecimal("10")),
                new SimpleEntry<>(20.0, new BigDecimal("20")),
                new SimpleEntry<>(Double.MAX_VALUE, new BigDecimal("50"))
        );
    }

    public BigDecimal findShippingCostForWeight(double totalWeight, List<SimpleEntry<Double, BigDecimal>> weightCosts) {
        return weightCosts.stream()
                .filter(entry -> totalWeight <= entry.getKey())
                .map(SimpleEntry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid weight: " + totalWeight));
    }

    public double computeTotalWeight(List<Product> products) {
        return products.stream()
                .mapToDouble(Product::getWeight)
                .sum();
    }
}
