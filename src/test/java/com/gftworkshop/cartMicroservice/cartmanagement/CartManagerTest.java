package com.gftworkshop.cartMicroservice.cartmanagement;

import ch.qos.logback.classic.LoggerContext;
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
import com.gftworkshop.cartMicroservice.services.UserService;
import com.mysql.cj.log.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.hibernate.id.factory.IdGenFactoryLogging.LOGGER_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@DisplayName("CartManager Unit Tests")
class CartManagerTest {

    @Mock
    private Logger log;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartProductRepository cartProductRepository;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private CartCalculator cartCalculator;

    @InjectMocks
    private CartManager cartManager;
    @Mock
    private CartManager cartManagerInternal;
    @Mock
    private EntityMapper entityMapper;

    private Cart cart;
    private List<CartProduct> cartProducts;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setUpdatedAt(LocalDate.now());
        cartProducts = new ArrayList<>();
        cart.setCartProducts(cartProducts);
        CartProduct cartProduct = CartProduct.builder().productId(1L).cart(cart).id(1L).build();
        cart.getCartProducts().add(cartProduct);
    }

    @Test
    @DisplayName("Test fetchCartById - Existing Cart")
    void testFetchCartByIdExisting() {
        Long cartId = 1L;
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        Cart actualCart = cartManager.fetchCartById(cartId);

        assertEquals(cart, actualCart);
    }

    @Test
    @DisplayName("Test fetchAllCarts - Empty List")
    void testFetchAllCartsEmpty() {
        List<Cart> expectedCarts = new ArrayList<>();
        when(cartRepository.findAll()).thenReturn(expectedCarts);

        List<Cart> actualCarts = cartManager.fetchAllCarts();

        assertEquals(expectedCarts, actualCarts);
    }

    @Test
    @DisplayName("Test fetchAllCarts - Non-empty List")
    void testFetchAllCartsNonEmpty() {
        List<Cart> expectedCarts = new ArrayList<>();
        expectedCarts.add(cart);
        when(cartRepository.findAll()).thenReturn(expectedCarts);

        List<Cart> actualCarts = cartManager.fetchAllCarts();

        assertEquals(expectedCarts, actualCarts);
    }

    @Test
    @DisplayName("Test fetchCartById - Nonexistent Cart")
    void testFetchCartByIdNonexistent() {
        Long cartId = 1L;
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartManager.fetchCartById(cartId));
    }

    @Test
    @DisplayName("Test addCartProduct")
    void testAddCartProduct() {
        CartProduct cartProduct = new CartProduct();

        cartManager.addCartProduct(cart, cartProduct);

        assertTrue(cart.getCartProducts().contains(cartProduct));
        assertEquals(cart, cartProduct.getCart());
    }

    @Test
    @DisplayName("Test updateCartTimestamp")
    void testUpdateCartTimestamp() {
        LocalDate initialUpdatedAt = cart.getUpdatedAt();

        cartManager.updateCartTimestamp(cart);

        assertEquals(LocalDate.now(), cart.getUpdatedAt());
    }

    @Test
    @DisplayName("Test handleCartProduct - Existing Product")
    void testHandleCartProductExisting() {
        cart.setId(1L);

        CartProduct existingCartProduct = new CartProduct();
        existingCartProduct.setCart(cart);
        existingCartProduct.setProductId(1L);
        existingCartProduct.setQuantity(2);

        CartProduct newCartProduct = new CartProduct();
        newCartProduct.setCart(cart);
        newCartProduct.setProductId(1L);
        newCartProduct.setQuantity(3);

        when(cartProductRepository.findByCartIdAndProductId(cart.getId(), newCartProduct.getProductId()))
                .thenReturn(Optional.of(existingCartProduct));

        cartManager.handleCartProduct(newCartProduct);

        assertEquals(5, existingCartProduct.getQuantity());
        verify(cartProductRepository, times(1)).save(existingCartProduct);
    }

    @Test
    @DisplayName("Test handleCartProduct - New Product")
    void testHandleCartProductNew() {
        cart.setId(1L);

        CartProduct newCartProduct = new CartProduct();
        newCartProduct.setCart(cart);
        newCartProduct.setProductId(1L);
        newCartProduct.setQuantity(3);

        when(cartProductRepository.findByCartIdAndProductId(cart.getId(), newCartProduct.getProductId()))
                .thenReturn(Optional.empty());
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));

        cartManager.handleCartProduct(newCartProduct);

        assertTrue(cart.getCartProducts().contains(newCartProduct));
        verify(cartProductRepository, times(1)).save(newCartProduct);
    }

    @Test
    @DisplayName("Test ensureUserDoesNotAlreadyHaveCart - User without Cart")
    void testEnsureUserDoesNotAlreadyHaveCartWithoutCart() {
        Long userId = 1L;
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> cartManager.ensureUserDoesNotAlreadyHaveCart(userId));
    }

    @Test
    @DisplayName("Test ensureUserDoesNotAlreadyHaveCart - User with Cart")
    void testEnsureUserDoesNotAlreadyHaveCartWithCart() {
        Long userId = 1L;
        Cart existingCart = new Cart();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

        assertThrows(UserWithCartException.class, () -> cartManager.ensureUserDoesNotAlreadyHaveCart(userId));
    }

    @Test
    @DisplayName("Test buildAndSaveCart")
    void testBuildAndSaveCart() {
        Long userId = 1L;
        Cart expectedCart = new Cart();
        expectedCart.setUserId(userId);
        expectedCart.setUpdatedAt(LocalDate.now());
        when(cartRepository.save(any(Cart.class))).thenReturn(expectedCart);

        Cart actualCart = cartManager.buildAndSaveCart(userId);

        assertEquals(expectedCart, actualCart);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Test updateAndSaveCartProductInfo")
    void testUpdateAndSaveCartProductInfo() {
        CartProduct cartProduct = new CartProduct();
        cartProduct.setProductId(1L);
        cart.getCartProducts().add(cartProduct);


        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("10.0"));
        product.setDescription("Test Description");

        when(productService.findProductsByIds(anyList())).thenReturn(Collections.singletonList(product));

        cartManager.updateAndSaveCartProductInfo(cart);

        assertEquals("Test Product", cartProduct.getProductName());
        assertEquals(new BigDecimal("10.0"), cartProduct.getPrice());
        assertEquals("Test Description", cartProduct.getProductDescription());
        verify(cartProductRepository, times(1)).saveAll(cart.getCartProducts());
    }

    @Test
    @DisplayName("Test updateCartProductsInfo - Updates and Saves Product Info")
    void testUpdateCartProductsInfo() {
        Cart cart = new Cart();
        cart.setId(1L);
        List<CartProduct> cartProducts = new ArrayList<>();
        CartProduct cartProduct1 = new CartProduct();
        cartProduct1.setId(1L);
        cartProduct1.setProductId(101L);
        CartProduct cartProduct2 = new CartProduct();
        cartProduct2.setId(2L);
        cartProduct2.setProductId(102L);
        cartProducts.add(cartProduct1);
        cartProducts.add(cartProduct2);
        cart.setCartProducts(cartProducts);

        Product product1 = new Product();
        product1.setId(101L);
        product1.setName("Product1");
        product1.setPrice(new BigDecimal("20.00"));

        Product product2 = new Product();
        product2.setId(102L);
        product2.setName("Product2");
        product2.setPrice(new BigDecimal("30.00"));

        Map<Long, Product> productMap = new HashMap<>();
        productMap.put(101L, product1);
        productMap.put(102L, product2);

        when(cartProductRepository.saveAll(anyList())).thenReturn(cartProducts);

        cartManager.updateCartProductsInfo(cart, productMap);

        verify(cartProductRepository, times(1)).saveAll(cartProducts);

        assertAll(
                () -> assertNotNull(cartProduct1.getProductName(), "Product name should be set for cartProduct1"),
                () -> assertNotNull(cartProduct2.getProductName(), "Product name should be set for cartProduct2")
        );
    }

    @Test
    @DisplayName("Test updateCartProductsInfo with null Product")
    void testUpdateCartProductsInfoWithNullProduct() {

        Map<Long, Product> productMap = new HashMap<>();
        productMap.put(102L, null);

        cartManager.updateCartProductsInfo(cart, productMap);

        assertNull(cartProducts.get(0).getProductName(), "Product name should be null for cartProduct2");
        verify(cartProductRepository, times(1)).saveAll(cart.getCartProducts());
    }

    @Test
    @DisplayName("Test clearCartProducts")
    void testClearCartProducts() {
        Long cartId = 1L;
        CartProduct cartProduct = new CartProduct();
        cart.getCartProducts().add(cartProduct);

        cartManager.clearCartProducts(cartId, cart);

        assertTrue(cart.getCartProducts().isEmpty());
        verify(cartProductRepository, times(1)).removeAllByCartId(cartId);
    }

    @Test
    @DisplayName("Test saveCart")
    void testSaveCart() {
        cartManager.saveCart(cart);

        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    @DisplayName("Test checkForAbandonedCarts")
    public void testCheckForAbandonedCarts_NoAbandonedCarts() {
        LocalDate thresholdDate = LocalDate.now().minusDays(1);
        when(cartRepository.identifyAbandonedCarts(thresholdDate)).thenReturn(Collections.emptyList());

        cartManager.checkForAbandonedCarts();

        verify(log, never()).debug(anyString(), any(), any());
        verify(cartRepository).identifyAbandonedCarts(thresholdDate);
    }

    @Test
    @DisplayName("Test convertCartsToDto")
    public void testConvertCartsToDto_ValidList() {
        try (MockedStatic<EntityMapper> mocked = Mockito.mockStatic(EntityMapper.class)) {
            Cart cart1 = Cart.builder().id(1L).build();
            Cart cart2 = Cart.builder().id(1L).build();
            List<Cart> carts = Arrays.asList(cart1, cart2);

            CartDto cartDto1 = CartDto.builder().id(1L).build();
            CartDto cartDto2 = CartDto.builder().id(1L).build();

            mocked.when(() -> EntityMapper.convertCartToDto(cart1)).thenReturn(cartDto1);
            mocked.when(() -> EntityMapper.convertCartToDto(cart2)).thenReturn(cartDto2);

            List<CartDto> result = cartManager.convertCartsToDto(carts);

            assertEquals(2, result.size(), "The size of the result list should be equal to the input list size");
            assertEquals(cartDto1, result.get(0), "The first DTO should match the first input cart conversion");
            assertEquals(cartDto2, result.get(1), "The second DTO should match the second input cart conversion");
        }
    }

    @Test
    public void testIdentifyAbandonedCartsWithNoCarts() {
        LocalDate thresholdDate = LocalDate.now().minusDays(1);
        when(cartRepository.identifyAbandonedCarts(thresholdDate)).thenReturn(Collections.emptyList());
        cartManager.identifyAbandonedCarts(thresholdDate);
        verify(cartRepository).identifyAbandonedCarts(thresholdDate);
    }

    @Test
    public void testLogAbandonedCartsInfo_EmptyList() {
        List<Cart> abandonedCarts = Collections.emptyList();
        LocalDate thresholdDate = LocalDate.now();

        cartManager.logAbandonedCartsInfo(abandonedCarts, thresholdDate);

        verify(log, never()).debug(anyString(), any(), any());
        verifyNoMoreInteractions(log);
    }

    @Test
    public void testLogAbandonedCartsInfo_NonEmptyList() {
        Cart cart1 = Cart.builder().updatedAt(LocalDate.now().minusDays(1)).build();
        List<Cart> abandonedCarts = Arrays.asList(cart1);
        LocalDate thresholdDate = LocalDate.now();

        cartManager.logAbandonedCartsInfo(abandonedCarts, thresholdDate);

        verify(log, never()).debug(anyString(), any(), any());
        verifyNoMoreInteractions(log);
    }


    @Test
    @DisplayName("Test prepareCartDto")
    void testPrepareCartDto() {
        CartDto expectedCartDto = new CartDto();
        expectedCartDto.setTotalPrice(new BigDecimal("100.00"));

        mockStatic(EntityMapper.class);
        when(EntityMapper.convertCartToDto(cart)).thenReturn(expectedCartDto);
        when(cartCalculator.calculateCartTotal(cart.getId(), cart.getUserId())).thenReturn(new BigDecimal("100.00"));

        CartDto actualCartDto = cartManager.prepareCartDto(cart);

        assertEquals(expectedCartDto, actualCartDto);
    }

    @Test
    @DisplayName("Test findExistingCartProduct - Existing Product")
    void testFindExistingCartProductExistingProduct() {
        CartProduct expectedCartProduct = new CartProduct();
        expectedCartProduct.setId(1L);
        expectedCartProduct.setCart(cart);
        when(cartProductRepository.findByCartIdAndProductId(cart.getId(), expectedCartProduct.getProductId()))
                .thenReturn(Optional.of(expectedCartProduct));

        Optional<CartProduct> actualCartProduct = cartManager.findExistingCartProduct(expectedCartProduct);

        assertEquals(Optional.of(expectedCartProduct), actualCartProduct);
    }

    @Test
    @DisplayName("Test findExistingCartProduct - Nonexistent Product")
    void testFindExistingCartProductNonexistentProduct() {
        CartProduct cartProduct = new CartProduct();
        cartProduct.setId(1L);
        cartProduct.setCart(cart);
        when(cartProductRepository.findByCartIdAndProductId(cart.getId(), cartProduct.getProductId()))
                .thenReturn(Optional.empty());

        Optional<CartProduct> actualCartProduct = cartManager.findExistingCartProduct(cartProduct);

        assertEquals(Optional.empty(), actualCartProduct);
    }

    @Test
    @DisplayName("Test updateExistingCartProduct")
    void testUpdateExistingCartProduct() {
        CartProduct existingCartProduct = new CartProduct();
        existingCartProduct.setQuantity(2);
        CartProduct newCartProduct = new CartProduct();
        newCartProduct.setQuantity(3);

        cartManager.updateExistingCartProduct(existingCartProduct, newCartProduct);

        assertEquals(5, existingCartProduct.getQuantity());
        verify(cartProductRepository, times(1)).save(existingCartProduct);
    }

    @Test
    @DisplayName("Test addNewCartProduct")
    void testAddNewCartProduct() {
        CartProduct cartProduct = new CartProduct();
        cartProduct.setCart(cart);
        cart.setId(1L);
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));

        cartManager.addNewCartProduct(cartProduct);

        verify(cartProductRepository, times(1)).save(cartProduct);
    }

    @Test
    @DisplayName("Test getProductMap")
    void testGetProductMap() {
        CartProduct cartProduct = new CartProduct();
        cartProduct.setProductId(1L);
        cart.getCartProducts().add(cartProduct);

        Product product = new Product();
        product.setId(1L);

        when(productService.findProductsByIds(anyList())).thenReturn(Collections.singletonList(product));

        Map<Long, Product> productMap = cartManager.getProductMap(cart);

        assertEquals(1, productMap.size());
        assertEquals(product, productMap.get(1L));
    }
}
