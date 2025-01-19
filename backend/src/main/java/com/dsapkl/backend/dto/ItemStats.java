package com.dsapkl.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemStats {
    private long totalCount;
    private long lowStockCount;
    private long onSaleCount;
    private long soldOutCount;
} 