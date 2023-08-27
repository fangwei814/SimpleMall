package com.fangw.simplemall.ware.vo;

import java.util.List;

import lombok.Data;

@Data
public class MergeVo {

    private Long purchaseId; // 整单id
    private List<Long> items;// [1,2,3,4] //合并项集合
}
