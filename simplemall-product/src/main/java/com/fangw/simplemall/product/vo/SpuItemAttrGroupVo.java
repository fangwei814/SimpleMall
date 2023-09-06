package com.fangw.simplemall.product.vo;

import java.util.List;

import lombok.Data;

// 规格参数
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}
