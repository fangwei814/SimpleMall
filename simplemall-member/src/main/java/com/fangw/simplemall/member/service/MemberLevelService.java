package com.fangw.simplemall.member.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fangw.common.utils.PageUtils;
import com.fangw.simplemall.member.entity.MemberLevelEntity;

/**
 * 会员等级
 *
 * @author fangw
 * @email 1779219498@qq.com
 * @date 2023-08-16 20:45:08
 */
public interface MemberLevelService extends IService<MemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取默认等级
     * 
     * @return
     */
    MemberLevelEntity getDefaultLevel();
}
