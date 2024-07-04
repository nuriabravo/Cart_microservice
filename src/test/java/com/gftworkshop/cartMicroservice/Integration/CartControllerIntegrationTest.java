package com.gftworkshop.cartMicroservice.Integration;

import com.gftworkshop.cartMicroservice.Integration.responses.JsonData;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class CartControllerIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(
                    wireMockConfig()
                            .dynamicPort()
                            .usingFilesUnderClasspath("wiremock")

            )
            .build();

    private Long productId;
    private Long cartProductId;
    private Long cartId;

    @BeforeEach
    void setUp() {

        wireMockServer.stubFor(WireMock.get(urlMatching("/users/.*"))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(JsonData.USER.getJson())));

        productId = 1L;
        cartProductId = 1L;
        cartId = 1L;

        wireMockServer.stubFor(WireMock.get(urlMatching("/catalog/products/" + productId))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(JsonData.CARTPRODUCT.getJson())));

        wireMockServer.stubFor(WireMock.post(urlMatching("/catalog/products/byIds"))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(JsonData.PRODUCTS.getJson())));

        wireMockServer.stubFor(WireMock.get(urlMatching("/catalog/products/volumePromotion"))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(JsonData.VOLUMEPROMOTION_PRODUCT.getJson())));

    }

    @Nested
    @DisplayName("GET - Tests for getting a cart by id")
    class GetCartByIdEndpoint {
        @Test
        void getCartByIdTest() throws Exception {

            mockMvc.perform(get("/carts/{id}", cartId))
                    .andExpect(status().isOk());
        }

        @Test
        void getCartByIdBadRequestTest() throws Exception {
            String invalidId = "abc";

            mockMvc.perform(get("/carts/{id}", invalidId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)))
                    .andExpect(jsonPath("$.message", is("Invalid input")));
        }

        @Test
        void getCartByIdNotFoundTest() throws Exception {
            Long nonExistentId = 999L;

            wireMockServer.stubFor(WireMock.get(urlMatching("/catalog/products/.*"))
                    .willReturn(aResponse().withStatus(404)));

            mockMvc.perform(get("/carts/{id}", nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code", is(404)))
                    .andExpect(jsonPath("$.message", is("Cart with ID " + nonExistentId + " not found")));
        }
    }

    @Nested
    @DisplayName("DELETE - Test for removing a cart by id")
    class RemoveCartByIdEndpoint {

        @Test
        void removeCartByIdTest() throws Exception {
            Long cartId = 1L;

            mockMvc.perform(delete("/carts/{id}", cartId))
                    .andExpect(status().isOk());
        }

        @Test
        void removeCartById_NotFoundTest() throws Exception {
            Long nonExistentId = 999L;

            mockMvc.perform(delete("/carts/{id}", nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code", is(404)))
                    .andExpect(jsonPath("$.message", is("Cart with ID " + nonExistentId + " not found")));
        }

        @Test
        void removeCartById_BadRequest_StringTest() throws Exception {
            String invalidId = "abc";

            mockMvc.perform(delete("/carts/{id}", invalidId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)))
                    .andExpect(jsonPath("$.message", is("Invalid input")));
        }

        @Test
        void removeCartById_BadRequest_DoubleTest() throws Exception {
            double invalidId = 2.5;

            mockMvc.perform(delete("/carts/{id}", invalidId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)))
                    .andExpect(jsonPath("$.message", is("Invalid input")));
        }
    }

    @Nested
    @DisplayName("DELETE - Delete product by id")
    class RemoveProductByIdEndpoint {
        @Test
        void removeCartProductByIdTest() throws Exception {

            mockMvc.perform(delete("/carts/products/{id}", cartProductId))
                    .andExpect(status().isOk());
        }

        @Test
        void removeCartProductById_NotFoundTest() throws Exception {
            Long cartProductId = 9999L;

            mockMvc.perform(delete("/carts/products/{id}", cartProductId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code", is(404)))
                    .andExpect(jsonPath("$.message", is("No se encontró el CartProduct con ID: " + cartProductId)));

        }

        @Test
        void removeCartProductById_BadRequest_StringTest() throws Exception {
            String invalidId = "abc";

            mockMvc.perform(delete("/carts/products/{id}", invalidId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)))
                    .andExpect(jsonPath("$.message", is("Invalid input")));
        }

        @Test
        void removeCartProductById_BadRequest_DoubleTest() throws Exception {
            double invalidId = 2.5;

            mockMvc.perform(delete("/carts/products/{id}", invalidId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)))
                    .andExpect(jsonPath("$.message", is("Invalid input")));
        }
    }

    @Nested
    @DisplayName("POST - Tests for adding a cart by user id")
    class AddCartByUserIdEndpoint {

        @Test
        void addCartByUserIdTest() throws Exception {
            Long userId = 10L;
            wireMockServer.stubFor(WireMock.get(urlMatching("/users/.*"))
                    .willReturn(
                            aResponse()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                    .withBody(JsonData.USER.getJson())));

            mockMvc.perform(post("/carts/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().string("true"));;
        }

        @Test
        void addCartByUserId_BadRequest_StringTest() throws Exception {
            String userId = "badFormatId";

            mockMvc.perform(post("/carts/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)))
                    .andExpect(jsonPath("$.message", is("Invalid input")));
        }

        @Test
        void addCartByUserId_BadRequest_DoubleTest() throws Exception {
            Double userId = 1.1;

            mockMvc.perform(post("/carts/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)))
                    .andExpect(jsonPath("$.message", is("Invalid input")));
        }
    }

    @Nested
    @DisplayName("POST - Tests for adding a product to the cart")
    class AddProductToCartIdEndpoint {

        @Test
        void postCartProductTest() throws Exception {

            String jsonContent = JsonData.CARTPRODUCT.getJson();
            System.out.println("JSON Content: " + jsonContent);

            mockMvc.perform(post("/carts/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isCreated());
        }

        @Test
        void postCartProduct_BadRequest_Test() throws Exception {
            String invalidInput = "a";
            String cartProductJson = "{\n" +
                    "  \"cart\": {\n" +
                    "    \"id\": 1\n" +
                    "  },\n" +
                    "  \"productId\": 1,\n" +
                    "  \"productName\": \"Pride and Prejudice\",\n" +
                    "  \"productCategory\": \"Books\",\n" +
                    "  \"productDescription\": \"Book by Jane Austen\",\n" +
                    "  \"quantity\": \"" + invalidInput + "\",\n" +
                    "  \"price\": 20.00\n" +
                    "}";

            String expectedErrorMessage = "Invalid JSON format: JSON parse error: Cannot deserialize value of type `java.lang.Integer` from String \""
                    + invalidInput + "\": not a valid `java.lang.Integer` value";

            //When
            mockMvc.perform(post("/carts/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cartProductJson))
                    // Then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is(400)))
                    .andExpect(jsonPath("$.message", is(expectedErrorMessage)));
        }
    }
}
