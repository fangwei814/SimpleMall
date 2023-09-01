package com.fangw.simplemall.search.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.fangw.common.es.SkuEsModel;
import com.fangw.simplemall.search.config.SimplemallElasticSearchConfig;
import com.fangw.simplemall.search.constant.EsConstant;
import com.fangw.simplemall.search.service.MallSearchService;
import com.fangw.simplemall.search.vo.SearchParam;
import com.fangw.simplemall.search.vo.SearchResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam param) {
        // 动态构建DSL语句
        SearchResult result = null;

        // 1.生成检索请求
        SearchRequest searchRequest = buildSearchRequrest(param);

        try {
            // 2.执行检索请求
            SearchResponse response = client.search(searchRequest, SimplemallElasticSearchConfig.COMMON_OPTIONS);

            // 3.分析相应数据并封装成需要的指定形式
            if (Objects.nonNull(response)) {
                result = buildSearchResult(response, param);
            }
        } catch (IOException e) {
            log.error("ES搜索商品失败", e);
        }

        return result;
    }

    /**
     * 构建结果数据
     *
     * @param response
     *            执行检索请求获得的响应数据
     * @return 封装成需要的指定格式并返回
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();

        // 1.返回的所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if (Objects.nonNull(hits) && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (StringUtils.isNotBlank(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);

        // 2.当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrsAgg = response.getAggregations().get("attrs_agg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 属性id
            long attrId = bucket.getKeyAsNumber().longValue();
            // 属性名字
            String attrName =
                ((ParsedStringTerms)bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            // 属性所有值
            List<String> attrValues =
                ((ParsedStringTerms)bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                    return ((Terms.Bucket)item).getKeyAsString();
                }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        // 3.保存当前商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 1、得到品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            // 2、得到品牌的name
            String brandName = ((ParsedStringTerms)bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0)
                .getKeyAsString();
            // 3、得到品牌的img
            String brandImg =
                ((ParsedStringTerms)bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        // 4.保存当前商品所设计的所有分类信息
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 得到分类id
            catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());
            // 得到分类name
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCtatLogs(catalogVos);

        // 5.分页信息
        // 分页信息-页码
        result.setPageNum(param.getPageNum());
        // 分页信息-总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        // 分页信息-总页码 (总记录数 对 每页数求余数）
        int totalPages = (int)total % EsConstant.PRODUCT_PAGESIZE == 0 ? ((int)total / EsConstant.PRODUCT_PAGESIZE)
            : ((int)total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);
        // 分页信息-导航栏
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        return result;
    }

    /**
     * 准备检索请求 模糊匹配：过滤（按照属性、分类、品牌、价格区间、库存）、排序、分页、高亮，聚合分析
     *
     * @return SearchRequest
     */
    private SearchRequest buildSearchRequrest(SearchParam param) {
        // 用来构建DSL语句
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /*
         * 查询：模糊匹配，过滤（按照分类、品牌、属性、库存、价格区间）
         */
        // 1.构建bool-query
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

        // 1.1 match模糊匹配
        if (StringUtils.isNotBlank(param.getKeyword())) {
            boolBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        // 1.2 filter过滤，按照分类、品牌、属性、库存、价格区间
        // 1.2.1 按照三级分类id过滤
        if (Objects.nonNull(param.getCatalog3Id())) {
            boolBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        // 1.2.2 品牌id
        if (Objects.nonNull(param.getBrandId()) && !param.getBrandId().isEmpty()) {
            boolBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        // 1.2.3 按照所有指定的属性
        if (Objects.nonNull(param.getAttrs()) && !param.getAttrs().isEmpty()) {
            for (String attrStr : param.getAttrs()) {
                // attrs=1_其他:安卓&attrs=2_5寸:1.5寸
                BoolQueryBuilder nestedBoolBuilder = QueryBuilders.boolQuery();

                // 拆分提取
                String[] s = attrStr.split("_");
                String attrId = s[0]; // 检索的属性id
                String[] attrValues = s[1].split(":"); // 这个属性检索用的值
                nestedBoolBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                // 生成嵌入式的查询nested
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolBuilder, ScoreMode.None);
                boolBuilder.filter(nestedQuery);
            }
        }

        // 1.2.4 是否有库存
        if (Objects.nonNull(param.getHasStock())) {
            boolBuilder.filter(QueryBuilders.termsQuery("hasStock", param.getHasStock() == 1));
        }

        // 1.2.5 价格区间
        if (StringUtils.isNotBlank(param.getSkuPrice())) {
            // 1_500/_500/500_
            // 拆分区间
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                // 如果是小于某
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                } else {
                    rangeQuery.gte(s[0]);
                }
            }

            boolBuilder.filter(rangeQuery);
        }
        sourceBuilder.query(boolBuilder);

        // 2.排序
        if (StringUtils.isNotBlank(param.getSort())) {
            String sort = param.getSort();

            // sort=saleCount_desc/asc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }

        // 3.分页
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        // 4.高亮
        // 模糊匹配才有必要高亮
        if (StringUtils.isNotBlank(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        // 5.聚合分析
        // 5.1 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1)); // 品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        // 5.2 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);

        // 5.3 属性聚合
        NestedAggregationBuilder attrs_agg = AggregationBuilders.nested("attrs_agg", "attrs");
        // 聚合出当前所有的attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        // 聚合分析出当前attr_id对应的名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // 聚合分析出当前attr_id对应所有可能的属性值attrValue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrs_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attrs_agg);

        SearchRequest searchRequest = new SearchRequest(new String[] {EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }
}
