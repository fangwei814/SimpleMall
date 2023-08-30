package com.fangw.simplemall.search.service;

import java.io.IOException;
import java.util.List;

import com.fangw.common.es.SkuEsModel;

public interface ProductSaveService {

    /**
     * 更新商品状态为上架
     * 
     * @param skuEsModel
     * @return
     */
    boolean productStatsUp(List<SkuEsModel> skuEsModel) throws IOException;
}
