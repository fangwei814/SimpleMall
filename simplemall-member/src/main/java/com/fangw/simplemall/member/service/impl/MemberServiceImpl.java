package com.fangw.simplemall.member.service.impl;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fangw.common.exception.PhoneExistException;
import com.fangw.common.exception.UsernameExistException;
import com.fangw.common.utils.PageUtils;
import com.fangw.common.utils.Query;
import com.fangw.simplemall.member.dao.MemberDao;
import com.fangw.simplemall.member.entity.MemberEntity;
import com.fangw.simplemall.member.entity.MemberLevelEntity;
import com.fangw.simplemall.member.service.MemberLevelService;
import com.fangw.simplemall.member.service.MemberService;
import com.fangw.simplemall.member.vo.MemberLoginVo;
import com.fangw.simplemall.member.vo.MemberRegistVo;

@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page =
            this.page(new Query<MemberEntity>().getPage(params), new QueryWrapper<MemberEntity>());

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity memberEntity = new MemberEntity();
        // 1.获取默认会员等级
        MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        // 2.设置用户名手机号
        // 判断是否重复
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());

        // 3.密码加密存储
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(vo.getPassWord());
        memberEntity.setPassword(encode);

        // 其他默认信息

        // 保存
        save(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        if (count(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getMobile, phone)) > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserNameUnique(String userName) throws UsernameExistException {
        if (count(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getUsername, userName)) > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        // 1.查询数据库账号
        // 只要手机号或者账号正确都能匹配
        MemberEntity entity = getOne(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getMobile, loginacct).or()
            .eq(MemberEntity::getUsername, loginacct));
        if (Objects.isNull(entity)) {
            return null;
        } else {
            // 2.判断密码是福哦正确
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if (matches) {
                return entity;
            } else {
                return null;
            }
        }
    }
}