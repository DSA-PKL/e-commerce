package com.dsapkl.backend.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartOrderDto {

    List<CartForm> cartOrderDtoList;

//    public CartOrderServiceDto toServiceDto() {
//        return CartOrderServiceDto.builder()
//                .cartOrderDtoList(cartOrderDtoList)
//                .build();
//    }

    public CartOrderDto() {
    }

}