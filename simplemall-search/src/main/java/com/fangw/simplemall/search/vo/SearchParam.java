package com.fangw.simplemall.search.vo;

import java.util.List;

import lombok.Data;

@Data
public class SearchParam {
    private String keyword; // 页面传递过来的检索参数，相当于全文匹配关键字
    private Long catalog3Id; // 三级分类的id

    /**
     * sort=saleCount_desc/asc sort=skuPrice_asc/desc sort=hotScore_asc/desc
     */
    private String sort; // 排序条件

    /**
     * 好多的过滤条件 hasStock、skuPrice区间、brandId、catalog3Id、 hasStock=0/1 skuPrice=1_500/_500/500_ brandId=1
     *
     */
    private Integer hasStock = 1; // 是否只显示有货 v 0(无库存) 1(有库存)
    private String skuPrice; // 价格区间查询
    private List<Long> brandId; // 按照品牌进行查询，可以多选
    private List<String> attrs; // 按照属性进行筛选
    private Integer pageNum = 1; // 页码 默认第1页
}
