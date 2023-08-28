package com.fangw.common.to;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SpuBoundTo {

    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
