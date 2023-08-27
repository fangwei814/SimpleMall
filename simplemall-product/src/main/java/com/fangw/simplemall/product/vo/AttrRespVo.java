package com.fangw.simplemall.product.vo;

import lombok.Data;

@Data
public class AttrRespVo extends AttrVo {
    /**
     * "catelogName": "手机/数码/手机", //所属分类名字 "groupName": "主体", //所属分组名字
     */
    private String catelogName;
    private String groupName;

    /**
     * 分类的多级路径
     */
    private Long[] catelogPath;
}
