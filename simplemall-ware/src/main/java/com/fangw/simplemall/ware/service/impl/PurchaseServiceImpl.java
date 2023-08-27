package com.fangw.simplemall.ware.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.constant.WareConstant;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.ware.dao.PurchaseDao;
import com.fangw.simplemall.ware.entity.PurchaseDetailEntity;
import com.fangw.simplemall.ware.entity.PurchaseEntity;
import com.fangw.simplemall.ware.service.PurchaseDetailService;
import com.fangw.simplemall.ware.service.PurchaseService;
import com.fangw.simplemall.ware.vo.MergeVo;

@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page =
            this.page(new Query<PurchaseEntity>().getPage(params), new QueryWrapper<PurchaseEntity>());

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        IPage<PurchaseEntity> page =
            this.page(new Query<PurchaseEntity>().getPage(params), new LambdaQueryWrapper<PurchaseEntity>()
                .eq(PurchaseEntity::getStatus, 0).or().eq(PurchaseEntity::getStatus, 1));

        return new PageUtils(page);
    }

    @Override
    public void mergePurchase(MergeVo mergeVo) {
        // 1.获取采购单id
        Long purchaseId = mergeVo.getPurchaseId();
        if (Objects.isNull(purchaseId)) {
            // 创建采购单
            PurchaseEntity purchase = new PurchaseEntity();

            // 设置状态、时间
            purchase.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchase.setCreateTime(new Date());
            purchase.setUpdateTime(new Date());

            // 保存
            save(purchase);

            // 获取id
            purchaseId = purchase.getId();
        }

        // 2.更新采购需求对应的采购单id
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = mergeVo.getItems().stream().map(id -> {
            // 每一项采购需求的id
            // 设置采购需求的采购单id
            PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
            purchaseDetail.setId(id); // 用来指示更新哪个
            purchaseDetail.setPurchaseId(finalPurchaseId);
            // 设置状态
            purchaseDetail.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());

            return purchaseDetail;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        // 3.更新采购单
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}