package com.dsapkl.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {

    private long itemId;
    private int count;
    private int price;
    private String itemName;

}
