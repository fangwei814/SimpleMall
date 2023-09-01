package com.fangw.simplemall.search.vo;

import java.util.List;

import com.fangw.common.es.SkuEsModel;

import lombok.Data;

@Data
public class SearchResult {

    // 查询到的所有商品信息
    private List<SkuEsModel> products;

    /**
     * 分页信息
     */
    private Integer pageNum; // 当前页码
    private Long total; // 总记录数
    private Integer totalPages;// 总页码
    private List<Integer> pageNavs; // 导航页码

    private List<BrandVo> brands; // 当前查询到的结果，所有涉及到的品牌
    private List<CatalogVo> ctatLogs; // 当前查询到的结果，所有涉及到的分类
    private List<AttrVo> attrs; // 当前查询到的结果，所有涉及到的属性

    // ==================以上返回给页面的所有信息====================
    /**
     * 品牌
     */
    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    /**
     * 分类
     */
    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    /**
     * 属性
     */
    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
