package com.fangw.simplemall.ware.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.ware.entity.PurchaseEntity;
import com.fangw.simplemall.ware.vo.MergeVo;
import com.fangw.simplemall.ware.vo.PurchaseDoneVo;

/**
 * 采购信息
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:50:20
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    /**
     * 合并采购需求
     * 
     * @param mergeVo
     */
    void mergePurchase(MergeVo mergeVo);

    /**
     * 领取采购单
     * 
     * @param ids
     */
    void received(List<Long> ids);

    /**
     * 完成采购单
     * 
     * @param doneVo
     */
    void done(PurchaseDoneVo doneVo);
}
