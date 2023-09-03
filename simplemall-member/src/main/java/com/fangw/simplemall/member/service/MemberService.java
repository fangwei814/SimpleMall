package com.fangw.simplemall.member.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.exception.PhoneExistException;
import com.fangw.common.exception.UsernameExistException;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.member.entity.MemberEntity;
import com.fangw.simplemall.member.vo.MemberLoginVo;
import com.fangw.simplemall.member.vo.MemberRegistVo;
import com.fangw.simplemall.member.vo.SocialUser;

/**
 * 会员
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:45:08
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 注册
     * 
     * @param vo
     */
    void regist(MemberRegistVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUserNameUnique(String userName) throws UsernameExistException;

    /**
     * 登录
     * 
     * @param vo
     * @return
     */
    MemberEntity login(MemberLoginVo vo);

    /**
     * 社交账号登录
     * 
     * @param socialUser
     * @return
     */
    MemberEntity login(SocialUser socialUser);
}
