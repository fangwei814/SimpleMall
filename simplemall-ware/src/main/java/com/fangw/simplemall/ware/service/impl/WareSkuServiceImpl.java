package com.fangw.simplemall.ware.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.exception.NoStockException;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.common.utils.R;
import com.fangw.common.vo.SkuHasStockVo;
import com.fangw.simplemall.ware.dao.WareSkuDao;
import com.fangw.simplemall.ware.entity.WareSkuEntity;
import com.fangw.simplemall.ware.feign.ProductFeignService;
import com.fangw.simplemall.ware.service.WareSkuService;
import com.fangw.simplemall.ware.vo.OrderItemVo;
import com.fangw.simplemall.ware.vo.WareSkuLockVo;

import lombok.Data;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    private WareSkuDao wareSkuDao;
    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /*
        wareId: 123,//仓库id
        skuId: 123//商品id
         */
        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();

        String wareId = (String)params.get("wareId");
        if (StringUtils.isNotBlank(wareId)) {
            wrapper.eq(WareSkuEntity::getWareId, wareId);
        }

        String skuId = (String)params.get("skuId");
        if (StringUtils.isNotBlank(skuId)) {
            wrapper.eq(WareSkuEntity::getSkuId, skuId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 如果没有记录就新增
        LambdaQueryWrapper<WareSkuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WareSkuEntity::getWareId, wareId).eq(WareSkuEntity::getSkuId, skuId);
        long count = count(wrapper);
        if (count == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);

            // sku名字
            try {
                R ret = productFeignService.info(skuId);
                Map<String, Object> skuInfo = (Map<String, Object>)ret.get("skuInfo");
                if (ret.getCode() == 0) {
                    wareSkuEntity.setSkuName((String)skuInfo.get("skuName"));
                }
            } catch (Exception e) {

            }
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        // 根据skuIds查询总的库存量
        return skuIds.stream().map(skuId -> {
            // 每个id查找库存量
            Long count = baseMapper.getSkuStock(skuId);

            // 设置vo
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(!Objects.isNull(count) && count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean orderLockStock(WareSkuLockVo vo) {
        // 1、每个商品在哪个库存里有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHashStock> collect = locks.stream().map(item -> {
            SkuWareHashStock stock = new SkuWareHashStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            // 查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHashSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // 2、锁定库存
        for (SkuWareHashStock hashStock : collect) {
            boolean skuStocked = false;
            Long skuId = hashStock.getSkuId();
            List<Long> wareIds = hashStock.getWareId();
            if (wareIds == null || wareIds.isEmpty()) {
                // 没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                // 成功就返回1，否则就返回0
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hashStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    break;
                } else {
                    // 当前仓库锁失败，重试下一个仓库

                }
            }
            if (!skuStocked) {
                // 当前商品所有仓库都没有锁住，其他商品也不需要锁了，直接返回没有库存了
                throw new NoStockException(skuId);
            }
        }

        // 3、运行到这，全部都是锁定成功的
        return true;
    }

    @Data
    class SkuWareHashStock {
        private Long skuId; // skuid
        private Integer num; // 锁定件数
        private List<Long> wareId; // 锁定仓库id
    }

}