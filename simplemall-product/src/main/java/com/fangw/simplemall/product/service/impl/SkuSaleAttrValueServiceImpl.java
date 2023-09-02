package com.fangw.simplemall.product.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.product.dao.SkuSaleAttrValueDao;
import com.fangw.simplemall.product.entity.SkuSaleAttrValueEntity;
import com.fangw.simplemall.product.service.SkuSaleAttrValueService;
import com.fangw.simplemall.product.vo.SkuItemSaleAttrsVo;

@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity>
    implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page =
            this.page(new Query<SkuSaleAttrValueEntity>().getPage(params), new QueryWrapper<SkuSaleAttrValueEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrsVo> getSaleAttrsBySpuId(Long spuId) {
        return baseMapper.getSaleAttrsBySpuId(spuId);
    }

}