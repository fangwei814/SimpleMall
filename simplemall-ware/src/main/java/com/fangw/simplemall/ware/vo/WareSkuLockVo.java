package com.fangw.simplemall.ware.vo;

import java.util.List;

import lombok.Data;

/**
 * 锁定库存的vo
 **/

@Data
public class WareSkuLockVo {

    private String orderSn;

    /** 需要锁住的所有库存信息 **/
    private List<OrderItemVo> locks;

}
