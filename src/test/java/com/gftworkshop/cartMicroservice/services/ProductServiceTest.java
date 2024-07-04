package com.gftworkshop.cartMicroservice.services;

import com.gftworkshop.cartMicroservice.api.dto.CartProductDto;
import com.gftworkshop.cartMicroservice.api.dto.Product;
import com.gftworkshop.cartMicroservice.exceptions.ExternalMicroserviceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ProductServiceTest {

    private MockWebServer mockWebServer;
    private ProductService productService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        RestClient restClient = RestClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        productService = new ProductService(restClient,
                mockWebServer.url("/").toString(),
                "/product/{productId}",
                "/catalog/products/{product_id}/price-checkout?quantity={quantity}",
                "/catalog/products/byIds",
                "/catalog/products/volumePromotion");
    }

    @Test
    @DisplayName("When fetching a product by ID, then the correct product details are returned")
    void testGetProductById() {
        String productJson = """
                {
                    "id": 1,
                    "name": "Laptop",
                    "description": "High-end gaming laptop",
                    "price": 1200.00,
                    "stock": 10,
                    "category": "Electronics",
                    "discount": 0.10,
                    "weight": 2.5
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(productJson)
                .addHeader("Content-Type", "application/json"));

        Product product = productService.getProductById(1L);

        assertNotNull(product);
        assertEquals(1L, (long) product.getId());
        assertEquals(0, new BigDecimal("1200.00").compareTo(product.getPrice()));
    }

    @Test
    @DisplayName("When fetching a product by ID and the product does not exist, then a 404 Not Found error is returned")
    void testGetProductByIdNotFound() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Product not found")
                .addHeader("Content-Type", "text/plain"));

        assertThrows(ExternalMicroserviceException.class, () -> {
            productService.getProductById(999L);
        });
    }

    @Test
    @DisplayName("When fetching a product by ID and an internal server error occurs, then a 500 error is returned")
    void testGetProductByIdServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "text/plain"));

        assertThrows(ExternalMicroserviceException.class, () -> {
            productService.getProductById(1L);
        });
    }

    @Test
    @DisplayName("When fetching discounted price, then the correct discounted price is returned")
    void testGetProductDiscountedPrice() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("75.0")
                .addHeader("Content-Type", "application/json"));

        float price = productService.getProductDiscountedPrice(1L, 2);
        assertEquals(75.0, price, 0.001);
    }

    @Test
    @DisplayName("When product ID does not exist for discount, then a 404 Not Found error is returned")
    void testGetProductDiscountedPriceNotFound() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value())
                .setBody("Product discount not found")
                .addHeader("Content-Type", "text/plain"));

        assertThrows(ExternalMicroserviceException.class, () -> {
            productService.getProductDiscountedPrice(999L, 1);
        });
    }

    @Test
    @DisplayName("When server error occurs during fetching discount, then a 500 error is returned")
    void testGetProductDiscountedPriceServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "text/plain"));

        assertThrows(ExternalMicroserviceException.class, () -> {
            productService.getProductDiscountedPrice(1L, 1);
        });
    }

    @Test
    @DisplayName("When the server returns malformed response, then handle the error gracefully")
    void testGetProductDiscountedPriceMalformedResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("not a float")
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .addHeader("Content-Type", "application/json"));

        assertThrows(ExternalMicroserviceException.class, () -> {
            productService.getProductDiscountedPrice(1L, 3);
        });
    }

    @Test
    @DisplayName("When fetching products by IDs, then the correct list of products is returned")
    void testFindProductsByIds() {
        String productsJson = """
                [
                    {
                        "id": 1,
                        "name": "Laptop",
                        "description": "High-end gaming laptop",
                        "price": 1200.00,
                        "stock": 10,
                        "category": "Electronics",
                        "discount": 0.10,
                        "weight": 2.5
                    },
                    {
                        "id": 2,
                        "name": "Smartphone",
                        "description": "Latest smartphone model",
                        "price": 800.00,
                        "stock": 15,
                        "category": "Electronics",
                        "discount": 0.05,
                        "weight": 0.5
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(productsJson)
                .addHeader("Content-Type", "application/json"));

        List<Product> products = productService.findProductsByIds(Arrays.asList(1L, 2L));

        assertNotNull(products);
        assertEquals(2, products.size());
        assertEquals(1L, (long) products.get(0).getId());
        assertEquals(2L, (long) products.get(1).getId());
    }

    @Test
    @DisplayName("When fetching products by IDs and an internal server error occurs, then a 500 error is returned")
    void testFindProductsByIdsServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "text/plain"));

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> {
            productService.findProductsByIds(Arrays.asList(1L, 2L));
        });
    }

    @Test
    @DisplayName("When fetching products by IDs and the server returns malformed response, then handle the error gracefully")
    void testFindProductsByIdsMalformedResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("not a valid JSON")
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .addHeader("Content-Type", "application/json"));

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> {
            productService.findProductsByIds(Arrays.asList(1L, 2L));
        });
    }

    @Test
    @DisplayName("When fetching products with discounted price, then the correct list of products with discounted price is returned")
    void testGetProductByIdWithDiscountedPrice() {
        String productsJson = """
            [
                {
                    "id": 1,
                    "name": "Laptop",
                    "description": "High-end gaming laptop",
                    "price": 1200.00,
                    "stock": 10,
                    "category": "Electronics",
                    "discount": 0.10,
                    "weight": 2.5
                },
                {
                    "id": 2,
                    "name": "Smartphone",
                    "description": "Latest smartphone model",
                    "price": 800.00,
                    "stock": 15,
                    "category": "Electronics",
                    "discount": 0.05,
                    "weight": 0.5
                }
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(productsJson)
                .addHeader("Content-Type", "application/json"));

        CartProductDto cartProductDto1 = CartProductDto.builder()
                .id(1L)
                .productId(1L)
                .productName("Laptop")
                .productDescription("High-end gaming laptop")
                .quantity(1)
                .price(new BigDecimal("1200.00"))
                .build();

        CartProductDto cartProductDto2 = CartProductDto.builder()
                .id(2L)
                .productId(2L)
                .productName("Smartphone")
                .productDescription("Latest smartphone model")
                .quantity(1)
                .price(new BigDecimal("800.00"))
                .build();

        List<CartProductDto> cartProducts = Arrays.asList(cartProductDto1, cartProductDto2);

        List<Product> products = productService.getProductByIdWithDiscountedPrice(cartProducts);

        assertNotNull(products);
        assertEquals(2, products.size());
        assertEquals(1L, (long) products.get(0).getId());
        assertEquals(2L, (long) products.get(1).getId());
        assertEquals("Laptop", products.get(0).getName());
        assertEquals("Smartphone", products.get(1).getName());
    }

    @Test
    @DisplayName("When fetching products with discounted price and an error occurs, then ExternalMicroserviceException is thrown")
    void testGetProductByIdWithDiscountedPriceError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setBody("Internal Server Error")
                .addHeader("Content-Type", "text/plain"));

        CartProductDto cartProductDto1 = CartProductDto.builder()
                .id(1L)
                .productId(1L)
                .productName("Laptop")
                .productDescription("High-end gaming laptop")
                .quantity(1)
                .price(new BigDecimal("1200.00"))
                .build();

        CartProductDto cartProductDto2 = CartProductDto.builder()
                .id(2L)
                .productId(2L)
                .productName("Smartphone")
                .productDescription("Latest smartphone model")
                .quantity(1)
                .price(new BigDecimal("800.00"))
                .build();

        List<CartProductDto> cartProducts = Arrays.asList(cartProductDto1, cartProductDto2);

        assertThrows(ExternalMicroserviceException.class, () -> {
            productService.getProductByIdWithDiscountedPrice(cartProducts);
        });
    }


    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
}
