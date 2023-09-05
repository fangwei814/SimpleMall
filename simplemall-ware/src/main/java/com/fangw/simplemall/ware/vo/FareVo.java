package com.fangw.simplemall.ware.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 运费
 */
@Data
public class FareVo {
    private MemberAddressVo addressVo;
    private BigDecimal fare;
}
