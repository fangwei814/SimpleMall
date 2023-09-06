package com.fangw.simplemall.search.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.fangw.common.es.SkuEsModel;
import com.fangw.simplemall.search.config.SimplemallElasticSearchConfig;
import com.fangw.simplemall.search.constant.EsConstant;
import com.fangw.simplemall.search.service.ProductSaveService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatsUp(List<SkuEsModel> skuEsModel) throws IOException {
        // 保存到es
        // 1.给es中建立索引并建立好映射关系，通过kibana完成
        // 2.给es中保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel esModel : skuEsModel) {
            // 构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(esModel.getSkuId().toString());
            String modelJson = JSON.toJSONString(esModel);
            indexRequest.source(modelJson, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }

        // 发起请求
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, SimplemallElasticSearchConfig.COMMON_OPTIONS);

        // 如果批量错误
        boolean b = bulk.hasFailures(); // true：有错误 false：无错误
        List<String> collect = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
        log.info("商品上架完成:{},返回数据:{}", collect, bulk.toString());

        return b;
    }
}
