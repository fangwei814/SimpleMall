package com.fangw.simplemall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:45:08
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

