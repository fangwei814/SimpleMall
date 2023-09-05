package com.fangw.simplemall.product.service.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.constant.ProductConstant;
import com.fangw.common.es.SkuEsModel;
import com.fangw.common.to.SkuReductionTo;
import com.fangw.common.to.SpuBoundTo;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.common.utils.R;
import com.fangw.common.vo.SkuHasStockVo;
import com.fangw.simplemall.product.dao.SpuInfoDao;
import com.fangw.simplemall.product.entity.*;
import com.fangw.simplemall.product.feign.CouponFeignService;
import com.fangw.simplemall.product.feign.SearchFeignService;
import com.fangw.simplemall.product.feign.WareFeignService;
import com.fangw.simplemall.product.service.*;
import com.fangw.simplemall.product.vo.*;

@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SkuImagesService imagesService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService attrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page =
            this.page(new Query<SpuInfoEntity>().getPage(params), new QueryWrapper<SpuInfoEntity>());

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo vo) {
        // todo: 高级部分完善
        // 1、保存spu基本信息 pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        saveBaseSpuInfo(infoEntity);

        // 2、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        // 3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(infoEntity.getId(), images);

        // 4、保存spu的规格参数;pms_product_attr_value
        // 从vo获取规格参数list
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();

        // 对vo list进行处理
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            // 新建pms_product_attr_value数据库对应实体类
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            // 获取每个vo项对应的id
            valueEntity.setAttrId(attr.getAttrId());
            // 根据id查attr数据库中对应的实体类
            AttrEntity id = attrService.getById(attr.getAttrId());
            // 查到名字封装到实体类中
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);

        // 5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        // 5、保存当前spu对应的所有sku信息；
        List<Skus> skus = vo.getSkus();
        if (Objects.nonNull(skus) && !skus.isEmpty()) {
            skus.forEach(item -> {
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                // private String skuName;
                // private BigDecimal price;
                // private String skuTitle;
                // private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                // 5.1）、sku的基本信息；pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    // 返回true就是需要，false就是剔除
                    return StringUtils.isNotBlank(entity.getImgUrl());
                }).collect(Collectors.toList());

                // 5.2）、sku的图片信息；pms_sku_image
                skuImagesService.saveBatch(imagesEntities);
                // TODO 没有图片路径的无需保存

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);

                    return attrValueEntity;
                }).collect(Collectors.toList());
                // 5.3）、sku的销售属性信息：pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0
                    || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }

            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        /*
         * 要利用的
         * key: '华为',//检索关键字
            catelogId: 6,//三级分类id
            brandId: 1,//品牌id
            status: 0,//商品状态
         */
        LambdaQueryWrapper<SpuInfoEntity> wrapper = new LambdaQueryWrapper<>();

        // 1.key
        String key = (String)params.get("key");
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(obj -> {
                obj.eq(SpuInfoEntity::getId, key).or().like(SpuInfoEntity::getSpuName, key);
            });
        }

        // 2.catelogId
        String catelogId = (String)params.get("catelogId");
        if (StringUtils.isNotBlank(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq(SpuInfoEntity::getCatalogId, catelogId);
        }

        // 3.brandId
        String brandId = (String)params.get("brandId");
        if (StringUtils.isNotBlank(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq(SpuInfoEntity::getBrandId, brandId);
        }

        // 4.status
        String status = (String)params.get("status");
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(SpuInfoEntity::getPublishStatus, status);
        }

        IPage<SpuInfoEntity> page = page(new Query<SpuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        /*
        private Long skuId;
        private Long spuId;
        private String skuTitle;
        private BigDecimal skuPrice;
        private String skuImd;
        private Long saleCount; // 商品的销量
        private boolean hasStock; // 商品的库存
        private Long hotScore; // 热度评分
        private Long brandId; // 品牌id
        private Long catalogId; // 分类id
        private String brandName; // 品牌名
        private String brandImg; // 品牌图片
        private String catalogName; // 分类的名字
        private List<Attr> attrs;
        
        @Data
        public static class Attr {
            private Long attrId; // 属性id
            private String attrName; // 属性名
            private String attrValue; // 属性值
        }
         */
        // 1.查找当前spuId对应的所有skuId的信息
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 2.查找当前sku的所有可以被检索的属性
        // 先查spuId对应的所有属性，然后获取所有的id
        List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        // 过滤出来可以被检索的属性id
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        HashSet<Long> attrIdSet = new HashSet<>(searchAttrIds); // 利用set查找
        List<SkuEsModel.Attr> attrsList = baseAttrs.stream().filter(item -> {
            return attrIdSet.contains(item.getAttrId());
        }).map(item -> {
            // 拷贝到es里面准备后面赋值
            SkuEsModel.Attr attr = new SkuEsModel.Attr();
            BeanUtils.copyProperties(item, attr);
            return attr;
        }).collect(Collectors.toList());

        // 3.发送远程调用，查询是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getSkusHasStock(skuIds);

            // 封装成map
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {};
            stockMap = r.getData(typeReference).stream()
                .collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}", e);
        }
        Map<Long, Boolean> finalStockMap = stockMap;

        // 4.封装每个sku的信息到es
        List<SkuEsModel> skuEsModels = skus.stream().map(sku -> {
            // 填充sku相关部分
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());

            // 库存
            if (Objects.isNull(finalStockMap)) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            // 热度评分
            skuEsModel.setHotScore(0L);

            // 品牌分类信息
            BrandEntity brand = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandImg(brand.getLogo());
            skuEsModel.setBrandName(brand.getName());
            CategoryEntity category = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(category.getName());

            // 可以被检索的规格属性
            skuEsModel.setAttrs(attrsList);

            return skuEsModel;
        }).collect(Collectors.toList());

        // 5.将数据发给search模块进行保存
        R r = searchFeignService.ProductStatusUp(skuEsModels);
        if (r.getCode() == 0) {
            // 远程调用成功
            // TODO 6、远程调用成功 修改当前SPU的状态为上架
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // 远程调用失败
            // TODO 7、重复调用？接口幂等性；重试机制？
            log.error("商品远程es保存失败");
        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity sku = skuInfoService.getById(skuId);
        return getById(sku.getSpuId());
    }

}