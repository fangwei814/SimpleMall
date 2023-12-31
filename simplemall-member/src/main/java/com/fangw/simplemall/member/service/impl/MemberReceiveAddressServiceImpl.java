package com.fangw.simplemall.member.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.member.dao.MemberReceiveAddressDao;
import com.fangw.simplemall.member.entity.MemberReceiveAddressEntity;
import com.fangw.simplemall.member.service.MemberReceiveAddressService;

@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity>
    implements MemberReceiveAddressService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(new Query<MemberReceiveAddressEntity>().getPage(params),
            new QueryWrapper<MemberReceiveAddressEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<MemberReceiveAddressEntity> getAddress(Long memberId) {
        return list(
            new LambdaQueryWrapper<MemberReceiveAddressEntity>().eq(MemberReceiveAddressEntity::getMemberId, memberId));
    }

}