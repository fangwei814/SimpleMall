package com.fangw.simplemall.order.vo;

import com.fangw.simplemall.order.entity.OrderEntity;

import lombok.Data;

@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    /** 错误状态码 **/
    private Integer code;

}