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
import com.fangw.simplemall.member.vo.SocialUser;

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

    @Override
    public MemberEntity login(SocialUser socialUser) {
        // String uid = socialUser.getUid();
        // // 1、判断当前社交用户是否已经登录过系统
        // MemberDao memberDao = this.baseMapper;
        // MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        // if (memberEntity != null) {
        // // 2、这个用户已经注册过
        // MemberEntity update = new MemberEntity();
        // update.setId(memberEntity.getId());
        // update.setAccessToken(socialUser.getAccess_token());
        // update.setExpiresIn(socialUser.getExpires_in());
        //
        // // 需要修改 登录令牌 和 登录令牌的过期时间
        // memberDao.updateById(update);
        //
        // memberEntity.setAccessToken(socialUser.getAccess_token());
        // memberEntity.setExpiresIn(socialUser.getExpires_in());
        // return memberEntity;
        // } else {
        // // 2、没有查到当前社交用户对应的记录，我们需要注册一个
        // MemberEntity regist = new MemberEntity();
        // try{
        // // 3、查询当前社交用户的社交账号信息（昵称，性别等）
        // Map<String,String> query = new HashMap<>();
        // query.put("access_token",socialUser.getAccess_token());
        // query.put("uid",socialUser.getUid());
        // HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new
        // HashMap<String, String>(), query);
        // if (response.getStatusLine().getStatusCode() == 200) {
        // // 查询成功
        // String json = EntityUtils.toString(response.getEntity());
        // JSONObject jsonObject = JSON.parseObject(json);
        // // 当前社交账号的信息
        // String name = jsonObject.getString("name");
        // String gender = jsonObject.getString("gender");
        // String profile_image_url = jsonObject.getString("profile_image_url");
        // regist.setNickname(name);
        // regist.setGender("m".equals(gender)?1:0);
        // regist.setHeader(profile_image_url);
        // }
        // }catch (Exception e){
        //
        // }
        // regist.setSocialUid(socialUser.getUid());
        // regist.setAccessToken(socialUser.getAccess_token());
        // regist.setExpiresIn(socialUser.getExpires_in());
        // memberDao.insert(regist);
        // // 设置默认等级
        // MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        // regist.setLevelId(levelEntity.getId());
        // return regist;
        return null;
    }
}