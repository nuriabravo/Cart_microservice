package com.gftworkshop.cartMicroservice.services;

import com.gftworkshop.cartMicroservice.api.dto.CartProductDto;
import com.gftworkshop.cartMicroservice.api.dto.Product;
import com.gftworkshop.cartMicroservice.exceptions.ExternalMicroserviceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ProductService {
    private final RestClient restClient;
    private final String baseUrl;
    private final String productUri;
    private final String discountUrl;

    public String findByIdsUrl;
    public String volumePromotionUrl;


    public ProductService(RestClient restClient,
                          @Value("${catalog.api.base-url}") String baseUrl,
                          @Value("${catalog.api.product-uri}") String productUri,
                          @Value("${catalog.api.discount-uri}") String discountUrl,
                          @Value("${catalog.api.products-uri}") String findByIdsUrl,
                          @Value("${catalog.api.volumePromotion-uri}") String volumePromotionUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
        this.productUri = productUri;
        this.discountUrl = discountUrl;
        this.findByIdsUrl=findByIdsUrl;
        this.volumePromotionUrl=volumePromotionUrl;
    }

    public Product getProductById(Long productId) {
        return restClient.get()
                .uri(baseUrl + productUri, productId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ((request, response) -> {
                    throw new ExternalMicroserviceException("CATALOG MICROSERVICE EXCEPTION: " + response.getStatusText()+" "+response.getBody());
                }))
                .body(Product.class);
    }

    public float getProductDiscountedPrice(Long productId, int quantity) {
        return restClient.get()
                .uri(baseUrl + discountUrl, productId, quantity)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ((request, response) -> {
                    throw new ExternalMicroserviceException("CATALOG MICROSERVICE EXCEPTION: " + response.getStatusText()+" "+response.getBody());
                }))
                .body(Float.class);
    }



    public List<Product> findProductsByIds(List<Long> ids){
        String url = baseUrl + findByIdsUrl;
        return List.of(Objects.requireNonNull(restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ids)
                .retrieve()
                .body(Product[].class)));
    }


    public List<Product> getProductByIdWithDiscountedPrice(List<CartProductDto> cartProducts) {
        return List.of(Objects.requireNonNull(restClient.post()
                .uri(baseUrl + volumePromotionUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cartProducts)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ((request, response) -> {
                    throw new ExternalMicroserviceException("CATALOG MICROSERVICE EXCEPTION: " + response.getStatusText()+" "+response.getBody());
                }))
                .body(Product[].class)));
    }
}
