package com.fangw.simplemall.ware.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.to.OrderTo;
import com.fangw.common.to.mq.StockLockedTo;
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

    /**
     * 1、库存自动解锁 下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁 2、订单失败 锁库存失败，则库存回滚了，这种情况无需解锁
     * 如何判断库存是否锁定失败呢？查询数据库关于这个订单的锁库存消息即可 自动ACK机制：只要解决库存的消息失败，一定要告诉服务器解锁是失败的。启动手动ACK机制
     * 
     * @param to
     *
     */
    void unlockStock(StockLockedTo to);

    /**
     * 防止订单服务卡顿，导致订单状态一直修改不了，库存消息优先到期。查订单状态肯定是新建状态，什么都不做就走了 导致卡顿的订单，永远不能解锁库存
     * 
     * @param orderTo
     */
    void unlockStock(OrderTo orderTo);
}
