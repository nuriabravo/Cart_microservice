package com.gftworkshop.cartMicroservice.services;

import com.gftworkshop.cartMicroservice.api.dto.CartProductDto;

public interface CartProductService {

    int updateQuantity(Long id, int quantity);

    CartProductDto removeProduct(Long id);

}
