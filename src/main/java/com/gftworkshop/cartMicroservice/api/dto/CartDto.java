package com.gftworkshop.cartMicroservice.api.dto;

import com.gftworkshop.cartMicroservice.model.CartProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {

    private Long id;
    private Long userId;
    private List<CartProduct> cartProducts;
    private BigDecimal totalPrice;

}
