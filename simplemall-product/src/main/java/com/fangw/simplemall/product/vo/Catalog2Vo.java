package com.fangw.simplemall.product.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catalog2Vo {
    private String catalog1Id; // 父分类的id（一级分类）
    private List<Catelog3Vo> catalog3List; // 三级子分类
    private String id;
    private String name;

    /**
     * 三级分类 Vo
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catelog3Vo {
        private String catalog2Id; // 父分类的id（二级分类）
        private String id;
        private String name;
    }
}
