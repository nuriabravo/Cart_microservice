package com.gftworkshop.cartMicroservice.services;

import com.gftworkshop.cartMicroservice.api.dto.User;
import com.gftworkshop.cartMicroservice.exceptions.ExternalMicroserviceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class UserService {
    public String baseUrl;
    public String usersUri;
    private final RestClient restClient;


    public UserService(RestClient restClient,
                       @Value("${users.api.base-url}") String baseUrl,
                       @Value("${users.api.users-uri}") String usersUri) {
        this.restClient = restClient;
        this.usersUri=usersUri;
        this.baseUrl=baseUrl;
    }

    public User getUserById(Long userId) {
        return restClient.get()
                .uri(baseUrl + usersUri, userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ExternalMicroserviceException("USER MICROSERVICE EXCEPTION: " + response.getStatusText()+" "+response.getBody());
                })
                .body(User.class);
    }
}
