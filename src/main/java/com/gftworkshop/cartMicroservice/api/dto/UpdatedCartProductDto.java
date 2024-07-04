package com.gftworkshop.cartMicroservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class UpdatedCartProductDto {
    private Long id;
    private Integer quantity;
}
