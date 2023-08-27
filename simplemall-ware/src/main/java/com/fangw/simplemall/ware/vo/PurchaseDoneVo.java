package com.fangw.simplemall.ware.vo;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id;// 采购单id

    private List<PurchaseItemDoneVo> items;
}
