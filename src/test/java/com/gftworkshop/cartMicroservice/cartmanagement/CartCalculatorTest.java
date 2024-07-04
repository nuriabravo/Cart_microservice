package com.gftworkshop.cartMicroservice.cartmanagement;

import com.gftworkshop.cartMicroservice.api.dto.Country;
import com.gftworkshop.cartMicroservice.api.dto.Product;
import com.gftworkshop.cartMicroservice.api.dto.User;
import com.gftworkshop.cartMicroservice.model.Cart;
import com.gftworkshop.cartMicroservice.model.CartProduct;
import com.gftworkshop.cartMicroservice.repositories.CartRepository;
import com.gftworkshop.cartMicroservice.services.ProductService;
import com.gftworkshop.cartMicroservice.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("CartCalculator Unit Tests")
class CartCalculatorTest {

    private ProductService productService;
    private UserService userService;
    private CartRepository cartRepository;
    private CartCalculator cartCalculator;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        userService = mock(UserService.class);
        cartRepository = mock(CartRepository.class);
        cartCalculator = new CartCalculator(productService, userService, cartRepository);
    }

    @Test
    @DisplayName("Test calculateCartTotal")
    void testCalculateCartTotal() {
        Long userId = 1L;
        Long cartId = 1L;
        User user = User.builder().country(new Country(1L, 10.0)).id(userId).build();
        List<CartProduct> cartProducts = List.of(
                CartProduct.builder().productId(1L).quantity(1).price(new BigDecimal("10")).build(),
                CartProduct.builder().productId(2L).quantity(1).price(new BigDecimal("20")).build()
        );
        Cart cart = Cart.builder().id(cartId).userId(userId).cartProducts(cartProducts).build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(userService.getUserById(userId)).thenReturn(user);

        Product product1 = Product.builder().id(1L).price(new BigDecimal("10")).weight(1.0).build();
        Product product2 = Product.builder().id(2L).price(new BigDecimal("20")).weight(1.0).build();
        List<Product> products = new ArrayList<>();
        products.add(product1);
        products.add(product2);

        when(productService.getProductByIdWithDiscountedPrice(anyList())).thenReturn(products);


        BigDecimal expectedTotal = new BigDecimal("38.0"); // Total products = 10+20, Tax = 10%, Shipping = 5
        BigDecimal cartTotal = cartCalculator.calculateCartTotal(cartId, userId);

        assertEquals(expectedTotal, cartTotal);
    }


    @Test
    @DisplayName("Test computeProductTotal")
    void testComputeProductTotal() {
        // Given
        List<Product> products = List.of(
                new Product(1L, "Product1", "Description1", BigDecimal.TEN, 5, 2.0),
                new Product(2L, "Product2", "Description2", BigDecimal.valueOf(20), 10, 3.0)
        );
        BigDecimal expectedTotal = BigDecimal.valueOf(30);

        // When
        BigDecimal total = cartCalculator.computeProductTotal(products);

        // Then
        assertEquals(expectedTotal, total);
    }

    @Test
    @DisplayName("Test computeTax")
    void testComputeTax() {
        // Given
        BigDecimal total = BigDecimal.valueOf(100);
        User user = new User();
        user.setCountry(new Country(1L, 10.0));
        BigDecimal expectedTax = BigDecimal.TEN;

        // When
        BigDecimal tax = cartCalculator.computeTax(total, user);

        // Then
        assertEquals(0, expectedTax.compareTo(tax));
    }


    @Test
    @DisplayName("Test computeShippingCost")
    void testComputeShippingCost() {
        // Given
        double totalWeight = 15.0;
        List<AbstractMap.SimpleEntry<Double, BigDecimal>> weightCosts = List.of(
                new AbstractMap.SimpleEntry<>(5.0, BigDecimal.valueOf(5)),
                new AbstractMap.SimpleEntry<>(10.0, BigDecimal.valueOf(10)),
                new AbstractMap.SimpleEntry<>(20.0, BigDecimal.valueOf(20)),
                new AbstractMap.SimpleEntry<>(Double.MAX_VALUE, BigDecimal.valueOf(50))
        );
        BigDecimal expectedShippingCost = BigDecimal.valueOf(20);

        // When
        BigDecimal shippingCost = cartCalculator.findShippingCostForWeight(totalWeight, weightCosts);

        // Then
        assertEquals(expectedShippingCost, shippingCost);
    }

    @Test
    @DisplayName("Test computeTotalWeight")
    void testComputeTotalWeight() {
        // Given
        List<Product> products = List.of(
                new Product(1L, "Product1", "Description1", BigDecimal.TEN, 5, 2.0),
                new Product(2L, "Product2", "Description2", BigDecimal.valueOf(20), 10, 3.0)
        );
        double expectedTotalWeight = 5.0;

        // When
        double totalWeight = cartCalculator.computeTotalWeight(products);

        // Then
        assertEquals(expectedTotalWeight, totalWeight);
    }
}
