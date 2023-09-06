package com.fangw.simplemall.member.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.member.dao.MemberLevelDao;
import com.fangw.simplemall.member.entity.MemberLevelEntity;
import com.fangw.simplemall.member.service.MemberLevelService;

@Service("memberLevelService")
public class MemberLevelServiceImpl extends ServiceImpl<MemberLevelDao, MemberLevelEntity>
    implements MemberLevelService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberLevelEntity> page =
            this.page(new Query<MemberLevelEntity>().getPage(params), new QueryWrapper<MemberLevelEntity>());

        return new PageUtils(page);
    }

    @Override
    public MemberLevelEntity getDefaultLevel() {
        return getOne(new LambdaQueryWrapper<MemberLevelEntity>().eq(MemberLevelEntity::getDefaultStatus, 1));
    }

}