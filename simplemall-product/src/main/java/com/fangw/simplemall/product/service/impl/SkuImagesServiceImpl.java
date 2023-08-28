package com.fangw.simplemall.product.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.product.dao.SkuImagesDao;
import com.fangw.simplemall.product.entity.SkuImagesEntity;
import com.fangw.simplemall.product.service.SkuImagesService;

@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesDao, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page =
            this.page(new Query<SkuImagesEntity>().getPage(params), new QueryWrapper<SkuImagesEntity>());

        return new PageUtils(page);
    }

    @Override
    public void saveImages(Long id, List<String> images) {
        if (Objects.isNull(images) || images.isEmpty()) {

        } else {
            List<SkuImagesEntity> collect = images.stream().map(image -> {
                // 生成所有的entity
                SkuImagesEntity imagesEntity = new SkuImagesEntity();
                imagesEntity.setSkuId(id);
                imagesEntity.setImgUrl(image);

                return imagesEntity;
            }).collect(Collectors.toList());

            saveBatch(collect);
        }
    }

}