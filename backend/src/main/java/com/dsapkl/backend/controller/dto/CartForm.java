package com.dsapkl.backend.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartForm {

    private Long itemId;
    private int count;
}
