package com.fangw.simplemall.coupon.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.coupon.dao.SeckillSessionDao;
import com.fangw.simplemall.coupon.entity.SeckillSessionEntity;
import com.fangw.simplemall.coupon.entity.SeckillSkuRelationEntity;
import com.fangw.simplemall.coupon.service.SeckillSessionService;
import com.fangw.simplemall.coupon.service.SeckillSkuRelationService;

@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity>
    implements SeckillSessionService {
    @Autowired
    SeckillSkuRelationService skuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page =
            this.page(new Query<SeckillSessionEntity>().getPage(params), new QueryWrapper<SeckillSessionEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLates3DaySession() {
        List<SeckillSessionEntity> list = list(new LambdaQueryWrapper<SeckillSessionEntity>()
            .between(SeckillSessionEntity::getStartTime, startTime(), endTime()));

        if (Objects.nonNull(list) && !list.isEmpty()) {
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                // 对每个秒杀活动展示所有关联商品
                Long id = session.getId();
                List<SeckillSkuRelationEntity> relations =
                    skuRelationService.list(new LambdaQueryWrapper<SeckillSkuRelationEntity>()
                        .eq(SeckillSkuRelationEntity::getPromotionSessionId, id));
                session.setRelationSkus(relations);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 当前时间
     * 
     * @return
     */
    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);

        // 格式化时间
        String startFormat = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return startFormat;
    }

    /**
     * 结束时间
     * 
     * @return
     */
    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalDate plus = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(plus, max);

        // 格式化时间
        String endFormat = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return endFormat;
    }

}