package com.fangw.simplemall.ware.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
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

        // todo: 确认采购单状态是0,1才可以合并

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

    @Override
    @Transactional
    public void received(List<Long> ids) {
        // 1.改变采购单的状态为派出
        List<PurchaseEntity> collect = ids.stream().map(this::getById).filter(item -> {
            // 过滤实体
            // 主要是采购单的状态要是新建或者已分配
            return item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode();
        }).peek(item -> {
            // 更新状态
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
        }).collect(Collectors.toList());

        // 更新
        // todo: collect是否要判空
        updateBatchById(collect);

        // 2.改变采购单中每个采购需求的状态
        collect.forEach(item -> {
            // 查询每一个采购需求
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchseId(item.getId());
            List<PurchaseDetailEntity> collect1 = entities.stream().map(entity -> {
                // 重新设置
                PurchaseDetailEntity entity1 = new PurchaseDetailEntity();
                entity1.setId(entity.getId());
                entity1.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return entity1;
            }).collect(Collectors.toList());

            purchaseDetailService.updateBatchById(collect1);
        });
    }

}