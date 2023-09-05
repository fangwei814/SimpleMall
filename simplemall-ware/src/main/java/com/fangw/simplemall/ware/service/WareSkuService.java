package com.fangw.simplemall.ware.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.vo.SkuHasStockVo;
import com.fangw.simplemall.ware.entity.WareSkuEntity;
import com.fangw.simplemall.ware.vo.WareSkuLockVo;

/**
 * 商品库存
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:50:20
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 商品入库
     * 
     * @param skuId
     * @param wareId
     * @param skuNum
     */
    void addStock(Long skuId, Long wareId, Integer skuNum);

    /**
     * 查询指定的skuIds是否有库存
     * 
     * @param skuIds
     * @return
     */
    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    /**
     * 锁定指定订单的库存
     * 
     * @param vo
     * @return (rollbackFor = NoStockException.class) 默认只要是运行时异常都会回滚
     */
    Boolean orderLockStock(WareSkuLockVo vo);
}
